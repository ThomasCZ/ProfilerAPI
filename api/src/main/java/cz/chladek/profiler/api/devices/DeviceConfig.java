package cz.chladek.profiler.api.devices;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

public abstract class DeviceConfig implements Parcelable {

    public enum Type {
        CPU, GPU, RAM, NETWORK, TEMPERATURE, BATTERY
    }

    public enum Unit {
        BYTES, PERCENTAGE, TEMPERATURE, VOLTAGE, CURRENT
    }

    public static final double BAR_MAX_MAX = -963258741;

    public static final String TAG_CURRENT = "#current";
    public static final String TAG_CURRENT_FILTERED = "#fcurrent";
    public static final String TAG_AVERAGE = "#average";
    public static final String TAG_MAX = "#max";
    public static final String TAG_ARROW_UP = "#up";
    public static final String TAG_ARROW_DOWN = "#down";

    private Type type;
    private String link;
    private String labelFormat;
    private double barMax;
    private int barColor;
    protected Unit unit;

    protected DeviceConfig(Parcel in, Type type) {
        this.type = type;
        link = in.readString();
        labelFormat = in.readString();
        barMax = in.readDouble();
        barColor = in.readInt();
        unit = Unit.values()[in.readInt()];
    }

    public Type getType() {
        return type;
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    /**
     * Label can contains tags. For instance this is default format for network "TAG_ARROW_DOWN TAG_CURRENT/TAG_MAX".
     *
     * @see #TAG_CURRENT
     * @see #TAG_CURRENT_FILTERED
     * @see #TAG_AVERAGE
     * @see #TAG_MAX
     * @see #TAG_ARROW_UP
     * @see #TAG_ARROW_DOWN
     */
    public void setLabelFormat(String labelFormat) {
        this.labelFormat = labelFormat;
    }

    /**
     * @see #BAR_MAX_MAX
     */
    public double getBarMax() {
        return barMax;
    }

    /**
     * Use {@link #BAR_MAX_MAX} for maximum from all available values.
     */
    public void setBarMax(double barMax) {
        this.barMax = barMax;
    }

    public int getBarColor() {
        return barColor;
    }

    public void setBarColor(@ColorInt int barColor) {
        this.barColor = barColor;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.ordinal());
        dest.writeString(link);
        dest.writeString(labelFormat);
        dest.writeDouble(barMax);
        dest.writeInt(barColor);
        dest.writeInt(unit.ordinal());
    }

    public static final Creator<DeviceConfig> CREATOR = new Creator<DeviceConfig>() {
        @Override
        public DeviceConfig createFromParcel(Parcel in) {
            switch (Type.values()[in.readInt()]) {
                case CPU:
                    return new CPUDeviceConfig(in);
                case GPU:
                    return new GPUDeviceConfig(in);
                case RAM:
                    return new RAMDeviceConfig(in);
                case NETWORK:
                    return new NetworkDeviceConfig(in);
                case TEMPERATURE:
                    return new TemperatureDeviceConfig(in);
                case BATTERY:
                    return new BatteryDeviceConfig(in);
                default:
                    return null;
            }
        }

        @Override
        public DeviceConfig[] newArray(int size) {
            return new DeviceConfig[size];
        }
    };
}