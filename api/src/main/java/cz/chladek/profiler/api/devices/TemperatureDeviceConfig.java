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

	/**
	 * If {@link cz.chladek.profiler.api.devices.DeviceConfig.Unit#TEMPERATURE} is set, celsius or fahrenheit will be automatically selected from device locale.
	 *
	 * @see cz.chladek.profiler.api.devices.DeviceConfig.Unit#TEMPERATURE
	 * @see cz.chladek.profiler.api.devices.DeviceConfig.Unit#TEMPERATURE_CELSIUS
	 * @see cz.chladek.profiler.api.devices.DeviceConfig.Unit#TEMPERATURE_FAHRENHEIT
	 */
	public void setUnit(Unit unit) {
		if (unit != Unit.TEMPERATURE && unit != Unit.TEMPERATURE_CELSIUS && unit != Unit.TEMPERATURE_FAHRENHEIT)
			throw new IllegalArgumentException("Illegal temperature unit");

		this.unit = unit;
	}

	@NonNull
	@Override
	public String toString() {
		return "TemperatureDeviceConfig{name=" + name + ", unit=" + unit + '}';
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(name);
	}
}