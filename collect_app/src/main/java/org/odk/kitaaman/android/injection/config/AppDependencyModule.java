package org.odk.kitaaman.android.injection.config;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.kitaaman.android.BuildConfig;
import org.odk.kitaaman.android.analytics.Analytics;
import org.odk.kitaaman.android.analytics.FirebaseAnalytics;
import org.odk.kitaaman.android.backgroundwork.CollectBackgroundWorkManager;
import org.odk.kitaaman.android.dao.FormsDao;
import org.odk.kitaaman.android.dao.InstancesDao;
import org.odk.kitaaman.android.events.RxEventBus;
import org.odk.kitaaman.android.formentry.media.AudioHelperFactory;
import org.odk.kitaaman.android.formentry.media.ScreenContextAudioHelperFactory;
import org.odk.kitaaman.android.geo.MapProvider;
import org.odk.kitaaman.android.jobs.CollectJobCreator;
import org.odk.kitaaman.android.metadata.InstallIDProvider;
import org.odk.kitaaman.android.metadata.SharedPreferencesInstallIDProvider;
import org.odk.kitaaman.android.network.ConnectivityProvider;
import org.odk.kitaaman.android.network.NetworkStateProvider;
import org.odk.kitaaman.android.openrosa.CollectThenSystemContentTypeMapper;
import org.odk.kitaaman.android.openrosa.OpenRosaAPIClient;
import org.odk.kitaaman.android.openrosa.OpenRosaHttpInterface;
import org.odk.kitaaman.android.openrosa.okhttp.OkHttpConnection;
import org.odk.kitaaman.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;
import org.odk.kitaaman.android.preferences.AdminSharedPreferences;
import org.odk.kitaaman.android.preferences.GeneralSharedPreferences;
import org.odk.kitaaman.android.preferences.MetaSharedPreferencesProvider;
import org.odk.kitaaman.android.preferences.qr.ObservableQRCodeGenerator;
import org.odk.kitaaman.android.preferences.qr.QRCodeGenerator;
import org.odk.kitaaman.android.storage.StorageInitializer;
import org.odk.kitaaman.android.storage.StoragePathProvider;
import org.odk.kitaaman.android.storage.StorageStateProvider;
import org.odk.kitaaman.android.storage.migration.StorageEraser;
import org.odk.kitaaman.android.storage.migration.StorageMigrationRepository;
import org.odk.kitaaman.android.storage.migration.StorageMigrator;
import org.odk.kitaaman.android.tasks.sms.SmsSubmissionManager;
import org.odk.kitaaman.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.kitaaman.android.utilities.ActivityAvailability;
import org.odk.kitaaman.android.utilities.AdminPasswordProvider;
import org.odk.kitaaman.android.utilities.AndroidUserAgent;
import org.odk.kitaaman.android.utilities.DeviceDetailsProvider;
import org.odk.kitaaman.android.utilities.FormListDownloader;
import org.odk.kitaaman.android.utilities.PermissionUtils;
import org.odk.kitaaman.android.utilities.WebCredentialsUtils;
import org.odk.kitaaman.android.version.VersionInformation;
import org.odk.kitaaman.utilities.BackgroundWorkManager;
import org.odk.kitaaman.utilities.UserAgentProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static org.odk.kitaaman.android.preferences.GeneralKeys.KEY_INSTALL_ID;

/**
 * Add dependency providers here (annotated with @Provides)
 * for objects you need to inject
 */
@Module
@SuppressWarnings("PMD.CouplingBetweenObjects")
public class AppDependencyModule {

    @Provides
    SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    SmsSubmissionManagerContract provideSmsSubmissionManager(Application application) {
        return new SmsSubmissionManager(application);
    }

    @Provides
    Context context(Application application) {
        return application;
    }

    @Provides
    public InstancesDao provideInstancesDao() {
        return new InstancesDao();
    }

    @Provides
    public FormsDao provideFormsDao() {
        return new FormsDao();
    }

    @Provides
    @Singleton
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    MimeTypeMap provideMimeTypeMap() {
        return MimeTypeMap.getSingleton();
    }

    @Provides
    @Singleton
    UserAgentProvider providesUserAgent() {
        return new AndroidUserAgent();
    }

    @Provides
    @Singleton
    OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                new CollectThenSystemContentTypeMapper(mimeTypeMap),
                userAgentProvider.getUserAgent()
        );
    }

    @Provides
    public OpenRosaAPIClient provideCollectServerClient(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
        return new OpenRosaAPIClient(httpInterface, webCredentialsUtils);
    }

    @Provides
    WebCredentialsUtils provideWebCredentials() {
        return new WebCredentialsUtils();
    }

    @Provides
    FormListDownloader provideDownloadFormListDownloader(
            Application application,
            OpenRosaAPIClient openRosaAPIClient,
            WebCredentialsUtils webCredentialsUtils,
            FormsDao formsDao) {
        return new FormListDownloader(
                application,
                openRosaAPIClient,
                webCredentialsUtils,
                formsDao
        );
    }

    @Provides
    @Singleton
    public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
        com.google.firebase.analytics.FirebaseAnalytics firebaseAnalyticsInstance = com.google.firebase.analytics.FirebaseAnalytics.getInstance(application);
        return new FirebaseAnalytics(firebaseAnalyticsInstance, generalSharedPreferences);
    }

    @Provides
    public PermissionUtils providesPermissionUtils() {
        return new PermissionUtils();
    }

    @Provides
    public ReferenceManager providesReferenceManager() {
        return ReferenceManager.instance();
    }

    @Provides
    public AudioHelperFactory providesAudioHelperFactory() {
        return new ScreenContextAudioHelperFactory();
    }

    @Provides
    public ActivityAvailability providesActivityAvailability(Context context) {
        return new ActivityAvailability(context);
    }

    @Provides
    @Singleton
    public StorageMigrationRepository providesStorageMigrationRepository() {
        return new StorageMigrationRepository();
    }

    @Provides
    @Singleton
    public StorageInitializer providesStorageInitializer() {
        return new StorageInitializer();
    }

    @Provides
    StorageMigrator providesStorageMigrator(StoragePathProvider storagePathProvider, StorageStateProvider storageStateProvider, StorageMigrationRepository storageMigrationRepository, ReferenceManager referenceManager, BackgroundWorkManager backgroundWorkManager, Analytics analytics) {
        StorageEraser storageEraser = new StorageEraser(storagePathProvider);

        return new StorageMigrator(storagePathProvider, storageStateProvider, storageEraser, storageMigrationRepository, GeneralSharedPreferences.getInstance(), referenceManager, backgroundWorkManager, analytics);
    }

    @Provides
    InstallIDProvider providesInstallIDProvider(Context context) {
        SharedPreferences prefs = new MetaSharedPreferencesProvider(context).getMetaSharedPreferences();
        return new SharedPreferencesInstallIDProvider(prefs, KEY_INSTALL_ID);
    }

    @Provides
    public DeviceDetailsProvider providesDeviceDetailsProvider(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return new DeviceDetailsProvider() {

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getDeviceId() {
                return telMgr.getDeviceId();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getLine1Number() {
                return telMgr.getLine1Number();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getSubscriberId() {
                return telMgr.getSubscriberId();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getSimSerialNumber() {
                return telMgr.getSimSerialNumber();
            }
        };
    }

    @Provides
    @Singleton
    GeneralSharedPreferences providesGeneralSharedPreferences(Context context) {
        return new GeneralSharedPreferences(context);
    }

    @Provides
    @Singleton
    AdminSharedPreferences providesAdminSharedPreferences(Context context) {
        return new AdminSharedPreferences(context);
    }

    @Provides
    @Singleton
    public MapProvider providesMapProvider() {
        return new MapProvider();
    }

    @Provides
    public StorageStateProvider providesStorageStateProvider() {
        return new StorageStateProvider();
    }

    @Provides
    public StoragePathProvider providesStoragePathProvider() {
        return new StoragePathProvider();
    }

    @Provides
    public AdminPasswordProvider providesAdminPasswordProvider() {
        return new AdminPasswordProvider(AdminSharedPreferences.getInstance());
    }

    @Provides
    public CollectJobCreator providesCollectJobCreator() {
        return new CollectJobCreator();
    }

    @Provides
    public BackgroundWorkManager providesBackgroundWorkManager() {
        return new CollectBackgroundWorkManager();
    }

    @Provides
    public NetworkStateProvider providesConnectivityProvider() {
        return new ConnectivityProvider();
    }

    @Provides
    public QRCodeGenerator providesQRCodeGenerator() {
        return new ObservableQRCodeGenerator();
    }

    @Provides
    public VersionInformation providesVersionInformation() {
        return new VersionInformation(() -> BuildConfig.VERSION_NAME);
    }
}
