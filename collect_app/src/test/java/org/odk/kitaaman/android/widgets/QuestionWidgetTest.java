package org.odk.kitaaman.android.widgets;

import android.app.Application;
import android.content.Context;

import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.audio.AudioButton;
import org.odk.kitaaman.android.audio.AudioHelper;
import org.odk.kitaaman.android.audio.Clip;
import org.odk.kitaaman.android.formentry.media.AudioHelperFactory;
import org.odk.kitaaman.android.formentry.questions.QuestionDetails;
import org.odk.kitaaman.android.injection.config.AppDependencyModule;
import org.odk.kitaaman.android.preferences.GeneralSharedPreferences;
import org.odk.kitaaman.android.support.MockFormEntryPromptBuilder;
import org.odk.kitaaman.android.support.RobolectricHelpers;
import org.odk.kitaaman.android.support.TestScreenContextActivity;
import org.robolectric.RobolectricTestRunner;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.kitaaman.android.support.CollectHelpers.setupFakeReferenceManager;

@RunWith(RobolectricTestRunner.class)
public class QuestionWidgetTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public AudioHelper audioHelper;

    @Mock
    public Analytics analytics;

    @Before
    public void setup() throws Exception {
        overrideDependencyModule();
        when(audioHelper.setAudio(any(AudioButton.class), any())).thenReturn(new MutableLiveData<>());
    }

    @Test
    public void whenQuestionHasAudio_audioButtonUsesIndexAsClipID() throws Exception {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withAudioURI("ref")
                .build();

        TestScreenContextActivity activity = RobolectricHelpers.createThemedActivity(TestScreenContextActivity.class);
        TestWidget widget = new TestWidget(activity, new QuestionDetails(prompt, "formAnalyticsID"));

        AudioButton audioButton = widget.getAudioVideoImageTextLabel().findViewById(R.id.audioButton);
        verify(audioHelper).setAudio(audioButton, new Clip("i am index", "blah.mp3"));
    }

    @Test
    public void whenQuestionHasAudio_logsAudioLabelEvent() throws Exception {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withAudioURI("ref")
                .build();

        TestScreenContextActivity activity = RobolectricHelpers.createThemedActivity(TestScreenContextActivity.class);
        new TestWidget(activity, new QuestionDetails(prompt, "formAnalyticsID"));

        verify(analytics).logEvent("Prompt", "AudioLabel", "formAnalyticsID");
    }

    private void overrideDependencyModule() throws Exception {
        ReferenceManager referenceManager = setupFakeReferenceManager(asList(new Pair<>("ref", "blah.mp3")));
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }

            @Override
            public AudioHelperFactory providesAudioHelperFactory() {
                return context -> audioHelper;
            }

            @Override
            public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
                return analytics;
            }
        });
    }

    private static class TestWidget extends QuestionWidget {

        TestWidget(Context context, QuestionDetails questionDetails) {
            super(context, questionDetails);
        }

        @Override
        public IAnswerData getAnswer() {
            return null;
        }

        @Override
        public void clearAnswer() {

        }

        @Override
        public void setOnLongClickListener(OnLongClickListener l) {

        }
    }
}
