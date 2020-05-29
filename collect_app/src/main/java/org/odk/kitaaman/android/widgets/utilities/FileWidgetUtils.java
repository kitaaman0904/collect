package org.odk.kitaaman.android.widgets.utilities;

import androidx.annotation.NonNull;

import org.odk.kitaaman.android.utilities.FileUtil;

import java.io.File;

public class FileWidgetUtils {

    private FileWidgetUtils() {

    }

    public static String getDestinationPathFromSourcePath(@NonNull String sourcePath, String instanceFolder, FileUtil fileUtil) {
        String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
        return instanceFolder + File.separator + fileUtil.getRandomFilename() + extension;
    }
}
