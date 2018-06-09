package cz.chladek.profiler.api;

import android.support.annotation.MainThread;

public interface ProfilerEventListener {

    @MainThread
    void onConnected();

    @MainThread
    void onDisconnected();

    @MainThread
    void onWindowSizeChanged(int width, int height);

    @MainThread
    void onStateRestored();
}