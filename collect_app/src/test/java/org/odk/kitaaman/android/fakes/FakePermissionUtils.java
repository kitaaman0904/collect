package org.odk.kitaaman.android.fakes;

import android.app.Activity;
import androidx.annotation.NonNull;

import org.odk.kitaaman.android.listeners.PermissionListener;
import org.odk.kitaaman.android.utilities.PermissionUtils;

/**
 * Mocked implementation of {@link PermissionUtils}.
 * The runtime permissions can be stubbed for unit testing
 *
 * @author Shobhit Agarwal
 */
public class FakePermissionUtils extends PermissionUtils {

    private boolean isPermissionGranted;

    @Override
    protected void requestPermissions(Activity activity, @NonNull PermissionListener listener, String... permissions) {
        if (isPermissionGranted) {
            listener.granted();
        } else {
            listener.denied();
        }
    }

    @Override
    protected void showAdditionalExplanation(Activity activity, int title, int message, int drawable, @NonNull PermissionListener action) {
        action.denied();
    }

    public void setPermissionGranted(boolean permissionGranted) {
        isPermissionGranted = permissionGranted;
    }
}
