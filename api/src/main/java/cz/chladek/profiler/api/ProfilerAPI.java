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
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

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
        private static final String CONNECTED = "CONNECTED";
        private static final String LAYOUT = "LAYOUT";
        private static final String LOCATION_PORTRAIT_ANCHOR = "LOCATION_PORTRAIT_ANCHOR";
        private static final String LOCATION_PORTRAIT_X = "LOCATION_PORTRAIT_X";
        private static final String LOCATION_PORTRAIT_Y = "LOCATION_PORTRAIT_Y";
        private static final String LOCATION_LANDSCAPE_ANCHOR = "LOCATION_LANDSCAPE_ANCHOR";
        private static final String LOCATION_LANDSCAPE_X = "LOCATION_LANDSCAPE_X";
        private static final String LOCATION_LANDSCAPE_Y = "LOCATION_LANDSCAPE_Y";
        private static final String BACKGROUND_ALPHA = "BACKGROUND_ALPHA";
        private static final String WINDOW_ALPHA = "WINDOW_ALPHA";
        private static final String CHART_SCALE = "CHART_SCALE";
        private static final String VISIBLE = "VISIBLE";
    }

    private static final String PROFILER_PACKAGE = "cz.chladek.profiler";
    private static final String PROFILER_CONNECT_ACTION = "cz.chladek.profiler.api.RemoteService";
    private static final String EXTRA_SENDER_PACKAGE = "EXTRA_SENDER_PACKAGE";

    private static final String BUNDLE_SAVED_STATE = BuildConfig.APPLICATION_ID + ".BUNDLE_SAVED_STATE";

    public enum AppStatus {
        OK, NOT_INSTALLED, UNSUPPORTED_VERSION
    }

    private Context context;
    private Handler handler;
    private ProfilerInterface profilerInterface;
    private DeviceConfig[] devices;
    private ProfilerEventListener listener;
    private boolean started;
    private Bundle state;

    public ProfilerAPI(@NonNull Context context) {
        this.context = context;

        handler = new Handler(Looper.getMainLooper());
        state = new Bundle();
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
            return info.versionCode >= 14 ? AppStatus.OK : AppStatus.UNSUPPORTED_VERSION;
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

    public void setListener(ProfilerEventListener listener) {
        this.listener = listener;
    }

    /**
     * Establish communication with the application.
     */
    public void connect() {
        Intent intent = new Intent();
        intent.setPackage(PROFILER_PACKAGE);
        intent.setAction(PROFILER_CONNECT_ACTION);
        intent.putExtra(EXTRA_SENDER_PACKAGE, context.getPackageName());
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);

        state.putBoolean(BundleKey.CONNECTED, true);
    }

    /**
     * Cancel communication with the application.
     */
    public void disconnect() {
        if (profilerInterface != null) {
            context.unbindService(connection);

            profilerInterface = null;
            state.clear();

            if (started) {
                started = false;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null)
                            listener.onDisconnected();
                    }
                });
            }
        }
    }

    public boolean isConnected() {
        return started && profilerInterface != null;
    }

    /**
     * Save current Profiler state. Call in {@link Activity#onSaveInstanceState(Bundle)}.
     */
    public void saveState(@NonNull Bundle bundle) {
        bundle.putBundle(BUNDLE_SAVED_STATE, state);
    }

    /**
     * Restore Profiler state. Call in {@link Activity#onCreate(Bundle)}.
     */
    public void restoreState(@Nullable Bundle bundle) {
        if (bundle == null)
            return;

        state = bundle.getBundle(BUNDLE_SAVED_STATE);

        if (state == null) {
            state = new Bundle();
            return;
        }

        if (state.getBoolean(BundleKey.CONNECTED)) {
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

                    if (state.containsKey(BundleKey.LOCATION_PORTRAIT_ANCHOR)) {
                        Anchor anchor = Anchor.values()[state.getInt(BundleKey.LOCATION_PORTRAIT_ANCHOR)];
                        int x = state.getInt(BundleKey.LOCATION_PORTRAIT_X);
                        int y = state.getInt(BundleKey.LOCATION_PORTRAIT_Y);
                        setLocation(Orientation.PORTRAIT, anchor, x, y);
                    }

                    if (state.containsKey(BundleKey.LOCATION_LANDSCAPE_ANCHOR)) {
                        Anchor anchor = Anchor.values()[state.getInt(BundleKey.LOCATION_LANDSCAPE_ANCHOR)];
                        int x = state.getInt(BundleKey.LOCATION_LANDSCAPE_X);
                        int y = state.getInt(BundleKey.LOCATION_LANDSCAPE_Y);
                        setLocation(Orientation.LANDSCAPE, anchor, x, y);
                    }

                    if (state.getBoolean(BundleKey.VISIBLE))
                        setVisible(true, false);

                    listener = listenerBackup;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onStateRestored();
                        }
                    });
                }
            };

            connect();
        }
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
                devices = profilerInterface.getSupportedDevices();
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

    /**
     * Returns current window location in pixels from set anchor.
     *
     * @see #setLocation(Orientation, Anchor, int, int)
     */
    public Point getLocation() {
        throwWhenDisconnected();

        try {
            return profilerInterface.getLocation();
        } catch (RemoteException ignored) {
            return null;
        }
    }

    /**
     * Sets window location for screen orientation in pixels from top left corner with desired window anchor.
     */
    public void setLocation(@NonNull Orientation orientation, @NonNull Anchor anchor, int x, int y) {
        throwWhenDisconnected();

        switch (orientation) {
            case PORTRAIT:
                state.putInt(BundleKey.LOCATION_PORTRAIT_ANCHOR, anchor.ordinal());
                state.putInt(BundleKey.LOCATION_PORTRAIT_X, x);
                state.putInt(BundleKey.LOCATION_PORTRAIT_Y, y);
                break;
            case LANDSCAPE:
                state.putInt(BundleKey.LOCATION_LANDSCAPE_ANCHOR, anchor.ordinal());
                state.putInt(BundleKey.LOCATION_LANDSCAPE_X, x);
                state.putInt(BundleKey.LOCATION_LANDSCAPE_Y, y);
                break;
        }

        try {
            profilerInterface.setLocation(orientation.ordinal(), anchor.ordinal(), x, y);
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
     * Scale of the profiler charts. Scale pivot is anchor set by {@link #setLocation(Orientation, Anchor, int, int)}.
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
            state.clear();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (started && listener != null)
                        listener.onDisconnected();

                    started = false;
                }
            });
        }
    };

    private ProfilerListener remoteProfilerListener = new ProfilerListener.Stub() {

        @Override
        public void onStarted() {
            started = true;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null)
                        listener.onConnected();
                }
            });
        }

        @Override
        public void onWindowSizeChanged(final int width, final int height) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null)
                        listener.onWindowSizeChanged(width, height);
                }
            });
        }
    };
}