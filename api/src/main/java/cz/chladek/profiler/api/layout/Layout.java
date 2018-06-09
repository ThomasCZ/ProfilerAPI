package cz.chladek.profiler.api.layout;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Arrays;

import cz.chladek.profiler.api.devices.DeviceConfig;

public abstract class Layout implements Parcelable {

    protected enum Type {
        ABSOLUTE, FLOATING
    }

    protected Type type;
    protected int width, height;

    protected Layout(Type type) {
        this.type = type;
    }

    protected Layout(Parcel in, Type type) {
        this.type = type;
        width = in.readInt();
        height = in.readInt();

        Parcelable[] parcelables = in.readParcelableArray(LayoutItem.class.getClassLoader());
        LayoutItem[] items = Arrays.copyOf(parcelables, parcelables.length, LayoutItem[].class);
        processItems(items);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Nullable
    public abstract <T extends DeviceConfig> T getDevice(int x, int y);

    public abstract void removeDevice(@Nullable DeviceConfig device);

    public abstract boolean containsDevice(@Nullable DeviceConfig device);

    public abstract int getCount();

    protected void checkRange(int x, int y) {
        if (x < 0 || x >= width)
            throw new IllegalArgumentException("x index " + x + " is out of range 0 - " + (width - 1));

        if (y < 0 || y >= height)
            throw new IllegalArgumentException("y index " + y + " is out of range 0 - " + (height - 1));
    }

    protected abstract void processItems(LayoutItem[] items);

    protected abstract LayoutItem[] getItems();

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.ordinal());
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeParcelableArray(getItems(), flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Layout> CREATOR = new Creator<Layout>() {
        @Override
        public Layout createFromParcel(Parcel in) {
            switch (Type.values()[in.readInt()]) {
                case ABSOLUTE:
                    return new AbsoluteLayout(in);
                case FLOATING:
                    return new FloatingLayout(in);
                default:
                    return null;
            }
        }

        @Override
        public Layout[] newArray(int size) {
            return new Layout[size];
        }
    };
}