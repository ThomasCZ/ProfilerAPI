package cz.chladek.profiler.api.layout;

import android.os.Parcel;
import android.os.Parcelable;

import cz.chladek.profiler.api.devices.DeviceConfig;

public class LayoutItem implements Parcelable {

    protected final int x, y;
    protected final DeviceConfig device;

    protected LayoutItem(int x, int y, DeviceConfig device) {
        this.x = x;
        this.y = y;
        this.device = device;
    }

    protected LayoutItem(Parcel in) {
        x = in.readInt();
        y = in.readInt();
        device = in.readParcelable(DeviceConfig.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(x);
        dest.writeInt(y);
        dest.writeParcelable(device, flags);
    }

    public static final Creator<LayoutItem> CREATOR = new Creator<LayoutItem>() {
        @Override
        public LayoutItem createFromParcel(Parcel in) {
            return new LayoutItem(in);
        }

        @Override
        public LayoutItem[] newArray(int size) {
            return new LayoutItem[size];
        }
    };
}