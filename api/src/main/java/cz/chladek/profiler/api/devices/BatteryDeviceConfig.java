package cz.chladek.profiler.api.devices;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class BatteryDeviceConfig extends DeviceConfig {

	public enum Direction {
		CHARGE, DISCHARGE
	}

	private Direction direction;

	protected BatteryDeviceConfig(Parcel in) {
		super(in, Type.BATTERY);

		if (unit == Unit.CURRENT)
			direction = Direction.values()[in.readInt()];
	}

	public Direction getDirection() {
		return direction;
	}

	/**
	 * Can be set only when {@link DeviceConfig#getUnit()} returns some of temperature unit.
	 * If {@link cz.chladek.profiler.api.devices.DeviceConfig.Unit#TEMPERATURE} is set, celsius or fahrenheit will be automatically selected from device locale.
	 *
	 * @see cz.chladek.profiler.api.devices.DeviceConfig.Unit#TEMPERATURE
	 * @see cz.chladek.profiler.api.devices.DeviceConfig.Unit#TEMPERATURE_CELSIUS
	 * @see cz.chladek.profiler.api.devices.DeviceConfig.Unit#TEMPERATURE_FAHRENHEIT
	 */
	public void setUnit(Unit newUnit) {
		if (unit != Unit.TEMPERATURE && unit != Unit.TEMPERATURE_CELSIUS && unit != Unit.TEMPERATURE_FAHRENHEIT)
			throw new IllegalArgumentException("Unit can be set only when temperature is measured");

		if (newUnit != Unit.TEMPERATURE && newUnit != Unit.TEMPERATURE_CELSIUS && newUnit != Unit.TEMPERATURE_FAHRENHEIT)
			throw new IllegalArgumentException("Illegal temperature unit");

		unit = newUnit;
	}

	@NonNull
	@Override
	public String toString() {
		return "BatteryDeviceConfig{unit=" + unit + (direction == null ? "" : ", direction=" + direction.name()) + '}';
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		if (unit == Unit.CURRENT)
			dest.writeInt(direction.ordinal());
	}
}