package cz.chladek.profiler.api.devices;

import android.os.Parcel;

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