package cz.chladek.profiler.api.devices;

import android.os.Parcel;

public class CPUDeviceConfig extends DeviceConfig {

	public static final int WHOLE_CPU = -1;

	public enum Mode {
		LOAD, FREQUENCY
	}

	private int core;
	private Mode mode;

	protected CPUDeviceConfig(Parcel in) {
		super(in, Type.CPU);
		core = in.readInt();
		mode = Mode.values()[in.readInt()];
	}

	/**
	 * Returns core index or {@link #WHOLE_CPU} for whole CPU load.
	 */
	public int getCore() {
		return core;
	}

	public Mode getMode() {
		return mode;
	}

	@Override
	public String toString() {
		return "CPUDeviceConfig{core=" + core + ", mode=" + mode.name() + '}';
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(core);
		dest.writeInt(mode.ordinal());
	}
}