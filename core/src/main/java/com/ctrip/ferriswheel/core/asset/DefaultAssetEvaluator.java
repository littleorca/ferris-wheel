/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.formula.DirectedAcyclicGraph;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluator;
import com.ctrip.ferriswheel.core.formula.eval.ReferenceResolver;
import com.ctrip.ferriswheel.core.util.GraphHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class DefaultAssetEvaluator implements AssetEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAssetEvaluator.class);
    private static final int WORKER_THREADS_PER_WORKBOOK = 4;
    private static final long WAIT_PERIOD_IN_MILLI_SECONDS = 5 * 1000L;
    private static final long REFRESH_TIMEOUT_IN_MILLI_SECONDS = 15 * 60 * 1000L;

    private ReferenceResolver referenceResolver;

    public DefaultAssetEvaluator(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public void evaluate(Asset asset, EvaluationMode mode) {

        FormulaEvaluator evaluator = new FormulaEvaluator(referenceResolver);
        ExecutorService executor = Executors.newFixedThreadPool(WORKER_THREADS_PER_WORKBOOK);
        CompletionService<Long> completionService = new ExecutorCompletionService<>(executor);

        EvaluationContext evaluationContext = new DefaultEvaluationContext(mode, evaluator, completionService);

        Set<Long> dirtyNodes = new HashSet<>();
        Set<Long> volatileNodes = new HashSet<>();
        DirectedAcyclicGraph<Long, Asset> graph = GraphHelper.buildGraph(asset, dirtyNodes, volatileNodes);

        Set<Long> pendingTasks = new HashSet<>();

        if (mode != EvaluationMode.Aggressive) {
            HashSet<Long> keyNodes = new HashSet<>(dirtyNodes);
            if (mode != EvaluationMode.Lazy) {
                keyNodes.addAll(volatileNodes);
            }
            graph.trimOutboundToKeyNodes(keyNodes);
        }

        try {
            long start = System.currentTimeMillis();

            /**
             * The while-loop will break on exception and thus the whole process will be failed.
             * As one task hang up forever can stop the whole process, this behavior is good to
             * prevent the whole process from hanging forever. However, this can be improved to
             * execute as more tasks as possible by checking there dependency relationships.
             */
            while (!graph.isEmpty()) {
                Set<Long> tasks = graph.collectOutboundEnds();
                boolean processedAnyTask = false;
                for (Long id : tasks) {
                    if (pendingTasks.contains(id)) {
                        continue;
                    }
                    processedAnyTask = true;
                    AssetNode assetNode = (AssetNode) referenceResolver.getAssetById(id);
                    if (EvaluationState.PENDING != evaluateAssetNode(assetNode, evaluationContext)) {
                        graph.removeNode(id);
                    } else {
                        pendingTasks.add(id);
                    }
                }

                // check if any pending task has been done.
                Future<Long> future;
                if (!processedAnyTask && !pendingTasks.isEmpty()) {
                    // calculation blocks on pending tasks, so let's poll with waiting
                    future = completionService.poll(WAIT_PERIOD_IN_MILLI_SECONDS, TimeUnit.MILLISECONDS);
                } else {
                    // calculation can be continued despite possible pending tasks, just poll without waiting.
                    future = completionService.poll();
                }
                if (future != null) {
                    Long id = future.get();
                    graph.removeNode(id);
                    pendingTasks.remove(id);
                }

                if (System.currentTimeMillis() - start >= REFRESH_TIMEOUT_IN_MILLI_SECONDS) {
                    throw new RuntimeException("Refresh procedure timed out.");
                }
            }

            while (!pendingTasks.isEmpty()) {
                Future<Long> future = completionService.poll(WAIT_PERIOD_IN_MILLI_SECONDS, TimeUnit.MILLISECONDS);
                if (future != null) {
                    pendingTasks.remove(future.get());
                }
                if (System.currentTimeMillis() - start >= REFRESH_TIMEOUT_IN_MILLI_SECONDS) {
                    throw new RuntimeException("Refresh procedure timed out.");
                }
            }

        } catch (InterruptedException e) {
            LOG.warn("Refresh procedure interrupted.", e);
            executor.shutdownNow();
        } catch (ExecutionException e) {
            LOG.warn("Refresh procedure caught an exception while executing a task.", e);
            executor.shutdownNow();
        } catch (RuntimeException e) {
            LOG.warn("Refresh procedure caught an runtime exception.", e);
            executor.shutdownNow();
        } finally {
            executor.shutdown(); // it's ok to shutdown an executor that already shutdown.
        }

//        if (LOG.isDebugEnabled()) {
//            LOG.debug(toString());
//        }

    }

    private EvaluationState evaluateAssetNode(AssetNode node, EvaluationContext context) {
        LOG.debug("Evaluating asset node: {}({}), mode={}",
                node.getClass(), node.getAssetId(), context.getEvaluationMode());
        return node.evaluate(context);
    }
}
