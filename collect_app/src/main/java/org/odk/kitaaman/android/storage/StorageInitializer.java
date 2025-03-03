package org.odk.kitaaman.android.storage;

import android.content.Context;
import android.os.Environment;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.application.Collect;

import java.io.File;

import timber.log.Timber;

public class StorageInitializer {

    private StorageStateProvider storageStateProvider;
    private StoragePathProvider storagePathProvider;
    private Context context;

    public StorageInitializer() {
        this(new StorageStateProvider(), new StoragePathProvider(), Collect.getInstance());
    }

    public StorageInitializer(StorageStateProvider storageStateProvider, StoragePathProvider storagePathProvider, Context context) {
        this.storageStateProvider = storageStateProvider;
        this.storagePathProvider = storagePathProvider;
        this.context = context;
    }

    public void createOdkDirsOnStorage() throws RuntimeException {
        if (!storageStateProvider.isStorageMounted()) {
            throw new RuntimeException(context.getString(R.string.sdcard_unmounted, Environment.getExternalStorageState()));
        }

        for (String dirPath : storagePathProvider.getOdkDirPaths()) {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    String message = context.getString(R.string.cannot_create_directory, dirPath);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            } else {
                if (!dir.isDirectory()) {
                    String message = context.getString(R.string.not_a_directory, dirPath);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            }
        }
    }
}
