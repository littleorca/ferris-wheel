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

import com.ctrip.ferriswheel.core.dom.Amendment;

public abstract class AmendmentImpl implements Amendment {
    private final String pathname;

    private AmendmentImpl(String pathname) {
        this.pathname = pathname;
    }

    @Override
    public String getPathname() {
        return pathname;
    }

    public static final class AddImpl extends AmendmentImpl implements Amendment.Add {
        public AddImpl(String pathname) {
            super(pathname);
        }
    }

    public static final class DelImpl extends AmendmentImpl implements Del {
        public DelImpl(String pathname) {
            super(pathname);
        }
    }

    public static final class RenameImpl extends AmendmentImpl implements Rename {
        private final String newPathname;

        public RenameImpl(String pathname, String newPathname) {
            super(pathname);
            this.newPathname = newPathname;
        }

        @Override
        public String getNewPathname() {
            return newPathname;
        }
    }

    public static final class PutAttrImpl extends AmendmentImpl implements PutAttr {
        private final String attrName;
        private final String attrValue;

        public PutAttrImpl(String pathname, String attrName, String attrValue) {
            super(pathname);
            this.attrName = attrName;
            this.attrValue = attrValue;
        }

        @Override
        public String getAttrName() {
            return attrName;
        }

        @Override
        public String getAttrValue() {
            return attrValue;
        }
    }

    public static final class DelAttrImpl extends AmendmentImpl implements DelAttr {
        private final String attrName;

        public DelAttrImpl(String pathname, String attrName) {
            super(pathname);
            this.attrName = attrName;
        }

        @Override
        public String getAttrName() {
            return attrName;
        }

    }

}
