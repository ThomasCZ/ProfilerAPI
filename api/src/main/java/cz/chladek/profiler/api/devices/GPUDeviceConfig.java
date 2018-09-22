package cz.chladek.profiler.api.devices;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class GPUDeviceConfig extends DeviceConfig {

	protected GPUDeviceConfig(Parcel in) {
		super(in, Type.GPU);
	}

	@NonNull
	@Override
	public String toString() {
		return "GPUDeviceConfig{}";
	}
}