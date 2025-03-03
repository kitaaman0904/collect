package org.odk.kitaaman.android.widgets.base;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.kitaaman.android.widgets.interfaces.FileWidget;

import java.io.File;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class FileWidgetTest<W extends FileWidget> extends BinaryWidgetTest<W, StringData> {

    @Mock
    public File instancePath;

    @NonNull
    @Override
    public StringData getInitialAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        when(formController.getInstanceFile()).thenReturn(instancePath);
        when(instancePath.getParent()).thenReturn("");
    }

    @Test
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        prepareForSetAnswer();

        super.settingANewAnswerShouldRemoveTheOldAnswer();

        W widget = getSpyWidget();
        verify(widget).deleteFile();
    }

    @Test
    public void callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        super.callingClearShouldRemoveTheExistingAnswer();

        W widget = getSpyWidget();
        verify(widget).deleteFile();
    }

    @Override
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        prepareForSetAnswer();
        super.getAnswerShouldReturnCorrectAnswerAfterBeingSet();
    }

    @Override
    public void settingANewAnswerShouldRemoveTheOldAnswer() {
        prepareForSetAnswer();
        super.settingANewAnswerShouldRemoveTheOldAnswer();
    }

    /**
     * Override this to provide additional set-up prior to testing any set answer methods.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected void prepareForSetAnswer() {
        // Default implementation does nothing.
    }
}