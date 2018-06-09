package cz.chladek.profiler.api.utils;

import android.content.Context;
import android.content.res.Configuration;

public enum Orientation {
    PORTRAIT, LANDSCAPE;

    public static Orientation getCurrent(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PORTRAIT : LANDSCAPE;
    }
}