package org.odk.kitaaman.android.injection.config;

import android.app.Application;
import android.telephony.SmsManager;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.kitaaman.android.activities.FormDownloadListActivity;
import org.odk.kitaaman.android.activities.FormEntryActivity;
import org.odk.kitaaman.android.activities.FormHierarchyActivity;
import org.odk.kitaaman.android.activities.FormMapActivity;
import org.odk.kitaaman.android.activities.GeoPointMapActivity;
import org.odk.kitaaman.android.activities.GeoPolyActivity;
import org.odk.kitaaman.android.activities.GoogleDriveActivity;
import org.odk.kitaaman.android.activities.GoogleSheetsUploaderActivity;
import org.odk.kitaaman.android.activities.InstanceUploaderListActivity;
import org.odk.kitaaman.android.activities.MainMenuActivity;
import org.odk.kitaaman.android.activities.SplashScreenActivity;
import org.odk.kitaaman.android.adapters.InstanceUploaderAdapter;
import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.application.Collect;
import org.odk.kitaaman.android.events.RxEventBus;
import org.odk.kitaaman.android.formentry.ODKView;
import org.odk.kitaaman.android.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.kitaaman.android.fragments.DataManagerList;
import org.odk.kitaaman.android.geo.GoogleMapFragment;
import org.odk.kitaaman.android.geo.MapboxMapFragment;
import org.odk.kitaaman.android.geo.OsmDroidMapFragment;
import org.odk.kitaaman.android.preferences.qr.QRCodeTabsActivity;
import org.odk.kitaaman.android.preferences.qr.ShowQRCodeFragment;
import org.odk.kitaaman.android.logic.PropertyManager;
import org.odk.kitaaman.android.openrosa.OpenRosaHttpInterface;
import org.odk.kitaaman.android.preferences.AdminPasswordDialogFragment;
import org.odk.kitaaman.android.preferences.AdminSharedPreferences;
import org.odk.kitaaman.android.preferences.FormManagementPreferences;
import org.odk.kitaaman.android.preferences.FormMetadataFragment;
import org.odk.kitaaman.android.preferences.GeneralSharedPreferences;
import org.odk.kitaaman.android.preferences.IdentityPreferences;
import org.odk.kitaaman.android.preferences.ServerPreferencesFragment;
import org.odk.kitaaman.android.preferences.UserInterfacePreferencesFragment;
import org.odk.kitaaman.android.storage.StorageInitializer;
import org.odk.kitaaman.android.storage.migration.StorageMigrationDialog;
import org.odk.kitaaman.android.storage.migration.StorageMigrationService;
import org.odk.kitaaman.android.tasks.InstanceServerUploaderTask;
import org.odk.kitaaman.android.tasks.ServerPollingJob;
import org.odk.kitaaman.android.tasks.sms.SmsNotificationReceiver;
import org.odk.kitaaman.android.tasks.sms.SmsSender;
import org.odk.kitaaman.android.tasks.sms.SmsSentBroadcastReceiver;
import org.odk.kitaaman.android.tasks.sms.SmsService;
import org.odk.kitaaman.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.kitaaman.android.upload.AutoSendWorker;
import org.odk.kitaaman.android.utilities.AuthDialogUtility;
import org.odk.kitaaman.android.utilities.FormDownloader;
import org.odk.kitaaman.android.widgets.ExStringWidget;
import org.odk.kitaaman.android.widgets.QuestionWidget;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/**
 * Dagger component for the application. Should include
 * application level Dagger Modules and be built with Application
 * object.
 *
 * Add an `inject(MyClass myClass)` method here for objects you want
 * to inject into so Dagger knows to wire it up.
 *
 * Annotated with @Singleton so modules can include @Singletons that will
 * be retained at an application level (as this an instance of this components
 * is owned by the Application object).
 *
 * If you need to call a provider directly from the component (in a test
 * for example) you can add a method with the type you are looking to fetch
 * (`MyType myType()`) to this interface.
 *
 * To read more about Dagger visit: https://google.github.io/dagger/users-guide
 **/

@Singleton
@Component(modules = {
        AppDependencyModule.class
})
public interface AppDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        Builder appDependencyModule(AppDependencyModule testDependencyModule);

        AppDependencyComponent build();
    }

    void inject(Collect collect);

    void inject(SmsService smsService);

    void inject(SmsSender smsSender);

    void inject(SmsSentBroadcastReceiver smsSentBroadcastReceiver);

    void inject(SmsNotificationReceiver smsNotificationReceiver);

    void inject(InstanceUploaderAdapter instanceUploaderAdapter);

    void inject(DataManagerList dataManagerList);

    void inject(PropertyManager propertyManager);

    void inject(FormEntryActivity formEntryActivity);

    void inject(InstanceServerUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(FormDownloader formDownloader);

    void inject(ServerPollingJob serverPollingJob);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(InstanceUploaderListActivity activity);

    void inject(GoogleDriveActivity googleDriveActivity);

    void inject(GoogleSheetsUploaderActivity googleSheetsUploaderActivity);

    void inject(QuestionWidget questionWidget);

    void inject(ExStringWidget exStringWidget);

    void inject(ODKView odkView);

    void inject(FormMetadataFragment formMetadataFragment);

    void inject(GeoPointMapActivity geoMapActivity);

    void inject(GeoPolyActivity geoPolyActivity);

    void inject(FormMapActivity formMapActivity);

    void inject(OsmDroidMapFragment mapFragment);

    void inject(GoogleMapFragment mapFragment);

    void inject(MapboxMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(QRCodeTabsActivity qrCodeTabsActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(StorageInitializer storageInitializer);

    void inject(StorageMigrationService storageMigrationService);

    void inject(AutoSendWorker autoSendWorker);

    void inject(StorageMigrationDialog storageMigrationDialog);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(SplashScreenActivity splashScreenActivity);

    void inject(FormHierarchyActivity formHierarchyActivity);

    void inject(FormManagementPreferences formManagementPreferences);

    void inject(IdentityPreferences identityPreferences);

    void inject(UserInterfacePreferencesFragment userInterfacePreferencesFragment);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    SmsManager smsManager();

    SmsSubmissionManagerContract smsSubmissionManagerContract();

    RxEventBus rxEventBus();

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    Analytics analytics();

    GeneralSharedPreferences generalSharedPreferences();

    AdminSharedPreferences adminSharedPreferences();
}
