package cz.chladek.profiler.api;

import android.support.annotation.MainThread;

public abstract class ProfilerEventListenerAdapter implements ProfilerEventListener {

	@Override
	@MainThread
	public void onConnected() {
	}

	@Override
	@MainThread
	public void onDisconnected() {
	}

	@Override
	@MainThread
	public void onWindowSizeChanged(int width, int height) {
	}

	@Override
	@MainThread
	public void onStateRestored() {
	}
}