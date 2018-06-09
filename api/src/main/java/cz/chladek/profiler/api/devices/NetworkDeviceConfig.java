package cz.chladek.profiler.api.devices;

import android.os.Parcel;

public class NetworkDeviceConfig extends DeviceConfig {

    public enum Direction {
        RECEIVED, TRANSMITTED
    }

    private String name;
    private Direction direction;

    protected NetworkDeviceConfig(Parcel in) {
        super(in, Type.NETWORK);
        name = in.readString();
        direction = Direction.values()[in.readInt()];
    }

    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "NetworkDeviceConfig{name=" + name + ", direction=" + direction + '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeInt(direction.ordinal());
    }
}