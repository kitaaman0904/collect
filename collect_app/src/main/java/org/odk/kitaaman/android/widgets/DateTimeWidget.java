/*
 * Copyright (C) 2009 University of Washington
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
import android.widget.LinearLayout;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.joda.time.LocalDateTime;
import org.odk.kitaaman.android.formentry.questions.QuestionDetails;
import org.odk.kitaaman.android.listeners.WidgetValueChangedListener;
import org.odk.kitaaman.android.widgets.interfaces.BinaryWidget;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class DateTimeWidget extends QuestionWidget implements BinaryWidget, WidgetValueChangedListener {

    DateWidget dateWidget;
    TimeWidget timeWidget;

    public DateTimeWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);

        dateWidget = new DateWidget(context, prompt, true);
        timeWidget = new TimeWidget(context, prompt, true);

        dateWidget.getAudioVideoImageTextLabel().getLabelTextView().setVisibility(GONE);
        dateWidget.getHelpTextLayout().setVisibility(GONE);

        timeWidget.getAudioVideoImageTextLabel().getLabelTextView().setVisibility(GONE);
        timeWidget.getHelpTextLayout().setVisibility(GONE);

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(dateWidget);
        if (!dateWidget.isDayHidden()) {
            linearLayout.addView(timeWidget);
        }
        addAnswerView(linearLayout);

        timeWidget.setValueChangedListener(this);
        dateWidget.setValueChangedListener(this);
    }

    @Override
    public IAnswerData getAnswer() {
        if (isNullAnswer()) {
            return null;
        } else {
            if (timeWidget.isNullAnswer()) {
                timeWidget.setTimeToCurrent();
                timeWidget.setTimeLabel();
            } else if (dateWidget.isNullAnswer()) {
                dateWidget.setDateToCurrent();
                dateWidget.setDateLabel();
            }

            int year = dateWidget.getDate().getYear();
            int month = dateWidget.getDate().getMonthOfYear();
            int day = dateWidget.getDate().getDayOfMonth();
            int hour = timeWidget.getHour();
            int minute = timeWidget.getMinute();

            LocalDateTime ldt = new LocalDateTime()
                    .withYear(year)
                    .withMonthOfYear(month)
                    .withDayOfMonth(day)
                    .withHourOfDay(hour)
                    .withMinuteOfHour(minute)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0);

            return new DateTimeData(ldt.toDate());
        }
    }

    @Override
    public void clearAnswer() {
        dateWidget.clearAnswerWithoutValueChangeEvent();
        timeWidget.clearAnswerWithoutValueChangeEvent();

        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        dateWidget.setOnLongClickListener(l);
        timeWidget.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        dateWidget.cancelLongPress();
        timeWidget.cancelLongPress();
    }

    @Override
    public void setBinaryData(Object answer) {
        dateWidget.setBinaryData(answer);
    }

    public DateWidget getDateWidget() {
        return dateWidget;
    }

    // Exposed for testing purposes to avoid reflection.
    public void setDateWidget(DateWidget dateWidget) {
        this.dateWidget = dateWidget;
    }

    // Exposed for testing purposes to avoid reflection.
    public void setTimeWidget(TimeWidget timeWidget) {
        this.timeWidget = timeWidget;
    }

    @Override
    public void onButtonClick(int buttonId) {
    }

    private boolean isNullAnswer() {
        return getFormEntryPrompt().isRequired()
                ? dateWidget.isNullAnswer() || timeWidget.isNullAnswer()
                : dateWidget.isNullAnswer() && timeWidget.isNullAnswer();
    }

    @Override
    public void widgetValueChanged(QuestionWidget changedWidget) {
        widgetValueChanged();
    }
}
