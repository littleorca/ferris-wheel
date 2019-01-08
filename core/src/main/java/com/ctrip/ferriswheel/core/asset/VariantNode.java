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

import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.common.variant.Variant;

/**
 * Variant node holds variant and optionally a formula.
 * It has asset ID for convenience to trace dependencies.
 */
public interface VariantNode extends DynamicVariant, Asset {

    /**
     * Get node value. If it is a formula node, the returned value represents
     * the evaluated result of the formula.
     *
     * @return
     */
    Variant getData();

    /**
     * Determine whether this node is a formula node or not.
     *
     * @return
     */
    boolean isFormula();

//    /**
//     * Determine whether the evaluated result is out of date or not.
//     *
//     * @return
//     */
//    boolean isDirty();

}
