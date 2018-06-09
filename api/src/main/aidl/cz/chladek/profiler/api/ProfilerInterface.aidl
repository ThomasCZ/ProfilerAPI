package cz.chladek.profiler.api;

import android.graphics.Point;
import cz.chladek.profiler.api.devices.DeviceConfig;
import cz.chladek.profiler.api.layout.Layout;
import cz.chladek.profiler.api.ProfilerListener;
import cz.chladek.profiler.api.utils.Size;

interface ProfilerInterface {

    void setListener(ProfilerListener listener);

    DeviceConfig[] getSupportedDevices();

    void setLayout(in Layout layout);

    Point getLocation();

    void setLocation(int orientation, int anchor, int x, int y);

    void setBackgroundAlpha(float alpha);

    void setWindowAlpha(float alpha);

    Size getWindowSize();

    void setChartScale(float scale);

    boolean hasOverlayPermission();

    void requestOverlayPermission(boolean ask);

    boolean isVisible();

    void setVisible(boolean visible, boolean animated);

    void start();
}