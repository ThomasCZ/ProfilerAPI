package cz.chladek.profiler.api.devices;

import android.os.Parcel;

public class CPUDeviceConfig extends DeviceConfig {

    public static final int WHOLE_CPU = -1;

    private int core;

    protected CPUDeviceConfig(Parcel in) {
        super(in, Type.CPU);
        core = in.readInt();
    }

    /**
     * Returns core index or {@link #WHOLE_CPU} for whole CPU load.
     */
    public int getCore() {
        return core;
    }

    @Override
    public String toString() {
        return "CPUDeviceConfig{core=" + core + '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(core);
    }
}