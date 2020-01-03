package cz.chladek.profiler.api.example;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import cz.chladek.profiler.api.ProfilerAPI;
import cz.chladek.profiler.api.ProfilerEventListener;
import cz.chladek.profiler.api.devices.CPUDeviceConfig;
import cz.chladek.profiler.api.devices.DeviceConfig;
import cz.chladek.profiler.api.devices.GPUDeviceConfig;
import cz.chladek.profiler.api.devices.RAMDeviceConfig;
import cz.chladek.profiler.api.layout.AbsoluteLayout;
import cz.chladek.profiler.api.layout.FloatingLayout;
import cz.chladek.profiler.api.utils.Anchor;
import cz.chladek.profiler.api.utils.DeviceConfigHelper;
import cz.chladek.profiler.api.utils.Orientation;
import cz.chladek.profiler.api.utils.Size;

public class MainActivity extends Activity {

	private ProfilerAPI profiler;

	private Button connectButton;
	private TextView appStatusTextView, connectedTextView, devicesTextView, permissionTextView, currentLocationTextView, sizeTextView;
	private EditText locationPortXField, locationPortYField, locationLandXField, locationLandYField, sizeScaleField;
	private RadioGroup floatingLayoutDirectionGroup, windowAnchorPortGroup, windowAnchorLandGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		profiler = new ProfilerAPI(this);
		profiler.setListener(profilerEventListener);
		profiler.getLifecycleHelper().onCreate(savedInstanceState);

		appStatusTextView = findViewById(R.id.appStatusTextView);
		connectedTextView = findViewById(R.id.connectedTextView);
		permissionTextView = findViewById(R.id.permissionTextView);
		devicesTextView = findViewById(R.id.devicesListTextView);
		currentLocationTextView = findViewById(R.id.currentLocationTextView);
		sizeTextView = findViewById(R.id.sizeTextView);

		connectButton = findViewById(R.id.connectButton);
		connectButton.setOnClickListener(onClickListener);

		findViewById(R.id.disconnectButton).setOnClickListener(onClickListener);
		findViewById(R.id.showButton).setOnClickListener(onClickListener);
		findViewById(R.id.showAnimatedButton).setOnClickListener(onClickListener);
		findViewById(R.id.hideButton).setOnClickListener(onClickListener);
		findViewById(R.id.hideAnimatedButton).setOnClickListener(onClickListener);
		findViewById(R.id.openGooglePlayButton).setOnClickListener(onClickListener);
		findViewById(R.id.requestPermissionButton).setOnClickListener(onClickListener);
		findViewById(R.id.requestPermissionAskButton).setOnClickListener(onClickListener);
		findViewById(R.id.createAbsoluteLayoutButton).setOnClickListener(onClickListener);
		findViewById(R.id.createFloatingLayoutButton).setOnClickListener(onClickListener);
		findViewById(R.id.clearLayoutButton).setOnClickListener(onClickListener);
		findViewById(R.id.getDevicesButton).setOnClickListener(onClickListener);
		findViewById(R.id.getLocationButton).setOnClickListener(onClickListener);
		findViewById(R.id.setLocationPortButton).setOnClickListener(onClickListener);
		findViewById(R.id.setLocationLandButton).setOnClickListener(onClickListener);
		findViewById(R.id.setSizeScaleButton).setOnClickListener(onClickListener);
		findViewById(R.id.getSizeButton).setOnClickListener(onClickListener);

		locationPortXField = findViewById(R.id.locationPortXField);
		locationPortYField = findViewById(R.id.locationPortYField);
		locationLandXField = findViewById(R.id.locationLandXField);
		locationLandYField = findViewById(R.id.locationLandYField);
		sizeScaleField = findViewById(R.id.sizeScaleField);

		windowAnchorPortGroup = findViewById(R.id.anchorPortGroup);
		windowAnchorLandGroup = findViewById(R.id.anchorLandGroup);
		floatingLayoutDirectionGroup = findViewById(R.id.floatingLayoutDirectionGroup);

		setAnchorListener(windowAnchorPortGroup, Orientation.PORTRAIT, onAnchorChangedListener);
		setAnchorListener(windowAnchorLandGroup, Orientation.LANDSCAPE, onAnchorChangedListener);

		((SeekBar) findViewById(R.id.backgroundAlphaSeekBar)).setOnSeekBarChangeListener(onSeekBarChangeListener);
		((SeekBar) findViewById(R.id.monitorAlphaSeekBar)).setOnSeekBarChangeListener(onSeekBarChangeListener);
	}

	@Override
	protected void onResume() {
		super.onResume();

		profiler.getLifecycleHelper().onResume();

		ProfilerAPI.AppStatus status = profiler.getAppStatus();
		appStatusTextView.setText(status.name());
		connectButton.setEnabled(status == ProfilerAPI.AppStatus.OK);

		if (profiler.isConnected())
			updatePermissionTextView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		profiler.getLifecycleHelper().onPause();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		profiler.getLifecycleHelper().onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		profiler.getLifecycleHelper().onDestroy();
		super.onDestroy();
	}

	private void setAnchorListener(RadioGroup group, Orientation orientation, CompoundButton.OnCheckedChangeListener listener) {
		group.setTag(orientation.ordinal());

		RadioButton button = (RadioButton) group.getChildAt(0);
		button.setChecked(true);

		for (int i = 0; i < group.getChildCount(); i++)
			((RadioButton) group.getChildAt(i)).setOnCheckedChangeListener(listener);
	}

	private void printSupportedDevices() {
		DeviceConfig[] devices = profiler.getSupportedDevices();

		StringBuilder builder = new StringBuilder();
		for (DeviceConfig deviceConfig : devices)
			builder.append(deviceConfig).append('\n');

		builder.setLength(Math.max(builder.length() - 1, 0));

		devicesTextView.setText(builder.toString());
	}

	private void createAbsoluteLayout() {
		DeviceConfig[] devices = profiler.getSupportedDevices();
		int width = (int) (1 + Math.random() * 5);
		int height = (int) (1 + Math.random() * 5);

		AbsoluteLayout layout = new AbsoluteLayout(width, height);

		int count = width * height;
		for (int i = 0; i < count; i++) {
			int deviceIndex = (int) (Math.random() * devices.length);
			layout.setDevice(i % width, i / width, devices[deviceIndex]);
		}

		profiler.setLayout(layout);
	}

	private void createFloatingLayout() {
		FloatingLayout.Direction direction = floatingLayoutDirectionGroup.getCheckedRadioButtonId() == R.id.floatingDirectionRight ? FloatingLayout.Direction.RIGHT : FloatingLayout.Direction.DOWN;
		DeviceConfig[] devices = profiler.getSupportedDevices();

		FloatingLayout layout = new FloatingLayout();

		CPUDeviceConfig[] cpus = DeviceConfigHelper.findDevices(devices, CPUDeviceConfig.class);
		GPUDeviceConfig gpu = DeviceConfigHelper.findDevice(devices, GPUDeviceConfig.class);
		RAMDeviceConfig ram = DeviceConfigHelper.findDevice(devices, RAMDeviceConfig.class);

		if (cpus.length > 0)
			layout.addDevice(cpus[0]);

		if (gpu != null)
			layout.addDevice(gpu);

		if (ram != null)
			layout.addDevice(ram);

		layout.addDevice(null);

		for (int i = 1; i < cpus.length; i++)
			layout.addDevice(cpus[i]);

		int count = layout.getCount();
		int w = (int) (2 + Math.random() * 4);
		int h = count / w;

		if (w * h < count)
			h++;

		layout.pack(direction, w, h);

		profiler.setLayout(layout);
	}

	private void setPortraitLocation() {
		String xS = locationPortXField.getText().toString();
		String yS = locationPortYField.getText().toString();
		int x = xS.length() == 0 ? 0 : Integer.parseInt(xS);
		int y = yS.length() == 0 ? 0 : Integer.parseInt(yS);

		profiler.setLocation(Orientation.PORTRAIT, x, y);
	}

	private void setLandscapeLocation() {
		String xS = locationLandXField.getText().toString();
		String yS = locationLandYField.getText().toString();
		int x = xS.length() == 0 ? 0 : Integer.parseInt(xS);
		int y = yS.length() == 0 ? 0 : Integer.parseInt(yS);

		profiler.setLocation(Orientation.LANDSCAPE, x, y);
	}

	private void setChartScale() {
		String sS = sizeScaleField.getText().toString();
		float scale = sS.length() == 0 ? 0 : Float.parseFloat(sS);

		profiler.setChartScale(scale);
	}

	private void updatePermissionTextView() {
		permissionTextView.setVisibility(View.VISIBLE);
		permissionTextView.setText(profiler.hasOverlayPermission() ? "HAS OVERLAY PERMISSION" : "HAS NO OVERLAY PERMISSION");
	}

	private ProfilerEventListener profilerEventListener = new ProfilerEventListener() {

		@Override
		public void onConnected() {
			connectedTextView.setText("connected");
			updatePermissionTextView();
		}

		@Override
		public void onDisconnected() {
			connectedTextView.setText("disconnected");
			permissionTextView.setVisibility(View.GONE);
		}

		@Override
		public void onWindowSizeChanged(int width, int height) {
			sizeTextView.setText(width + " × " + height);
		}

		@Override
		public void onStateRestored() {
			connectedTextView.setText("connected");

			if (profiler.isConnected()) {
				updatePermissionTextView();

				Anchor anchorPort = profiler.getWindowAnchor(Orientation.PORTRAIT);
				RadioButton buttonPort = (RadioButton) windowAnchorPortGroup.getChildAt(anchorPort.ordinal());
				buttonPort.setChecked(true);

				Anchor anchorLand = profiler.getWindowAnchor(Orientation.LANDSCAPE);
				RadioButton buttonLand = (RadioButton) windowAnchorLandGroup.getChildAt(anchorLand.ordinal());
				buttonLand.setChecked(true);
			}
		}
	};

	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.connectButton:
					profiler.connect();
					return;
				case R.id.openGooglePlayButton:
					profiler.openGooglePlay();
					return;
			}

			if (!profiler.isConnected()) {
				Toast.makeText(MainActivity.this, "Not connected!", Toast.LENGTH_SHORT).show();
				return;
			}

			switch (v.getId()) {
				case R.id.disconnectButton:
					profiler.disconnect();
					return;
				case R.id.hideButton:
					profiler.setVisible(false, false);
					break;
				case R.id.hideAnimatedButton:
					profiler.setVisible(false, true);
					break;
				case R.id.requestPermissionButton:
					profiler.requestOverlayPermission(false);
					return;
				case R.id.requestPermissionAskButton:
					profiler.requestOverlayPermission(true);
					return;
				case R.id.createAbsoluteLayoutButton:
					createAbsoluteLayout();
					return;
				case R.id.createFloatingLayoutButton:
					createFloatingLayout();
					return;
				case R.id.clearLayoutButton:
					profiler.setLayout(null);
					return;
				case R.id.getSizeButton:
					Size size = profiler.getWindowSize();
					sizeTextView.setText(size.width + " × " + size.height);
					return;
				case R.id.getDevicesButton:
					printSupportedDevices();
					return;
				case R.id.getLocationButton:
					Point location = profiler.getCurrentLocation();
					currentLocationTextView.setText("x: " + location.x + ", y: " + location.y);
					return;
				case R.id.setLocationPortButton:
					setPortraitLocation();
					return;
				case R.id.setLocationLandButton:
					setLandscapeLocation();
					return;
				case R.id.setSizeScaleButton:
					setChartScale();
					return;
			}

			if (!profiler.hasOverlayPermission()) {
				Toast.makeText(MainActivity.this, "Profiler has no overlay permission", Toast.LENGTH_SHORT).show();
				return;
			}

			switch (v.getId()) {
				case R.id.showButton:
					profiler.setVisible(true, false);
					break;
				case R.id.showAnimatedButton:
					profiler.setVisible(true, true);
					break;
			}
		}
	};

	private CompoundButton.OnCheckedChangeListener onAnchorChangedListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton button, boolean isChecked) {
			if (!isChecked || !profiler.isConnected())
				return;

			RadioGroup group = (RadioGroup) button.getParent();
			Orientation orientation = Orientation.values()[(int) group.getTag()];
			Anchor anchor = Anchor.values()[Integer.parseInt((String) button.getTag())];

			profiler.setWindowAnchor(orientation, anchor);
		}
	};

	private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

		private int lastProgress;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (!profiler.isConnected())
				return;

			switch (seekBar.getId()) {
				case R.id.backgroundAlphaSeekBar:
					profiler.setBackgroundAlpha((float) progress / seekBar.getMax());
					break;
				case R.id.monitorAlphaSeekBar:
					profiler.setWindowAlpha((float) progress / seekBar.getMax());
					break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			lastProgress = seekBar.getProgress();
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (!profiler.isConnected()) {
				seekBar.setProgress(lastProgress);

				Toast.makeText(MainActivity.this, "Not connected!", Toast.LENGTH_SHORT).show();
			}
		}
	};
}