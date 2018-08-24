package cz.chladek.profiler.api.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;

import cz.chladek.profiler.api.devices.DeviceConfig;

/**
 * Class for basic operations with DeviceConfig array.
 */
public class DeviceConfigHelper {

	private DeviceConfigHelper() {
	}

	@NonNull
	public static <T extends DeviceConfig> T[] findDevices(@NonNull DeviceConfig[] devices, @NonNull Class<T> clazz) {
		return findDevices(devices, clazz, null);
	}

	/**
	 * Returns array of desired DeviceConfig.
	 *
	 * @param suitableListener allows filter specific devices, for instance by name or type.
	 */
	@NonNull
	@SuppressWarnings("unchecked")
	public static <T extends DeviceConfig> T[] findDevices(@NonNull DeviceConfig[] devices, @NonNull Class<T> clazz, @Nullable SuitableListener<T> suitableListener) {
		ArrayList<T> result = new ArrayList<>();
		for (DeviceConfig device : devices)
			if (clazz.isInstance(device)) {
				T deviceOfType = clazz.cast(device);
				if (suitableListener == null || suitableListener.isSuitable(deviceOfType))
					result.add(deviceOfType);
			}

		return result.toArray((T[]) Array.newInstance(clazz, 0));
	}

	/**
	 * Returns desired DeviceConfig or null when not found.
	 */
	@Nullable
	public static <T extends DeviceConfig> T findDevice(@NonNull DeviceConfig[] devices, @NonNull Class<T> clazz) {
		for (DeviceConfig device : devices)
			if (clazz.isInstance(device))
				return clazz.cast(device);

		return null;
	}

	public interface SuitableListener<T> {

		boolean isSuitable(T device);
	}
}