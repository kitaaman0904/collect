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

package org.odk.kitaaman.android.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;

import org.odk.kitaaman.android.formentry.questions.QuestionDetails;
import org.odk.kitaaman.android.formentry.questions.WidgetViewUtils;

import static org.odk.kitaaman.android.utilities.ViewUtils.pxFromDp;

/**
 * A base widget class which is responsible for sharing the code used by simple select widgets like
 * {@link AbstractSelectOneWidget} and {@link SelectMultiWidget}.
 */
public abstract class SelectTextWidget extends SelectWidget {
    private static final String SEARCH_TEXT = "search_text";

    protected EditText searchStr;

    public SelectTextWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
    }

    @Override
    protected void saveState() {
        super.saveState();
        if (searchStr != null) {
            getState().putString(SEARCH_TEXT + getFormEntryPrompt().getIndex(), searchStr.getText().toString());
        }
    }

    protected void setUpSearchBox(Context context) {
        searchStr = new EditText(getContext());
        searchStr.setId(View.generateViewId());
        searchStr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(pxFromDp(context, WidgetViewUtils.getStandardMargin(context)), 5,
                pxFromDp(context, WidgetViewUtils.getStandardMargin(context)), 5);
        searchStr.setLayoutParams(params);
        setupChangeListener();
        answerLayout.addView(searchStr, 0);

        String searchText = null;
        if (getState() != null) {
            searchText = getState().getString(SEARCH_TEXT + getFormEntryPrompt().getIndex());
        }
        if (searchText != null && !searchText.isEmpty()) {
            searchStr.setText(searchText);
            Selection.setSelection(searchStr.getText(), searchStr.getText().toString().length());
        } else {
            doSearch("");
        }
    }

    private void setupChangeListener() {
        searchStr.addTextChangedListener(new TextWatcher() {
            private String oldText = "";

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(oldText)) {
                    doSearch(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected void doSearch(String searchStr) {
    }
}