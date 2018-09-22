package cz.chladek.profiler.api.devices;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class TemperatureDeviceConfig extends DeviceConfig {

	private String name;

	protected TemperatureDeviceConfig(Parcel in) {
		super(in, Type.TEMPERATURE);
		name = in.readString();
	}

	@NonNull
	public String getName() {
		return name;
	}

	@NonNull
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