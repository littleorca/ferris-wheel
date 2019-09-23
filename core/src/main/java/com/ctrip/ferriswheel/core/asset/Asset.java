/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
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
 *
 */

package com.ctrip.ferriswheel.core.asset;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface Asset extends Serializable {
    long UNSPECIFIED_ASSET_ID = -1;

    long getAssetId();

    boolean isValid();

    /**
     * A volatile node may evaluate different result each time even with the
     * same parameters.
     *
     * @return
     */
    boolean isVolatile();

    /**
     * Determine if an asset is phantom node. A phantom node is created by other node
     * and should not be used for dependency since it's not generally available.
     *
     * @return
     */
    boolean isPhantom();

    boolean isDirty();

    Asset getParent();

    List<? extends Asset> getChildren();

    /**
     * Get nodes that the represented node is depending on.
     *
     * @return
     */
    Set<? extends Asset> getDependencies();

    /**
     * Get nodes that depending on the represented node.
     *
     * @return
     */
    Set<? extends Asset> getDependents();

    /**
     * Evaluate the asset with the specified context.
     *
     * @param context
     */
    EvaluationState evaluate(EvaluationContext context);
}
