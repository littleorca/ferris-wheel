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

public interface AssetManager {

    /**
     * Acquire for a new asset ID.
     *
     * @return
     */
    long nextAssetId();

    /**
     * Attach the specified asset.
     *
     * @param asset
     */
    void attach(Asset asset);

    /**
     * Get attached asset by ID.
     *
     * @param id
     * @return
     */
    Asset get(long id);

    ReferenceMaintainer getReferenceMaintainer();

    /**
     * Determine whether the asset with the specified ID is attached or not.
     *
     * @param id
     * @return
     */
    boolean exists(long id);

    /**
     * Detach the specified asset.
     *
     * @param asset
     */
    void detach(Asset asset);

    /**
     * Get transaction manager.
     *
     * @return
     */
    TransactionManager getTransactionManager();
}
