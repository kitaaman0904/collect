/*
 * Copyright (C) 2013 Nafundi LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.kitaaman.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.formentry.questions.QuestionDetails;
import org.odk.kitaaman.android.formentry.questions.WidgetViewUtils;
import org.odk.kitaaman.android.utilities.CustomTabHelper;
import org.odk.kitaaman.android.widgets.interfaces.ButtonWidget;

import static org.odk.kitaaman.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.odk.kitaaman.android.formentry.questions.WidgetViewUtils.getCenteredAnswerTextView;

/**
 * Widget that allows user to open URLs from within the form
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class UrlWidget extends QuestionWidget implements ButtonWidget {

    private Uri uri;
    final Button openUrlButton;
    final TextView stringAnswer;
    private final CustomTabHelper customTabHelper;

    public UrlWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);

        openUrlButton = createSimpleButton(getContext(), getFormEntryPrompt().isReadOnly(), context.getString(R.string.open_url), getAnswerFontSize(), this);

        stringAnswer = getCenteredAnswerTextView(getContext(), getAnswerFontSize());

        String s = questionDetails.getPrompt().getAnswerText();
        if (s != null) {
            stringAnswer.setText(s);
            uri = Uri.parse(stringAnswer.getText().toString());
        }

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(openUrlButton);
        answerLayout.addView(stringAnswer);
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));

        customTabHelper = new CustomTabHelper();
    }

    private boolean isUrlEmpty(TextView stringAnswer) {
        return stringAnswer == null || stringAnswer.getText() == null
                || stringAnswer.getText().toString().isEmpty();
    }

    @Override
    public void clearAnswer() {
        Toast.makeText(getContext(), "URL is readonly", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = stringAnswer.getText().toString();
        return !s.isEmpty()
                ? new StringData(s)
                : null;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        openUrlButton.cancelLongPress();
        stringAnswer.cancelLongPress();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (customTabHelper.getServiceConnection() != null) {
            getContext().unbindService(customTabHelper.getServiceConnection());
        }
    }

    @Override
    public void onButtonClick(int buttonId) {
        if (!isUrlEmpty(stringAnswer)) {
            customTabHelper.bindCustomTabsService(getContext(), null);
            customTabHelper.openUri(getContext(), uri);
        } else {
            Toast.makeText(getContext(), "No URL set", Toast.LENGTH_SHORT).show();
        }
    }
}
