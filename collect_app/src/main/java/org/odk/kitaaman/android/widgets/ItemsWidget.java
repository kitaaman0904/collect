/*
 * Copyright 2019 Nafundi
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

import org.javarosa.core.model.SelectChoice;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.external.ExternalDataUtil;
import org.odk.kitaaman.android.formentry.questions.QuestionDetails;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemsWidget is an abstract class used by widgets containing a list of choices.
 * Those choices might be read from a form (xml file) or an external csv file and used in questions
 * like: SelectOne, SelectMultiple, Ranking.
 */
public abstract class ItemsWidget extends QuestionWidget {

    List<SelectChoice> items = new ArrayList<>();

    public ItemsWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
        readItems();
    }

    protected void readItems() {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(getFormEntryPrompt().getAppearanceHint());
        if (xpathFuncExpr != null) {
            try {
                items = ExternalDataUtil.populateExternalChoices(getFormEntryPrompt(), xpathFuncExpr);
            } catch (FileNotFoundException e) {
                showWarning(getContext().getString(R.string.file_missing, e.getMessage()));
            }
        } else {
            items = getFormEntryPrompt().getSelectChoices();
        }
    }
}
