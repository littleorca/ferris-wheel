/*
 * MIT License
 *
 * Copyright (c) 2018-2020 Ctrip.com
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

package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.TreeRevision;

import java.util.List;

public interface TreeRevisionHistory {

    /**
     * Move to the prior(older) revision, or null if not exists.
     *
     * @return prior(older) revision, or null if not exists.
     * @see #forward()
     * @see #go(int)
     */
    TreeRevision backward();

    /**
     * Move to the next(newer) revision, or null if not exists.
     *
     * @return next(newer) revision, or null if not exists.
     * @see #backward()
     * @see #go(int)
     */
    TreeRevision forward();

    /**
     * Move to the <code>n</code>-th prior or next revision. If <code>n</code>
     * is positive, the <code>n</code>-th newer revision should be returned
     * if it exists, like {@link #forward()} <code>n</code> times; If
     * <code>n</code> is negative, the <code>n</code>-th older revision should
     * be returned if it exists, like {@link #backward()} <code>n</code> times;
     * And if <code>n</code> is zero, the current revision should be returned
     * if it exists. In any way, if the expected revision does not exists,
     * <code>null</code> should be returned.
     *
     * @param n step count, positive value for move forward, negative value for
     *          move backward, zero means no move at all.
     * @return the <code>n</code>-th revision or null if it doesn't exist.
     * @see #backward()
     * @see #forward()
     */
    TreeRevision go(int n);

    /**
     * List the backward revisions, sorted from newer to older.
     *
     * @return Backward revision list, from newer to older.
     */
    List<TreeRevision> backwardList();

    /**
     * List the forward revisions, sorted from older to newer.
     *
     * @return Forward revision list, from older to newer.
     */
    List<TreeRevision> forwardList();

    /**
     * Push new revision right after the current revision. This erases forward
     * revisions and append the specified revision right after the current
     * revision, and the <em>current</em> revision should point to the newly
     * pushed revision at last.
     *
     * @param revision new revision to push
     */
    void push(TreeRevision revision);

}
