package cz.chladek.profiler.api;

import androidx.annotation.MainThread;

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