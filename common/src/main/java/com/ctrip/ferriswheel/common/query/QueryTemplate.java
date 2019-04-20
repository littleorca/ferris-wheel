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

package com.ctrip.ferriswheel.common.query;

import com.ctrip.ferriswheel.common.variant.Parameter;

import java.util.Map;
import java.util.Set;

/**
 * Compare to {@link DataQuery}, a query template is not a final query but
 * a mechanism provides flexible way of generating final query.
 *
 * @see DataQuery
 * @see DataProvider
 */
public interface QueryTemplate {
    /**
     * Get query scheme.
     *
     * @return
     */
    String getScheme();

    /**
     * Get builtin parameter.
     *
     * @param name
     * @return
     */
    Parameter getBuiltinParam(String name);

    /**
     * Get builtin parameter names.
     *
     * @return
     */
    Set<String> getBuiltinParamNames();

    /**
     * Get all builtin parameter names.
     *
     * @return
     */
    Map<String, Parameter> getAllBuiltinParams();
}
