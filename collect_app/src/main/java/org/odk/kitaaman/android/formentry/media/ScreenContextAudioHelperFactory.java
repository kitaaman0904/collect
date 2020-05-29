package org.odk.kitaaman.android.formentry.media;

import android.content.Context;

import org.odk.kitaaman.android.audio.AudioHelper;
import org.odk.kitaaman.android.utilities.ScreenContext;

public class ScreenContextAudioHelperFactory implements AudioHelperFactory {

    public AudioHelper create(Context context) {
        ScreenContext screenContext = (ScreenContext) context;
        return new AudioHelper(screenContext.getActivity(), screenContext.getViewLifecycle());
    }
}
