package cz.chladek.profiler.api.devices;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class RAMDeviceConfig extends DeviceConfig {

	protected RAMDeviceConfig(Parcel in) {
		super(in, Type.RAM);
	}

	@NonNull
	@Override
	public String toString() {
		return "RAMDeviceConfig{}";
	}
}