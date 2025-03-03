/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.kitaaman.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import org.odk.kitaaman.android.utilities.QuestionFontSizeUtils;

public class CustomNumberPicker extends NumberPicker {

    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View view) {
        super.addView(view);
        updateView(view);
    }

    @Override
    public void addView(View view, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(view, index, params);
        updateView(view);
    }

    @Override
    public void addView(View view, android.view.ViewGroup.LayoutParams params) {
        super.addView(view, params);
        updateView(view);
    }

    private void updateView(View view) {
        if (view instanceof EditText) {
            ((EditText) view).setTextSize(QuestionFontSizeUtils.getQuestionFontSize());
        }
    }
}
