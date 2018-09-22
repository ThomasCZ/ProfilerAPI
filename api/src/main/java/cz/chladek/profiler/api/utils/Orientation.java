package cz.chladek.profiler.api.utils;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public enum Orientation {
	PORTRAIT, LANDSCAPE;

	public static Orientation getCurrent(@NonNull Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PORTRAIT : LANDSCAPE;
	}
}