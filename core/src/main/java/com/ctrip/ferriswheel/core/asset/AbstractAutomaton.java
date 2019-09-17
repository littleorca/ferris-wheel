package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.automaton.AsynchronousAutomaton;
import com.ctrip.ferriswheel.common.automaton.Automaton;

import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;

public abstract class AbstractAutomaton extends AssetNode implements Automaton {

    AbstractAutomaton(AssetManager assetManager) {
        super(assetManager);
    }

    @Override
    public void init() {
        // query(Collections.emptyMap());
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        boolean isForceRefresh = EvaluationMode.Aggressive == context.getEvaluationMode();
        CompletionService<Long> completionService = context.getCompletionService();

        if (this instanceof AsynchronousAutomaton) {
            Future<Long> future = ((AsynchronousAutomaton) this).execute(isForceRefresh,
                    completionService, getAssetId());
            if (future != null) {
                return EvaluationState.PENDING;
            }
        } else {
            execute(isForceRefresh);
        }
        return EvaluationState.DONE;
    }

    @Override
    public void destroy() {
        // dummy
    }

    DefaultTable getTable() {
        return (DefaultTable) getParent();
    }

}
