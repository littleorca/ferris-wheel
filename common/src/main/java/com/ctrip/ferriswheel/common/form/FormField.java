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

package com.ctrip.ferriswheel.common.form;

import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;

/**
 * Describe a form field.
 */
public interface FormField {
    /**
     * Get field name. A field name should be unique among it's siblings in the
     * same form.
     *
     * @return
     */
    String getName();

    /**
     * Get field value type.
     *
     * @return
     */
    VariantType getType();

    /**
     * Get the bound value of the field.
     *
     * @return
     */
    Variant getValue();

    /**
     * Is mandatory filed (must be filled with valid value).
     *
     * @return
     */
    boolean isMandatory();

    /**
     * Is multiple values field.
     *
     * @return
     */
    boolean isMultiple();

    /**
     * Get label for display.
     *
     * @return
     */
    String getLabel();

    /**
     * Get tips information.
     *
     * @return
     */
    String getTips();

    /**
     * Get option values.
     *
     * @return
     */
    Variant getOptions();

    /**
     * Get binding count. A form field can bind to multiple target, when the
     * form is submitted, all bound targets will be updated.
     *
     * @return
     */
    int getBindingCount();

    /**
     * Get binding by index.
     *
     * @param index
     * @return
     */
    FormFieldBinding getBinding(int index);

}
