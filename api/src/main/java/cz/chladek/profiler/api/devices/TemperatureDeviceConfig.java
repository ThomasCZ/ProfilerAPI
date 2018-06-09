package cz.chladek.profiler.api.devices;

import android.os.Parcel;

public class TemperatureDeviceConfig extends DeviceConfig {

    private String name;

    protected TemperatureDeviceConfig(Parcel in) {
        super(in, Type.TEMPERATURE);
        name = in.readString();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TemperatureDeviceConfig{name=" + name + '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
    }
}