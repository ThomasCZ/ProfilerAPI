package cz.chladek.profiler.api.layout;

import android.os.Parcel;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import cz.chladek.profiler.api.devices.DeviceConfig;

/**
 * Allows define specific layout of DeviceConfigs in desired positions.
 */
public class AbsoluteLayout extends Layout {

    private DeviceConfig[][] devices;

    public AbsoluteLayout(int width, int height) {
        super(Type.ABSOLUTE);

        this.width = width;
        this.height = height;
        devices = new DeviceConfig[width][height];
    }

    protected AbsoluteLayout(Parcel in) {
        super(in, Type.ABSOLUTE);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends DeviceConfig> T getDevice(int x, int y) {
        checkRange(x, y);
        return (T) devices[x][y];
    }

    @Override
    public boolean containsDevice(@Nullable DeviceConfig device) {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (devices[x][y] == device)
                    return true;

        return false;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (devices[x][y] != null)
                    count++;

        return count;
    }

    /**
     * Sets device in desired location. Null for remove actual.
     */
    public void setDevice(int x, int y, @Nullable DeviceConfig device) {
        checkRange(x, y);
        devices[x][y] = device;
    }

    @Override
    public void removeDevice(@Nullable DeviceConfig device) {
        if (device == null)
            return;

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (devices[x][y] == device) {
                    devices[x][y] = null;
                    return;
                }
    }

    @Override
    protected void processItems(LayoutItem[] items) {
        devices = new DeviceConfig[width][height];

        for (LayoutItem item : items)
            devices[item.x][item.y] = item.device;
    }

    @Override
    protected LayoutItem[] getItems() {
        ArrayList<LayoutItem> items = new ArrayList<>();

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (devices[x][y] != null)
                    items.add(new LayoutItem(x, y, devices[x][y]));

        return items.toArray(new LayoutItem[0]);
    }
}