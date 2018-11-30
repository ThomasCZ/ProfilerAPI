package cz.chladek.profiler.api;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cz.chladek.profiler.api.devices.DeviceConfig;
import cz.chladek.profiler.api.layout.AbsoluteLayout;
import cz.chladek.profiler.api.layout.FloatingLayout;
import cz.chladek.profiler.api.layout.Layout;
import cz.chladek.profiler.api.utils.Anchor;
import cz.chladek.profiler.api.utils.DeviceConfigHelper;
import cz.chladek.profiler.api.utils.Orientation;
import cz.chladek.profiler.api.utils.Size;

/**
 * Class for communication with Android application Profiler.
 *
 * @author Tomas Chladek
 */
public class ProfilerAPI {

	private static final class BundleKey {
		private static final String LAYOUT = "LAYOUT";
		private static final String WINDOW_ANCHOR_PORTRAIT = "WINDOW_ANCHOR_PORTRAIT";
		private static final String WINDOW_ANCHOR_LANDSCAPE = "WINDOW_ANCHOR_LANDSCAPE";
		private static final String LOCATION_X_PORTRAIT = "LOCATION_X_PORTRAIT";
		private static final String LOCATION_Y_PORTRAIT = "LOCATION_Y_PORTRAIT";
		private static final String LOCATION_X_LANDSCAPE = "LOCATION_X_LANDSCAPE";
		private static final String LOCATION_Y_LANDSCAPE = "LOCATION_Y_LANDSCAPE";
		private static final String BACKGROUND_ALPHA = "BACKGROUND_ALPHA";
		private static final String WINDOW_ALPHA = "WINDOW_ALPHA";
		private static final String CHART_SCALE = "CHART_SCALE";
		private static final String VISIBLE = "VISIBLE";
	}

	private static final String PROFILER_PACKAGE = "cz.chladek.profiler";
	private static final String PROFILER_CONNECT_ACTION = "cz.chladek.profiler.api.RemoteService";
	private static final String EXTRA_SENDER_PACKAGE = "EXTRA_SENDER_PACKAGE";
	private static final String EXTRA_SENDER_API_VERSION = "EXTRA_SENDER_API_VERSION";

	private static final String BUNDLE_SAVED_STATE = BuildConfig.APPLICATION_ID + ".BUNDLE_SAVED_STATE";

	public enum AppStatus {
		OK, NOT_INSTALLED, UNSUPPORTED_VERSION
	}

	public static final class Version {

		private Version() {
		}

		public static final int CODE = BuildConfig.VERSION_CODE;
		public static final String NAME = BuildConfig.VERSION_NAME;
	}

	private final Context context;
	private final Handler handler;
	private final LifecycleHelper lifecycleHelper;
	private ProfilerInterface profilerInterface;
	private DeviceConfig[] devices;
	private ProfilerEventListener listener;
	private boolean started;
	private Bundle state;

	public ProfilerAPI(@NonNull Context context) {
		this.context = context;

		handler = new Handler(Looper.getMainLooper());
		lifecycleHelper = new LifecycleHelper(this);
	}

	/**
	 * Check if communication with the application can be established.
	 *
	 * @return <ul>
	 * <li>{@link AppStatus#OK} - Communication can be established.</li>
	 * <li>{@link AppStatus#NOT_INSTALLED} - The application is not installed.</li>
	 * <li>{@link AppStatus#UNSUPPORTED_VERSION} - Installed version of the application is not compatible with this API.</li>
	 * </ul>
	 * @see #openGooglePlay()
	 */
	public AppStatus getAppStatus() {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(PROFILER_PACKAGE, PackageManager.GET_META_DATA);
			return info.versionCode >= 18 ? AppStatus.OK : AppStatus.UNSUPPORTED_VERSION;
		} catch (PackageManager.NameNotFoundException e) {
			return AppStatus.NOT_INSTALLED;
		}
	}

	/**
	 * Opens the application detail in Google Play.
	 */
	public void openGooglePlay() {
		try {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PROFILER_PACKAGE)));
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Useful for handle Android application lifecycle.
	 */
	public LifecycleHelper getLifecycleHelper() {
		return lifecycleHelper;
	}

	public void setListener(ProfilerEventListener listener) {
		this.listener = listener;
	}

	/**
	 * Establish communication with the application.
	 *
	 * @throws IllegalArgumentException when {@link ProfilerAPI#getAppStatus()} not returns {@link ProfilerAPI.AppStatus#OK}.
	 */
	public void connect() {
		AppStatus appStatus = getAppStatus();
		if (appStatus != AppStatus.OK)
			throw new IllegalArgumentException("getAppStatus() must returns AppStatus.OK to connect.");

		Intent intent = new Intent();
		intent.setPackage(PROFILER_PACKAGE);
		intent.setAction(PROFILER_CONNECT_ACTION);
		intent.putExtra(EXTRA_SENDER_PACKAGE, context.getPackageName());
		intent.putExtra(EXTRA_SENDER_API_VERSION, Version.CODE);
		context.bindService(intent, connection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
	}

	/**
	 * Cancel communication with the application.
	 */
	public void disconnect() {
		if (profilerInterface != null) {
			context.unbindService(connection);

			profilerInterface = null;

			if (state != null)
				state.clear();

			if (started) {
				started = false;

				handler.post(() -> {
					if (listener != null)
						listener.onDisconnected();
				});
			}
		}
	}

	public boolean isConnected() {
		return started && profilerInterface != null;
	}

	/**
	 * Save current Profiler state. Call in {@link Activity#onSaveInstanceState(Bundle)}.
	 *
	 * @see #getLifecycleHelper()
	 */
	public void saveState(@NonNull Bundle bundle) {
		bundle.putBundle(BUNDLE_SAVED_STATE, state);
	}

	/**
	 * Restore Profiler state. Call in {@link Activity#onCreate(Bundle)}.
	 *
	 * @see #getLifecycleHelper
	 */
	public void restoreState(@Nullable Bundle bundle) {
		if (bundle == null)
			return;

		state = bundle.getBundle(BUNDLE_SAVED_STATE);

		if (state == null)
			return;

		final ProfilerEventListener listenerBackup = listener;

		listener = new ProfilerEventListenerAdapter() {
			@Override
			public void onConnected() {
				if (state.containsKey(BundleKey.CHART_SCALE))
					setChartScale(state.getFloat(BundleKey.CHART_SCALE));

				if (state.containsKey(BundleKey.WINDOW_ALPHA))
					setWindowAlpha(state.getFloat(BundleKey.WINDOW_ALPHA));

				if (state.containsKey(BundleKey.BACKGROUND_ALPHA))
					setBackgroundAlpha(state.getFloat(BundleKey.BACKGROUND_ALPHA));

				Layout layout = state.getParcelable(BundleKey.LAYOUT);
				if (layout != null)
					setLayout(layout);

				if (state.containsKey(BundleKey.WINDOW_ANCHOR_PORTRAIT)) {
					Anchor anchor = Anchor.values()[state.getInt(BundleKey.WINDOW_ANCHOR_PORTRAIT)];
					setWindowAnchor(Orientation.PORTRAIT, anchor);
				}

				if (state.containsKey(BundleKey.WINDOW_ANCHOR_LANDSCAPE)) {
					Anchor anchor = Anchor.values()[state.getInt(BundleKey.WINDOW_ANCHOR_LANDSCAPE)];
					setWindowAnchor(Orientation.LANDSCAPE, anchor);
				}

				if (state.containsKey(BundleKey.LOCATION_X_PORTRAIT)) {
					int x = state.getInt(BundleKey.LOCATION_X_PORTRAIT);
					int y = state.getInt(BundleKey.LOCATION_Y_PORTRAIT);
					setLocation(Orientation.PORTRAIT, x, y);
				}

				if (state.containsKey(BundleKey.LOCATION_X_LANDSCAPE)) {
					int x = state.getInt(BundleKey.LOCATION_X_LANDSCAPE);
					int y = state.getInt(BundleKey.LOCATION_Y_LANDSCAPE);
					setLocation(Orientation.LANDSCAPE, x, y);
				}

				if (state.getBoolean(BundleKey.VISIBLE))
					setVisible(true, false);

				lifecycleHelper.onInstanceStateRestored();

				listener = listenerBackup;

				if (listener != null)
					listener.onStateRestored();
			}
		};

		connect();
	}

	/**
	 * Obtain supported device configs on this Android device.
	 *
	 * @return Array of {@link DeviceConfig}, that can be monitored on this device.
	 * @see DeviceConfigHelper
	 */
	@NonNull
	public DeviceConfig[] getSupportedDevices() {
		if (devices == null) {
			throwWhenDisconnected();

			try {
				DeviceConfig[] array = profilerInterface.getSupportedDevices();
				ArrayList<DeviceConfig> list = new ArrayList<>(array.length);

				for (DeviceConfig device : array)
					if (device != null)
						list.add(device);

				devices = list.toArray(new DeviceConfig[0]);
			} catch (RemoteException ignored) {
			}
		}

		return Arrays.copyOf(devices, devices.length);
	}

	/**
	 * Set window layout. Null for clear, disable monitoring and hide window.
	 *
	 * @see AbsoluteLayout
	 * @see FloatingLayout
	 * @see #getSupportedDevices()
	 */
	public void setLayout(@Nullable Layout layout) {
		throwWhenDisconnected();

		state.putParcelable(BundleKey.LAYOUT, layout);

		try {
			profilerInterface.setLayout(layout);
		} catch (RemoteException ignored) {
		}
	}

	@NonNull
	public Anchor getWindowAnchor(@NonNull Orientation orientation) {
		throwWhenDisconnected();

		try {
			int anchor = profilerInterface.getWindowAnchor(orientation.ordinal());
			return Anchor.values()[anchor];
		} catch (RemoteException ignored) {
			return Anchor.TOP_LEFT;
		}
	}

	/**
	 * Set window anchor for specific orientation. Default is {@link Anchor#TOP_LEFT}.
	 *
	 * @see #setLocation(Orientation, int, int)
	 * @see #setChartScale(float)
	 */
	public void setWindowAnchor(@NonNull Orientation orientation, @NonNull Anchor anchor) {
		throwWhenDisconnected();

		switch (orientation) {
			case PORTRAIT:
				state.putInt(BundleKey.WINDOW_ANCHOR_PORTRAIT, anchor.ordinal());
				break;
			case LANDSCAPE:
				state.putInt(BundleKey.WINDOW_ANCHOR_LANDSCAPE, anchor.ordinal());
				break;
		}

		try {
			profilerInterface.setWindowAnchor(orientation.ordinal(), anchor.ordinal());
		} catch (RemoteException ignored) {
		}
	}

	/**
	 * Returns current window location in pixels from set anchor.
	 *
	 * @see #setLocation(Orientation, int, int)
	 */
	public Point getCurrentLocation() {
		throwWhenDisconnected();

		try {
			return profilerInterface.getCurrentLocation();
		} catch (RemoteException ignored) {
			return null;
		}
	}

	/**
	 * Sets window location for screen orientation in pixels from top left corner with desired window anchor.
	 */
	public void setLocation(@NonNull Orientation orientation, int x, int y) {
		throwWhenDisconnected();

		switch (orientation) {
			case PORTRAIT:
				state.putInt(BundleKey.LOCATION_X_PORTRAIT, x);
				state.putInt(BundleKey.LOCATION_Y_PORTRAIT, y);
				break;
			case LANDSCAPE:
				state.putInt(BundleKey.LOCATION_X_LANDSCAPE, x);
				state.putInt(BundleKey.LOCATION_Y_LANDSCAPE, y);
				break;
		}

		try {
			profilerInterface.setLocation(orientation.ordinal(), x, y);
		} catch (RemoteException ignored) {
		}
	}

	/**
	 * Sets window background alpha. Default value is 0.5.
	 */
	public void setBackgroundAlpha(@FloatRange(from = 0, to = 1) float alpha) {
		throwWhenDisconnected();

		state.putFloat(BundleKey.BACKGROUND_ALPHA, alpha);

		try {
			profilerInterface.setBackgroundAlpha(alpha);
		} catch (RemoteException ignored) {
		}
	}

	/**
	 * Sets window alpha. Default value is 1.
	 */
	public void setWindowAlpha(@FloatRange(from = 0, to = 1) float alpha) {
		throwWhenDisconnected();

		state.putFloat(BundleKey.WINDOW_ALPHA, alpha);

		try {
			profilerInterface.setWindowAlpha(alpha);
		} catch (RemoteException ignored) {
		}
	}

	/**
	 * Returns current window size in pixels.
	 */
	public Size getWindowSize() {
		throwWhenDisconnected();

		try {
			return profilerInterface.getWindowSize();
		} catch (RemoteException e) {
			return null;
		}
	}

	/**
	 * Scale of the profiler charts. Scale pivot is anchor set by {@link #setLocation(Orientation, int, int)}.
	 *
	 * @param scale recommended range is between 0.75 and 1.25, but value is clamped to range from 0 to 2
	 */
	public void setChartScale(@FloatRange(from = 0, to = 2) float scale) {
		throwWhenDisconnected();

		state.putFloat(BundleKey.CHART_SCALE, scale);

		try {
			profilerInterface.setChartScale(scale);
		} catch (RemoteException ignored) {
		}
	}

	/**
	 * Profiler application needs permission to show monitor.
	 *
	 * @see #requestOverlayPermission(boolean)
	 */
	public boolean hasOverlayPermission() {
		throwWhenDisconnected();

		try {
			return profilerInterface.hasOverlayPermission();
		} catch (RemoteException ignored) {
			return false;
		}
	}

	/**
	 * Request permission needed to show window.
	 *
	 * @param ask if true, dialog will be displayed
	 */
	public void requestOverlayPermission(boolean ask) {
		throwWhenDisconnected();

		try {
			profilerInterface.requestOverlayPermission(ask);
		} catch (RemoteException ignored) {
		}
	}

	public boolean isVisible() {
		throwWhenDisconnected();

		try {
			return profilerInterface.isVisible();
		} catch (RemoteException ignored) {
			return false;
		}
	}

	/**
	 * Sets profiler window visibility.
	 *
	 * @param visible  true for visible
	 * @param animated true for animated change with duration 300ms
	 */
	public void setVisible(boolean visible, boolean animated) {
		throwWhenDisconnected();

		if (visible && !hasOverlayPermission())
			throw new IllegalStateException("Profiler application does not have permission to show window.");

		state.putBoolean(BundleKey.VISIBLE, visible);

		try {
			profilerInterface.setVisible(visible, animated);
		} catch (RemoteException ignored) {
		}
	}

	private void throwWhenDisconnected() {
		if (profilerInterface == null)
			throw new RuntimeException("Service is not connected.");
	}

	private ServiceConnection connection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			profilerInterface = ProfilerInterface.Stub.asInterface(service);

			try {
				profilerInterface.setListener(remoteProfilerListener);
				profilerInterface.start();
			} catch (RemoteException ignored) {
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			profilerInterface = null;

			if (state != null)
				state.clear();

			handler.post(() -> {
				if (started && listener != null)
					listener.onDisconnected();

				started = false;
			});
		}
	};

	private ProfilerListener remoteProfilerListener = new ProfilerListener.Stub() {

		@Override
		public void onStarted() {
			if (state == null)
				state = new Bundle();

			started = true;

			handler.post(() -> {
				if (listener != null)
					listener.onConnected();
			});
		}

		@Override
		public void onWindowSizeChanged(final int width, final int height) {
			handler.post(() -> {
				if (listener != null)
					listener.onWindowSizeChanged(width, height);
			});
		}
	};
}