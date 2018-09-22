package cz.chladek.profiler.api.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Size implements Parcelable {

	public final int width, height;

	public Size(int width, int height) {
		this.width = width;
		this.height = height;
	}

	protected Size(Parcel in) {
		width = in.readInt();
		height = in.readInt();
	}

	@NonNull
	@Override
	public String toString() {
		return "Size{width=" + width + ", height=" + height + '}';
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(width);
		dest.writeInt(height);
	}

	public static final Creator<Size> CREATOR = new Creator<Size>() {
		@Override
		public Size createFromParcel(Parcel in) {
			return new Size(in);
		}

		@Override
		public Size[] newArray(int size) {
			return new Size[size];
		}
	};
}