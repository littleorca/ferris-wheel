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

package com.ctrip.ferriswheel.api;

import com.ctrip.ferriswheel.api.action.ActionListener;

public interface Workbook extends Iterable<Sheet> {
    Version getVersion();

    String getName();

    void setName(String name);

    int getSheetCount();

    Sheet getSheet(int index);

    Sheet getSheet(String name);

    Sheet addSheet(String name);

    Sheet addSheet(int index, String name);

    void renameSheet(String oldName, String newName);

    void moveSheet(String name, int index);

    Sheet removeSheet(int index);

    Sheet removeSheet(String name);

    void refresh();

    void refresh(boolean force);

    void addListener(ActionListener listener);

    boolean removeListener(ActionListener listener);
}
