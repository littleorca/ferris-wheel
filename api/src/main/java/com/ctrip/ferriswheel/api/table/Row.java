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

package com.ctrip.ferriswheel.api.table;

import java.util.Map;

/**
 * Holds cells and provides methods for getting them.
 * To manipulate a row, please refer to {@link Table}.
 *
 * @see Table
 * @see Cell
 */
public interface Row extends Iterable<Map.Entry<Integer, Cell>> {
    /**
     * Get cell count. Uninitialized cells before the last initialized cell will be count in.
     * In another word, cell count equals to last initialized cell index plus one.
     *
     * @return
     */
    int getCellCount();

    /**
     * Get cell by index.
     *
     * @param index
     * @return
     */
    Cell getCell(int index);

    /**
     * Whether it is a blank row.
     *
     * @return
     */
    boolean isBlank();
}
