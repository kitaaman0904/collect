package org.odk.kitaaman.android.activities;

import android.app.Fragment;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.activities.viewmodels.FormMapViewModel;
import org.odk.kitaaman.android.geo.MapPoint;
import org.odk.kitaaman.android.geo.MapProvider;
import org.odk.kitaaman.android.geo.TestMapFragment;
import org.odk.kitaaman.android.injection.config.AppDependencyModule;
import org.odk.kitaaman.android.instances.TestInstancesRepository;
import org.odk.kitaaman.android.preferences.AdminKeys;
import org.odk.kitaaman.android.preferences.AdminSharedPreferences;
import org.odk.kitaaman.android.preferences.MapsPreferences;
import org.odk.kitaaman.android.provider.InstanceProvider;
import org.odk.kitaaman.android.support.RobolectricHelpers;
import org.odk.kitaaman.android.utilities.ApplicationConstants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.kitaaman.android.activities.FormMapViewModelTest.testInstances;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
public class FormMapActivityTest {
    private ActivityController activityController;
    private FormMapActivity activity;

    private final TestMapFragment map = new TestMapFragment();

    private final List<MapPoint> expectedPoints = Arrays.asList(new MapPoint(10.0, 125.6),
            new MapPoint(10.1, 125.6), new MapPoint(10.1, 126.6),
            new MapPoint(10.3, 125.6), new MapPoint(10.3, 125.7),
            new MapPoint(10.4, 125.6));
    private final MapPoint currentLocation = new MapPoint(5, 5);

    @Before public void setUpActivity() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
               @Override
               public MapProvider providesMapProvider() {
                   MapProvider mapProvider = mock(MapProvider.class);
                   when(mapProvider.createMapFragment(ArgumentMatchers.any())).thenReturn(map);
                   return mapProvider;
               }
           });

        activityController = RobolectricHelpers.buildThemedActivity(FormMapActivity.class);
        activity = (FormMapActivity) activityController.get();

        TestInstancesRepository testInstancesRepository = new TestInstancesRepository(Arrays.asList(testInstances));
        FormMapViewModel viewModel = new FormMapViewModel(FormMapViewModelTest.TEST_FORM_1, testInstancesRepository);
        activity.viewModelFactory = new TestFactory(viewModel);

        activityController.setup();
    }

    @Test public void startingFormMap_zoomsToFitAllInstanceMarkers_ifThereAreInstanceMarkers() {
        assertThat(map.getZoomCount(), is(1));
        assertThat(map.getLatestZoomPoint(), is(nullValue()));
        assertThat(map.getLatestZoomBoundingBox(), is(expectedPoints));
        assertThat(map.getLatestScaleFactor(), is(0.8));
        assertThat(map.wasLatestZoomCallAnimated(), is(false));
    }

    @Test public void startingFormMap_doesNotZoom_ifThereAreNoInstanceMarkers_andLocationIsUnavailable() {
        // The @Before block set up a map with points. Reset everything for this test.
        map.resetState();

        ActivityController controller = RobolectricHelpers.buildThemedActivity(FormMapActivity.class);
        FormMapActivity activity = (FormMapActivity) controller.get();

        TestInstancesRepository testInstancesRepository = new TestInstancesRepository(new ArrayList<>());
        FormMapViewModel viewModel = new FormMapViewModel(FormMapViewModelTest.TEST_FORM_1, testInstancesRepository);
        activity.viewModelFactory = new TestFactory(viewModel);

        controller.setup();

        assertThat(map.getZoomCount(), is(0));
    }

    @Test public void locationChange_zoomsToCurrentLocation_ifTheViewportWasNotPreviouslyUpdated() {
        // The @Before block set up a map with points. Reset everything for this test.
        map.resetState();

        ActivityController controller = RobolectricHelpers.buildThemedActivity(FormMapActivity.class);
        FormMapActivity activity = (FormMapActivity) controller.get();

        TestInstancesRepository testInstancesRepository = new TestInstancesRepository(new ArrayList<>());
        FormMapViewModel viewModel = new FormMapViewModel(FormMapViewModelTest.TEST_FORM_1, testInstancesRepository);
        activity.viewModelFactory = new TestFactory(viewModel);

        controller.setup();

        assertThat(map.getZoomCount(), is(0));

        map.onLocationChanged(currentLocation);

        assertThat(map.getZoomCount(), is(1));
        assertThat(map.getLatestZoomPoint(), is(currentLocation));
        assertThat(map.wasLatestZoomCallAnimated(), is(true));
    }

    @Test public void tappingOnZoomToCurrentLocationButton_zoomsToCurrentLocationWithAnimation() {
        activity.findViewById(R.id.zoom_to_location).performClick();

        assertThat(map.getZoomCount(), is(2)); // once on initialization and once on click
        assertThat(map.getLatestZoomPoint(), is(currentLocation));
        assertThat(map.wasLatestZoomCallAnimated(), is(true));
    }

    @Test public void tappingOnZoomToFitButton_zoomsToFitAllInstanceMarkersWithoutAnimation() {
        activity.findViewById(R.id.zoom_to_bounds).performClick();

        assertThat(map.getZoomCount(), is(2));
        assertThat(map.getLatestZoomPoint(), is(nullValue()));
        assertThat(map.getLatestZoomBoundingBox(), is(expectedPoints));
        assertThat(map.getLatestScaleFactor(), is(0.8));
        assertThat(map.wasLatestZoomCallAnimated(), is(false));
    }

    @Test public void tappingOnLayerMenu_opensLayerDialog() {
        List<Fragment> fragments = activity.getFragmentManager().getFragments();
        assertThat(fragments, not(hasItem(isA(MapsPreferences.class))));

        activity.findViewById(R.id.layer_menu).performClick();

        fragments = activity.getFragmentManager().getFragments();
        assertThat(fragments, hasItem(isA(MapsPreferences.class)));
    }

    @LooperMode(PAUSED)
    @Test public void tappingOnInstance_centersToThatInstanceAndKeepsTheSameZoom() {
        MapPoint sent = new MapPoint(10.3, 125.7);
        map.zoomToPoint(new MapPoint(7, 8), 7, false);

        assertThat(map.getLatestZoomPoint().lat, is(7.0));
        assertThat(map.getLatestZoomPoint().lon, is(8.0));
        assertThat(map.getZoom(), is(7.0));

        activity.onFeatureClicked(map.getFeatureIdFor(sent));

        assertThat(map.getLatestZoomPoint().lat, is(10.3));
        assertThat(map.getLatestZoomPoint().lon, is(125.7));
        assertThat(map.getZoom(), is(7.0));
    }

    @Test public void tappingOnNewInstanceButton_opensNewInstance() {
        activity.findViewById(R.id.new_instance).performClick();

        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

        assertThat(actual.getAction(), is(Intent.ACTION_EDIT));
        assertThat(actual.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE), is(nullValue()));
    }

    @Ignore("Doesn't work with field-based dependency injection because we don't get an opportunity" +
            "to set test doubles before onCreate() is called after the orientation change")
    @Test public void centerAndZoomLevel_areRestoredAfterOrientationChange() {
        map.zoomToPoint(new MapPoint(7, 7), 7, false);

        RuntimeEnvironment.setQualifiers("+land");
        activityController.configurationChange();

        assertThat(map.getCenter(), is(new MapPoint(7, 7)));
        assertThat(map.getZoom(), is(7));
    }

    // Note that there's a point with deleted status included. This shouldn't be possible in real
    // usage because deleting a form removes the geometry from the database. However, the database
    // allows a deleted instance with geometry so we test it.
    @Test public void mappedPoints_matchInstancesWithGeometry() {
        assertThat(map.getMappedPointCount(), is(expectedPoints.size()));
        for (MapPoint expectedPoint : expectedPoints) {
            assertThat(map.isMapped(expectedPoint), is(true));
        }
    }

    @LooperMode(PAUSED)
    @Test public void openingEditableInstances_launchesEditActivity() {
        MapPoint editableAndFinalized = new MapPoint(10.1, 125.6);
        MapPoint unfinalized = new MapPoint(10.1, 126.6);
        MapPoint failedToSend = new MapPoint(10.3, 125.6);

        MapPoint[] testPoints = {editableAndFinalized, unfinalized, failedToSend};

        for (MapPoint toTap : testPoints) {
            int featureId = map.getFeatureIdFor(toTap);

            activity.onFeatureClicked(featureId);
            clickOnOpenFormChip();
            Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

            assertThat(actual.getAction(), is(Intent.ACTION_EDIT));
            assertThat(actual.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE), is(nullValue()));
        }
    }

    @LooperMode(PAUSED)
    @Test public void openingEditableInstance_whenEditingSettingisOff_launchesViewActivity() {
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_EDIT_SAVED, false);

        MapPoint editableAndFinalized = new MapPoint(10.1, 125.6);
        MapPoint unfinalized = new MapPoint(10.1, 126.6);
        MapPoint failedToSend = new MapPoint(10.3, 125.6);

        MapPoint[] testPoints = {editableAndFinalized, unfinalized, failedToSend};

        for (MapPoint toTap : testPoints) {
            int featureId = map.getFeatureIdFor(toTap);

            activity.onFeatureClicked(featureId);
            clickOnOpenFormChip();
            Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

            assertThat(actual.getAction(), is(Intent.ACTION_EDIT));
            assertThat(actual.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE), is(ApplicationConstants.FormModes.VIEW_SENT));
        }
    }

    @LooperMode(PAUSED)
    @Test public void openingUneditableInstances_launchesViewActivity() {
        MapPoint sent = new MapPoint(10.3, 125.7);

        int featureId = map.getFeatureIdFor(sent);

        activity.onFeatureClicked(featureId);
        clickOnOpenFormChip();
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

        assertThat(actual.getAction(), is(Intent.ACTION_EDIT));
        assertThat(actual.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE), is(ApplicationConstants.FormModes.VIEW_SENT));
    }

    @LooperMode(PAUSED)
    @Test public void tappingOnEditableInstance_showsSubmissionSummaryWithAppropriateMessage() {
        MapPoint editableAndFinalized = new MapPoint(10.1, 125.6);
        MapPoint unfinalized = new MapPoint(10.1, 126.6);
        MapPoint failedToSend = new MapPoint(10.3, 125.6);

        MapPoint[] testPoints = {editableAndFinalized, unfinalized, failedToSend};

        for (MapPoint toTap : testPoints) {
            int featureId = map.getFeatureIdFor(toTap);

            FormMapViewModel.MappableFormInstance mappableFormInstance = activity.instancesByFeatureId.get(featureId);
            activity.onFeatureClicked(featureId);

            assertSubmissionSummaryContent(mappableFormInstance);
        }
    }

    @LooperMode(PAUSED)
    @Test public void tappingOnUneditableInstances_showsSubmissionSummaryWithAppropriateMessage() {
        MapPoint sent = new MapPoint(10.3, 125.7);

        int featureId = map.getFeatureIdFor(sent);
        FormMapViewModel.MappableFormInstance mappableFormInstance = activity.instancesByFeatureId.get(featureId);
        activity.onFeatureClicked(featureId);

        assertSubmissionSummaryContent(mappableFormInstance);
    }

    // Geometry is removed from the database on instance encryption but just in case there is an
    // encrypted instance with geometry available, show an encrypted toast.
    @LooperMode(PAUSED)
    @Test public void tappingOnEncryptedInstances_showsSubmissionSummaryWithAppropriateMessage() {
        MapPoint submissionFailedCantEditWhenFinalized = new MapPoint(10.4, 125.6);

        int featureId = map.getFeatureIdFor(submissionFailedCantEditWhenFinalized);
        FormMapViewModel.MappableFormInstance mappableFormInstance = activity.instancesByFeatureId.get(featureId);
        activity.onFeatureClicked(featureId);

        assertSubmissionSummaryContent(mappableFormInstance);
    }

    // Geometry is removed from the database on instance deletion but just in case there is a
    // deleted instance with geometry available, show a deleted toast.
    @LooperMode(PAUSED)
    @Test public void tappingOnDeletedInstances_showsSubmissionSummaryWithAppropriateMessage() {
        MapPoint deleted = new MapPoint(10.0, 125.6);

        int featureId = map.getFeatureIdFor(deleted);
        FormMapViewModel.MappableFormInstance mappableFormInstance = activity.instancesByFeatureId.get(featureId);
        activity.onFeatureClicked(featureId);

        assertSubmissionSummaryContent(mappableFormInstance);
    }

    private void clickOnOpenFormChip() {
        activity.findViewById(R.id.openFormChip).performClick();
        assertThat(activity.summarySheet.getState(), is(BottomSheetBehavior.STATE_HIDDEN));
    }

    private void assertSubmissionSummaryContent(FormMapViewModel.MappableFormInstance mappableFormInstance) {
        assertThat(((TextView) activity.findViewById(R.id.submission_name)).getText().toString(), is(mappableFormInstance.getInstanceName()));
        String instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(activity, mappableFormInstance.getStatus(), mappableFormInstance.getLastStatusChangeDate());
        assertThat(((TextView) activity.findViewById(R.id.status_text)).getText().toString(), is(instanceLastStatusChangeDate));

        switch (mappableFormInstance.getClickAction()) {
            case DELETED_TOAST:
                assertThat(activity.findViewById(R.id.info).getVisibility(), is(View.VISIBLE));
                assertThat(activity.findViewById(R.id.openFormChip).getVisibility(), is(View.GONE));
                break;
            case NOT_VIEWABLE_TOAST:
                assertThat(activity.findViewById(R.id.info).getVisibility(), is(View.VISIBLE));
                assertThat(((TextView) activity.findViewById(R.id.info)).getText().toString(), is(activity.getString(R.string.cannot_edit_completed_form)));
                assertThat(activity.findViewById(R.id.openFormChip).getVisibility(), is(View.GONE));
                break;
            case OPEN_READ_ONLY:
                assertThat(activity.findViewById(R.id.info).getVisibility(), is(View.GONE));
                assertThat(activity.findViewById(R.id.openFormChip).getVisibility(), is(View.VISIBLE));
                assertThat(((Chip) activity.findViewById(R.id.openFormChip)).getText(), is(activity.getString(R.string.view_sent_forms)));
                break;
            case OPEN_EDIT:
                assertThat(activity.findViewById(R.id.info).getVisibility(), is(View.GONE));
                assertThat(activity.findViewById(R.id.openFormChip).getVisibility(), is(View.VISIBLE));
                assertThat(((Chip) activity.findViewById(R.id.openFormChip)).getText(), is(activity.getString(R.string.review_data)));
                break;
        }
    }

    private static class TestFactory implements ViewModelProvider.Factory {

        private final FormMapViewModel viewModel;

        TestFactory(FormMapViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) viewModel;
        }
    }
}
