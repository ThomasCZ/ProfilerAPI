package cz.chladek.profiler.api.layout;

import android.os.Parcel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cz.chladek.profiler.api.devices.DeviceConfig;

/**
 * Allows simple layout definition that can be packed to desired size later.
 */
public class FloatingLayout extends Layout {

	public enum Direction {
		RIGHT, DOWN
	}

	private ArrayList<DeviceConfig> devices;
	private Direction direction;

	public FloatingLayout() {
		super(Type.FLOATING);

		width = height = -1;
		devices = new ArrayList<>();
	}

	protected FloatingLayout(Parcel in) {
		super(in, Type.FLOATING);
	}

	/**
	 * Adds device to layout or null for empty place. Layout have to be packed again.
	 */
	public void addDevice(@Nullable DeviceConfig device) {
		devices.add(device);

		width = height = -1;
		direction = null;
	}

	/**
	 * Return DeviceConfig at location.
	 *
	 * @throws IllegalStateException when layout is not packed
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T extends DeviceConfig> T getDevice(int x, int y) {
		if (!isPacked())
			throw new IllegalStateException("Layout is not packed");

		checkRange(x, y);

		switch (direction) {
			case RIGHT:
				int ir = y * width + x;
				return (T) (ir < devices.size() ? devices.get(ir) : null);
			case DOWN:
				int id = x * height + y;
				return (T) (id < devices.size() ? devices.get(id) : null);
			default:
				return null;
		}
	}

	@Override
	public boolean containsDevice(@Nullable DeviceConfig device) {
		return devices.contains(device);
	}

	/**
	 * Returns count of DeviceConfigs and empty places.
	 */
	@Override
	public int getCount() {
		return devices.size();
	}

	@Override
	public void removeDevice(@Nullable DeviceConfig device) {
		devices.remove(device);
	}

	/**
	 * Returns floating direction or null when layout is not packed.
	 */
	@Nullable
	public Direction getDirection() {
		return direction;
	}

	public boolean isPacked() {
		return direction != null;
	}

	/**
	 * Pack layout to desired size.
	 *
	 * @throws IllegalArgumentException when content does not fit into desired size
	 */
	public void pack(@NonNull Direction direction, int width, int height) {
		if (width * height < devices.size())
			throw new IllegalArgumentException(devices.size() + " devices do not fit into box " + width + "×" + height);

		this.width = width;
		this.height = height;
		this.direction = direction;
	}

	@Override
	protected void processItems(LayoutItem[] items) {
		direction = items.length > 1 && items[0].x != items[1].x - 1 ? Direction.DOWN : Direction.RIGHT;
		devices = new ArrayList<>();

		for (LayoutItem item : items)
			devices.add(item.device);
	}

	@Override
	protected LayoutItem[] getItems() {
		if (!isPacked())
			throw new IllegalStateException("Layout is not packed.");

		ArrayList<LayoutItem> items = new ArrayList<>(devices.size());

		switch (direction) {
			case RIGHT:
				for (int i = 0; i < devices.size(); i++)
					items.add(new LayoutItem(i % width, i / width, devices.get(i)));
				break;
			case DOWN:
				for (int i = 0; i < devices.size(); i++)
					items.add(new LayoutItem(i / height, i % height, devices.get(i)));
				break;
		}

		return items.toArray(new LayoutItem[0]);
	}
}