package cz.chladek.profiler.api.devices;

import android.os.Parcel;

public class RAMDeviceConfig extends DeviceConfig {

	protected RAMDeviceConfig(Parcel in) {
		super(in, Type.RAM);
	}

	@Override
	public String toString() {
		return "RAMDeviceConfig{}";
	}
}