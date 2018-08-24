package cz.chladek.profiler.api;

import android.os.Bundle;

/**
 * Helper class for handle Android application lifecycle.
 */
public class LifecycleHelper {

	private static final String BUNDLE_VISIBLE = "BUNDLE_VISIBLE";

	private final ProfilerAPI profiler;
	private boolean shouldBeVisible;

	protected LifecycleHelper(ProfilerAPI profiler) {
		this.profiler = profiler;
	}

	public void onCreate(Bundle savedInstanceState) {
		shouldBeVisible = savedInstanceState != null && savedInstanceState.getBoolean(BUNDLE_VISIBLE);
		profiler.restoreState(savedInstanceState);
	}

	public void onResume() {
		showWindowIfShouldBeVisible();
	}

	public void onPause() {
		if (profiler.isConnected() && profiler.isVisible()) {
			shouldBeVisible = true;
			profiler.setVisible(false, true);
		} else
			shouldBeVisible = false;
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(BUNDLE_VISIBLE, shouldBeVisible);
		profiler.saveState(outState);
	}

	public void onDestroy() {
		profiler.disconnect();
	}

	protected void onInstanceStateRestored() {
		showWindowIfShouldBeVisible();
	}

	private void showWindowIfShouldBeVisible() {
		if (shouldBeVisible && profiler.isConnected() && profiler.hasOverlayPermission())
			profiler.setVisible(true, true);
	}
}