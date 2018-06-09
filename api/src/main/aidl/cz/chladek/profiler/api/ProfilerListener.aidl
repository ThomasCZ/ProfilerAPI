package cz.chladek.profiler.api;

interface ProfilerListener {

    void onStarted();

    void onWindowSizeChanged(int width, int height);
}