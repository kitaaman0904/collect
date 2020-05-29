package org.odk.kitaaman.android.formentry.media;

import android.content.Context;

import org.odk.kitaaman.android.audio.AudioHelper;

public interface AudioHelperFactory {

    AudioHelper create(Context context);
}
