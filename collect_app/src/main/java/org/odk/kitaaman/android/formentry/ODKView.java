/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.kitaaman.android.formentry;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.application.Collect;
import org.odk.kitaaman.android.audio.AudioHelper;
import org.odk.kitaaman.android.audio.PlaybackFailedException;
import org.odk.kitaaman.android.exception.ExternalParamsException;
import org.odk.kitaaman.android.exception.JavaRosaException;
import org.odk.kitaaman.android.external.ExternalAppsUtils;
import org.odk.kitaaman.android.javarosawrapper.FormController;
import org.odk.kitaaman.android.formentry.media.AudioHelperFactory;
import org.odk.kitaaman.android.formentry.media.PromptAutoplayer;
import org.odk.kitaaman.android.formentry.questions.QuestionTextSizeHelper;
import org.odk.kitaaman.android.listeners.WidgetValueChangedListener;
import org.odk.kitaaman.android.utilities.QuestionFontSizeUtils;
import org.odk.kitaaman.android.utilities.ScreenContext;
import org.odk.kitaaman.android.utilities.ThemeUtils;
import org.odk.kitaaman.android.utilities.ToastUtils;
import org.odk.kitaaman.android.widgets.QuestionWidget;
import org.odk.kitaaman.android.widgets.StringWidget;
import org.odk.kitaaman.android.widgets.WidgetFactory;
import org.odk.kitaaman.android.widgets.interfaces.BinaryWidget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.kitaaman.android.injection.DaggerUtils.getComponent;
import static org.odk.kitaaman.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Contains either one {@link QuestionWidget} if the current form element is a question or
 * multiple {@link QuestionWidget}s if the current form element is a group with the
 * {@code field-list} appearance.
 */
@SuppressLint("ViewConstructor")
public class ODKView extends FrameLayout implements OnLongClickListener, WidgetValueChangedListener {

    private final LinearLayout widgetsList;
    private final LinearLayout.LayoutParams layout;
    private final ArrayList<QuestionWidget> widgets;
    private final AudioHelper audioHelper;

    public static final String FIELD_LIST = "field-list";

    private WidgetValueChangedListener widgetValueChangedListener;

    @Inject
    public AudioHelperFactory audioHelperFactory;

    @Inject
    public Analytics analytics;

    /**
     * Builds the view for a specified question or field-list of questions.
     *
     * @param context         the activity creating this view
     * @param questionPrompts the questions to be included in this view
     * @param groups          the group hierarchy that this question or field list is in
     * @param advancingPage   whether this view is being created after a forward swipe through the
     *                        form. Used to determine whether to autoplay media.
     */
    public ODKView(Context context, final FormEntryPrompt[] questionPrompts,
                   FormEntryCaption[] groups, boolean advancingPage) {
        super(context);

        getComponent(context).inject(this);
        this.audioHelper = audioHelperFactory.create(context);

        inflate(getContext(), R.layout.odk_view, this); // keep in an xml file to enable the vertical scrollbar

        widgets = new ArrayList<>();
        widgetsList = findViewById(R.id.widgets);

        layout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        // display which group you are in as well as the question
        setGroupText(groups);

        // when the grouped fields are populated by an external app, this will get true.
        boolean readOnlyOverride = false;

        // handle intent groups that are intended to receive multiple values from an external app
        if (groups != null && groups.length > 0) {
            // get the group we are showing -- it will be the last of the groups in the groups list
            final FormEntryCaption c = groups[groups.length - 1];
            final String intentString = c.getFormElement().getAdditionalAttribute(null, "intent");
            if (intentString != null && intentString.length() != 0) {
                readOnlyOverride = true;

                addIntentLaunchButton(context, questionPrompts, c, intentString);
            }
        }

        for (FormEntryPrompt question : questionPrompts) {
            addWidgetForQuestion(question, readOnlyOverride);
        }

        setupAudioErrors();
        autoplayIfNeeded(advancingPage);
    }

    private void setupAudioErrors() {
        audioHelper.getError().observe(getScreenContext().getViewLifecycle(), e -> {
            if (e instanceof PlaybackFailedException) {
                final PlaybackFailedException playbackFailedException = (PlaybackFailedException) e;
                Toast.makeText(
                        getContext(),
                        getContext().getString(playbackFailedException.getExceptionMsg(), playbackFailedException.getURI()),
                        Toast.LENGTH_SHORT
                ).show();

                audioHelper.errorDisplayed();
            }
        });
    }

    private void autoplayIfNeeded(boolean advancingPage) {

        // see if there is an autoplay option.
        // Only execute it during forward swipes through the form
        if (advancingPage && widgets.size() == 1) {
            FormEntryPrompt firstPrompt = widgets.get(0).getFormEntryPrompt();
            Boolean autoplayedAudio = autoplayAudio(firstPrompt);

            if (!autoplayedAudio) {
                autoplayVideo(firstPrompt);
            }

        }
    }

    private Boolean autoplayAudio(FormEntryPrompt firstPrompt) {
        PromptAutoplayer promptAutoplayer = new PromptAutoplayer(
                audioHelper,
                ReferenceManager.instance(),
                analytics,
                Collect.getCurrentFormIdentifierHash()
        );

        return promptAutoplayer.autoplayIfNeeded(firstPrompt);
    }

    private void autoplayVideo(FormEntryPrompt prompt) {
        final String autoplayOption = prompt.getFormElement().getAdditionalAttribute(null, "autoplay");

        if (autoplayOption != null) {
            if (autoplayOption.equalsIgnoreCase("video")) {
                new Handler().postDelayed(() -> {
                    widgets.get(0).getAudioVideoImageTextLabel().playVideo();
                }, 150);
            }
        }
    }

    private ScreenContext getScreenContext() {
        return (ScreenContext) getContext();
    }

    /**
     * Creates a {@link QuestionWidget} for the given {@link FormEntryPrompt}, sets its listeners,
     * and adds it to the end of the view. If this widget is not the first one, add a divider above
     * it.
     */
    private void addWidgetForQuestion(FormEntryPrompt question, boolean readOnlyOverride) {
        QuestionWidget qw = configureWidgetForQuestion(question, readOnlyOverride);

        widgets.add(qw);

        if (widgets.size() > 1) {
            widgetsList.addView(getDividerView());
        }
        widgetsList.addView(qw, layout);
    }

    /**
     * Creates a {@link QuestionWidget} for the given {@link FormEntryPrompt}, sets its listeners,
     * and adds it to the view at the specified {@code index}. If this widget is not the first one,
     * add a divider above it. If the specified {@code index} is beyond the end of the widget list,
     * add it to the end.
     */
    public void addWidgetForQuestion(FormEntryPrompt question, boolean readOnlyOverride, int index) {
        if (index > widgets.size() - 1) {
            addWidgetForQuestion(question, readOnlyOverride);
            return;
        }

        QuestionWidget qw = configureWidgetForQuestion(question, readOnlyOverride);

        widgets.add(index, qw);

        int indexAccountingForDividers = index * 2;
        if (index > 0) {
            widgetsList.addView(getDividerView(), indexAccountingForDividers - 1);
        }

        widgetsList.addView(qw, indexAccountingForDividers, layout);
    }

    /**
     * Creates and configures a {@link QuestionWidget} for the given {@link FormEntryPrompt}.
     * <p>
     * Note: if the given question is of an unsupported type, a text widget will be created.
     */
    private QuestionWidget configureWidgetForQuestion(FormEntryPrompt question, boolean readOnlyOverride) {
        QuestionWidget qw = WidgetFactory.createWidgetFromPrompt(question, getContext(), readOnlyOverride);
        qw.setOnLongClickListener(this);
        qw.setValueChangedListener(this);

        return qw;
    }

    private View getDividerView() {
        View divider = new View(getContext());
        divider.setBackgroundResource(new ThemeUtils(getContext()).getDivider());
        divider.setMinimumHeight(3);

        return divider;
    }

    public Bundle getState() {
        Bundle state = new Bundle();
        for (QuestionWidget qw : getWidgets()) {
            state.putAll(qw.getCurrentState());
        }

        return state;
    }

    /**
     * Addresses 'bitmap size exceeds VM budget' crash.
     * http://code.google.com/p/android/issues/detail?id=8488
     */
    public void recycleDrawables() {
        this.destroyDrawingCache();
        widgetsList.destroyDrawingCache();
        for (QuestionWidget q : widgets) {
            q.recycleDrawables();
        }
    }

    /**
     * @return a HashMap of answers entered by the user for this set of widgets
     */
    public HashMap<FormIndex, IAnswerData> getAnswers() {
        HashMap<FormIndex, IAnswerData> answers = new LinkedHashMap<>();
        for (QuestionWidget q : widgets) {
            /*
             * The FormEntryPrompt has the FormIndex, which is where the answer gets stored. The
             * QuestionWidget has the answer the user has entered.
             */
            FormEntryPrompt p = q.getFormEntryPrompt();
            answers.put(p.getIndex(), q.getAnswer());
        }

        return answers;
    }

    /**
     * Add a TextView containing the hierarchy of groups to which the question belongs.
     */
    private void setGroupText(FormEntryCaption[] groups) {
        String path = getGroupsPath(groups);

        if (!path.isEmpty()) {
            TextView tv = findViewById(R.id.group_text);
            tv.setText(path);

            QuestionTextSizeHelper textSizeHelper = new QuestionTextSizeHelper();
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeHelper.getSubtitle1());

            tv.setVisibility(VISIBLE);
        }
    }

    /**
     * @see #getGroupsPath(FormEntryCaption[], boolean)
     */
    @NonNull
    public static String getGroupsPath(FormEntryCaption[] groups) {
        return getGroupsPath(groups, false);
    }

    /**
     * Builds a string representing the 'path' of the list of groups.
     * Each level is separated by `>`.
     * <p>
     * Some views (e.g. the repeat picker) may want to hide the multiplicity of the last item,
     * i.e. show `Friends` instead of `Friends > 1`.
     */
    @NonNull
    public static String getGroupsPath(FormEntryCaption[] groups, boolean hideLastMultiplicity) {
        if (groups == null) {
            return "";
        }

        List<String> segments = new ArrayList<>();
        int index = 1;
        for (FormEntryCaption group : groups) {
            String text = group.getLongText();

            if (text != null) {
                segments.add(text);

                boolean isMultiplicityAllowed = !(hideLastMultiplicity && index == groups.length);
                if (group.repeats() && isMultiplicityAllowed) {
                    segments.add(Integer.toString(group.getMultiplicity() + 1));
                }
            }

            index++;
        }

        return TextUtils.join(" > ", segments);
    }

    /**
     * Adds a button to launch an intent if the group displayed by this view is an intent group.
     * An intent group launches an intent and receives multiple values from the launched app.
     */
    private void addIntentLaunchButton(Context context, FormEntryPrompt[] questionPrompts,
                                       FormEntryCaption c, String intentString) {
        final String buttonText;
        final String errorString;
        String v = c.getSpecialFormQuestionText("buttonText");
        buttonText = (v != null) ? v : context.getString(R.string.launch_app);
        v = c.getSpecialFormQuestionText("noAppErrorString");
        errorString = (v != null) ? v : context.getString(R.string.no_app);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        // set button formatting
        Button launchIntentButton = new Button(getContext());
        launchIntentButton.setId(View.generateViewId());
        launchIntentButton.setText(buttonText);
        launchIntentButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                QuestionFontSizeUtils.getQuestionFontSize() + 2);
        launchIntentButton.setPadding(20, 20, 20, 20);
        launchIntentButton.setLayoutParams(params);

        launchIntentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String intentName = ExternalAppsUtils.extractIntentName(intentString);
                Map<String, String> parameters = ExternalAppsUtils.extractParameters(
                        intentString);

                Intent i = new Intent(intentName);
                try {
                    ExternalAppsUtils.populateParameters(i, parameters,
                            c.getIndex().getReference());

                    for (FormEntryPrompt p : questionPrompts) {
                        IFormElement formElement = p.getFormElement();
                        if (formElement instanceof QuestionDef) {
                            TreeReference reference =
                                    (TreeReference) formElement.getBind().getReference();
                            IAnswerData answerValue = p.getAnswerValue();
                            Object value =
                                    answerValue == null ? null : answerValue.getValue();
                            switch (p.getDataType()) {
                                case Constants.DATATYPE_TEXT:
                                case Constants.DATATYPE_INTEGER:
                                case Constants.DATATYPE_DECIMAL:
                                    i.putExtra(reference.getNameLast(),
                                            (Serializable) value);
                                    break;
                            }
                        }
                    }

                    ((Activity) getContext()).startActivityForResult(i, RequestCodes.EX_GROUP_CAPTURE);
                } catch (ExternalParamsException e) {
                    Timber.e(e, "ExternalParamsException");

                    ToastUtils.showShortToast(e.getMessage());
                } catch (ActivityNotFoundException e) {
                    Timber.d(e, "ActivityNotFoundExcept");

                    ToastUtils.showShortToast(errorString);
                }
            }
        });

        widgetsList.addView(getDividerView());
        widgetsList.addView(launchIntentButton, layout);
    }

    public void setFocus(Context context) {
        if (!widgets.isEmpty()) {
            widgets.get(0).setFocus(context);
        }
    }

    /**
     * Returns true if any part of the question widget is currently on the screen or false otherwise.
     */
    public boolean isDisplayed(QuestionWidget qw) {
        Rect scrollBounds = new Rect();
        findViewById(R.id.odk_view_container).getHitRect(scrollBounds);
        return qw.getLocalVisibleRect(scrollBounds);
    }

    public void scrollTo(@Nullable QuestionWidget qw) {
        if (qw != null && widgets.contains(qw)) {
            findViewById(R.id.odk_view_container).scrollTo(0, qw.getTop());
        }
    }

    /**
     * Called when another activity returns information to answer this question.
     */
    public void setBinaryData(Object answer) {
        boolean set = false;
        for (QuestionWidget q : widgets) {
            if (q instanceof BinaryWidget) {
                BinaryWidget binaryWidget = (BinaryWidget) q;
                if (binaryWidget.isWaitingForData()) {
                    try {
                        binaryWidget.setBinaryData(answer);
                        binaryWidget.cancelWaitingForData();
                    } catch (Exception e) {
                        Timber.e(e);
                        ToastUtils.showLongToast(getContext().getString(R.string.error_attaching_binary_file,
                                e.getMessage()));
                    }
                    set = true;
                    break;
                }
            }
        }

        if (!set) {
            Timber.w("Attempting to return data to a widget or set of widgets not looking for data");
        }
    }

    /**
     * Saves answers for the widgets in this view. Called when the widgets are in an intent group.
     */
    public void setDataForFields(Bundle bundle) throws JavaRosaException {
        if (bundle == null) {
            return;
        }
        FormController formController = Collect.getInstance().getFormController();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            for (QuestionWidget questionWidget : widgets) {
                FormEntryPrompt prompt = questionWidget.getFormEntryPrompt();
                TreeReference treeReference =
                        (TreeReference) prompt.getFormElement().getBind().getReference();

                if (treeReference.getNameLast().equals(key)) {
                    switch (prompt.getDataType()) {
                        case Constants.DATATYPE_TEXT:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asStringData(bundle.get(key)));
                            break;
                        case Constants.DATATYPE_INTEGER:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asIntegerData(bundle.get(key)));
                            break;
                        case Constants.DATATYPE_DECIMAL:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asDecimalData(bundle.get(key)));
                            break;
                        default:
                            throw new RuntimeException(
                                    getContext().getString(R.string.ext_assign_value_error,
                                            treeReference.toString(false)));
                    }

                    ((StringWidget) questionWidget).setDisplayValueFromModel();
                    break;
                }
            }
        }
    }

    public void cancelWaitingForBinaryData() {
        int count = 0;
        for (QuestionWidget q : widgets) {
            if (q instanceof BinaryWidget) {
                if (q.isWaitingForData()) {
                    q.cancelWaitingForData();
                    ++count;
                }
            }
        }

        if (count != 1) {
            Timber.w("Attempting to cancel waiting for binary data to a widget or set of widgets "
                    + "not looking for data");
        }
    }

    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX,
                                        float velocityY) {
        for (QuestionWidget q : widgets) {
            if (q.suppressFlingGesture(e1, e2, velocityX, velocityY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the answer was cleared, false otherwise.
     */
    public boolean clearAnswer() {
        // If there's only one widget, clear the answer.
        // If there are more, then force a long-press to clear the answer.
        if (widgets.size() == 1 && !widgets.get(0).getFormEntryPrompt().isReadOnly()) {
            widgets.get(0).clearAnswer();
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<QuestionWidget> getWidgets() {
        return widgets;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        for (int i = 0; i < widgets.size(); i++) {
            QuestionWidget qw = widgets.get(i);
            qw.setOnFocusChangeListener(l);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (QuestionWidget qw : widgets) {
            qw.cancelLongPress();
        }
    }

    /**
     * Highlights the question at the given {@link FormIndex} in red for 2.5 seconds, scrolls the
     * view to display that question at the top and gives it focus.
     */
    public void highlightWidget(FormIndex formIndex) {
        QuestionWidget qw = getQuestionWidget(formIndex);

        if (qw != null) {
            // postDelayed is needed because otherwise scrolling may not work as expected in case when
            // answers are validated during form finalization.
            new Handler().postDelayed(() -> {
                qw.setFocus(getContext());
                scrollTo(qw);

                ValueAnimator va = new ValueAnimator();
                va.setIntValues(getResources().getColor(R.color.red_500), getDrawingCacheBackgroundColor());
                va.setEvaluator(new ArgbEvaluator());
                va.addUpdateListener(valueAnimator -> qw.setBackgroundColor((int) valueAnimator.getAnimatedValue()));
                va.setDuration(2500);
                va.start();
            }, 100);
        }
    }

    private QuestionWidget getQuestionWidget(FormIndex formIndex) {
        for (QuestionWidget qw : widgets) {
            if (formIndex.equals(qw.getFormEntryPrompt().getIndex())) {
                return qw;
            }
        }
        return null;
    }

    /**
     * Removes the widget and corresponding divider at a particular index.
     */
    public void removeWidgetAt(int index) {
        int indexAccountingForDividers = index * 2;

        // There may be a first TextView to display the group path. See addGroupText(FormEntryCaption[])
        if (widgetsList.getChildCount() > 0 && widgetsList.getChildAt(0) instanceof TextView) {
            indexAccountingForDividers += 1;
        }
        widgetsList.removeViewAt(indexAccountingForDividers);

        if (index > 0) {
            widgetsList.removeViewAt(indexAccountingForDividers - 1);
        }

        widgets.remove(index);
    }

    public void setWidgetValueChangedListener(WidgetValueChangedListener listener) {
        widgetValueChangedListener = listener;
    }

    public void widgetValueChanged(QuestionWidget changedWidget) {
        if (widgetValueChangedListener != null) {
            widgetValueChangedListener.widgetValueChanged(changedWidget);
        }
    }
}
