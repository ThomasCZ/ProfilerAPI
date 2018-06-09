package cz.chladek.profiler.api.devices;

import android.os.Parcel;

public class GPUDeviceConfig extends DeviceConfig {

    protected GPUDeviceConfig(Parcel in) {
        super(in, Type.GPU);
    }

    @Override
    public String toString() {
        return "GPUDeviceConfig{}";
    }
}