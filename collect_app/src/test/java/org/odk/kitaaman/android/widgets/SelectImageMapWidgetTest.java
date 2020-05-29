package org.odk.kitaaman.android.widgets;

import android.view.MotionEvent;
import android.view.View;

import androidx.core.util.Pair;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.kitaaman.android.injection.config.AppDependencyModule;
import org.odk.kitaaman.android.support.MockFormEntryPromptBuilder;
import org.odk.kitaaman.android.support.RobolectricHelpers;
import org.odk.kitaaman.android.widgets.base.SelectWidgetTest;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.kitaaman.android.support.CollectHelpers.setupFakeReferenceManager;

public abstract class SelectImageMapWidgetTest<W extends SelectImageMapWidget, A extends IAnswerData>
        extends SelectWidgetTest<W, A> {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        overrideDependencyModule();
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withImageURI("jr://images/body.svg")
                .build();
    }

    private void overrideDependencyModule() throws Exception {
        ReferenceManager referenceManager = setupFakeReferenceManager(asList(
                new Pair<>("jr://images/body.svg", "body.svg")
        ));

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    @Override
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        formEntryPrompt = new MockFormEntryPromptBuilder(formEntryPrompt)
                .withReadOnly(true)
                .build();
        MotionEvent motionEvent = mock(MotionEvent.class);
        when(motionEvent.getAction()).thenReturn(MotionEvent.ACTION_DOWN);

        assertThat(getSpyWidget().webView.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().webView.isClickable(), is(Boolean.FALSE));
    }
}
