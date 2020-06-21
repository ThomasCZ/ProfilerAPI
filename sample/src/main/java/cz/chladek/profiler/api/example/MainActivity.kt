package cz.chladek.profiler.api.example

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.view.children
import cz.chladek.profiler.api.ProfilerAPI
import cz.chladek.profiler.api.ProfilerAPI.AppStatus
import cz.chladek.profiler.api.ProfilerEventListener
import cz.chladek.profiler.api.devices.CPUDeviceConfig
import cz.chladek.profiler.api.devices.GPUDeviceConfig
import cz.chladek.profiler.api.devices.RAMDeviceConfig
import cz.chladek.profiler.api.example.databinding.AnchorBinding
import cz.chladek.profiler.api.example.databinding.MainBinding
import cz.chladek.profiler.api.example.extensions.findDevice
import cz.chladek.profiler.api.example.extensions.findDevices
import cz.chladek.profiler.api.example.extensions.plusAssign
import cz.chladek.profiler.api.example.extensions.set
import cz.chladek.profiler.api.layout.AbsoluteLayout
import cz.chladek.profiler.api.layout.FloatingLayout
import cz.chladek.profiler.api.utils.Anchor
import cz.chladek.profiler.api.utils.Orientation

class MainActivity : Activity() {

	private val binding: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
	private val profiler: ProfilerAPI by lazy { ProfilerAPI(this) }

	private var notConnectedToast: Toast? = null
	private var noPermissionToast: Toast? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		profiler.listener = profilerEventListener
		profiler.lifecycleHelper.onCreate(savedInstanceState)

		binding.connect.setOnClickListener { profiler.connect() }
		binding.disconnect.setOnClickListener { whenConnected { profiler.disconnect() } }
		binding.show.setOnClickListener { whenConnected { whenHasOverlayPermission { profiler.setVisible(true, false) } } }
		binding.showAnimated.setOnClickListener { whenConnected { whenHasOverlayPermission { profiler.isVisible = true } } }
		binding.hide.setOnClickListener { whenConnected { profiler.setVisible(false, false) } }
		binding.hideAnimated.setOnClickListener { whenConnected { profiler.isVisible = false } }
		binding.openGooglePlay.setOnClickListener { profiler.openGooglePlay() }
		binding.requestPermission.setOnClickListener { whenConnected { profiler.requestOverlayPermission(false) } }
		binding.requestPermissionAsk.setOnClickListener { whenConnected { profiler.requestOverlayPermission(true) } }
		binding.createAbsoluteLayout.setOnClickListener { whenConnected { createAbsoluteLayout() } }
		binding.createFloatingLayout.setOnClickListener { whenConnected { createFloatingLayout() } }
		binding.clearLayout.setOnClickListener { whenConnected { profiler.layout = null } }
		binding.getSupportedDevices.setOnClickListener { whenConnected { binding.devicesList.text = profiler.supportedDevices.joinToString("\n") } }
		binding.getLocation.setOnClickListener { whenConnected { binding.currentLocation.text = profiler.currentLocation.run { "x: $x, y: $y" } } }
		binding.setPortLocation.setOnClickListener { whenConnected { updatePortraitLocation() } }
		binding.setLandLocation.setOnClickListener { whenConnected { updateLandscapeLocation() } }
		binding.setChartSizeScale.setOnClickListener { whenConnected { profiler.setChartScale(binding.chartSizeScale.text.toString().toFloatOrNull() ?: 0f) } }
		binding.getWindowSize.setOnClickListener { whenConnected { binding.windowSize.text = profiler.windowSize.run { "$width × $height" } } }

		binding.anchorPortGroup.setAnchorChangeListener(Orientation.PORTRAIT)
		binding.anchorLandGroup.setAnchorChangeListener(Orientation.LANDSCAPE)

		binding.backgroundAlpha.setOnSeekBarChangeListener(ProfilerSeekBarChangeListener { profiler.setBackgroundAlpha(it) })
		binding.windowAlpha.setOnSeekBarChangeListener(ProfilerSeekBarChangeListener { profiler.setWindowAlpha(it) })
	}

	override fun onResume() {
		super.onResume()
		profiler.lifecycleHelper.onResume()

		val status = profiler.appStatus
		binding.appStatus.text = status.name
		binding.connect.isEnabled = status == AppStatus.OK

		if (profiler.isConnected)
			updatePermissionTextView()
	}

	override fun onPause() {
		super.onPause()
		profiler.lifecycleHelper.onPause()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		profiler.lifecycleHelper.onSaveInstanceState(outState)
	}

	override fun onDestroy() {
		profiler.lifecycleHelper.onDestroy()
		super.onDestroy()
	}

	private fun AnchorBinding.setAnchorChangeListener(orientation: Orientation) {
		val onChangeListener = CompoundButton.OnCheckedChangeListener { button, isChecked ->
			if (isChecked)
				whenConnected {
					val anchor = Anchor.values()[(button.tag as String).toInt()]
					profiler.setWindowAnchor(orientation, anchor)
				}
		}

		for (child in root.children)
			(child as RadioButton).setOnCheckedChangeListener(onChangeListener)
	}

	private fun createAbsoluteLayout() {
		val width = (1 + Math.random() * 5).toInt()
		val height = (1 + Math.random() * 5).toInt()

		val layout = AbsoluteLayout(width, height)
		val devices = profiler.supportedDevices

		for (i in 0 until width * height) {
			val deviceIndex = (Math.random() * devices.size).toInt()
			layout[i % width, i / width] = devices[deviceIndex]
		}

		profiler.layout = layout
	}

	private fun createFloatingLayout() {
		val devices = profiler.supportedDevices
		val layout = FloatingLayout()

		devices.findDevices(CPUDeviceConfig::class).firstOrNull()?.let { layout += it }
		devices.findDevice(GPUDeviceConfig::class)?.let { layout += it }
		devices.findDevice(RAMDeviceConfig::class)?.let { layout += it }

		layout += null

		val cpus = devices.findDevices(CPUDeviceConfig::class)
		for (i in 1 until cpus.size)
			layout += cpus[i]

		val width = (2 + Math.random() * 4).toInt()
		var height = layout.count / width

		if (width * height < layout.count)
			height++

		val direction = if (binding.floatingLayoutDirectionGroup.checkedRadioButtonId == R.id.floating_direction_right) FloatingLayout.Direction.RIGHT else FloatingLayout.Direction.DOWN
		layout.pack(direction, width, height)

		profiler.layout = layout
	}

	private fun updatePortraitLocation() {
		val x = binding.locationPortX.text.toString().toIntOrNull() ?: 0
		val y = binding.locationPortY.text.toString().toIntOrNull() ?: 0
		profiler.setLocation(Orientation.PORTRAIT, x, y)
	}

	private fun updateLandscapeLocation() {
		val x = binding.locationLandX.text.toString().toIntOrNull() ?: 0
		val y = binding.locationLandY.text.toString().toIntOrNull() ?: 0
		profiler.setLocation(Orientation.LANDSCAPE, x, y)
	}

	private fun updatePermissionTextView() {
		binding.profilerPermission.visibility = View.VISIBLE
		binding.profilerPermission.text = if (profiler.hasOverlayPermission()) "HAS OVERLAY PERMISSION" else "HAS NO OVERLAY PERMISSION"
	}

	private fun whenConnected(action: () -> Unit) {
		if (profiler.isConnected)
			action()
		else
			showNotConnectedToast()
	}

	private fun whenHasOverlayPermission(action: () -> Unit) {
		if (profiler.hasOverlayPermission())
			action()
		else
			showNoPermissionToast()
	}

	private fun showNotConnectedToast() {
		notConnectedToast?.cancel()
		notConnectedToast = Toast.makeText(this, "Profiler is not connected", Toast.LENGTH_SHORT).apply { show() }
	}

	private fun showNoPermissionToast() {
		noPermissionToast?.cancel()
		noPermissionToast = Toast.makeText(this, "Profiler has no overlay permission", Toast.LENGTH_SHORT).apply { show() }
	}

	private val profilerEventListener = object : ProfilerEventListener {
		override fun onConnected() {
			binding.connectStatus.text = "connected"
			updatePermissionTextView()
		}

		override fun onDisconnected() {
			binding.connectStatus.text = "disconnected"
			binding.profilerPermission.visibility = View.GONE
		}

		override fun onWindowSizeChanged(width: Int, height: Int) {
			binding.windowSize.text = "$width × $height"
		}

		override fun onStateRestored() {
			binding.connectStatus.text = "connected"

			if (profiler.isConnected) {
				updatePermissionTextView()

				val anchorPort = profiler.getWindowAnchor(Orientation.PORTRAIT)
				binding.anchorPortGroup.get(anchorPort).isChecked = true

				val anchorLand = profiler.getWindowAnchor(Orientation.LANDSCAPE)
				binding.anchorLandGroup.get(anchorLand).isChecked = true
			}
		}

		private fun AnchorBinding.get(anchor: Anchor): RadioButton = root.getChildAt(anchor.ordinal) as RadioButton
	}

	private inner class ProfilerSeekBarChangeListener(private val onChanged: (Float) -> Unit) : OnSeekBarChangeListener {
		private var lastProgress = 0

		override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
			if (!profiler.isConnected)
				return

			onChanged(progress.toFloat() / seekBar.max)
		}

		override fun onStartTrackingTouch(seekBar: SeekBar) {
			lastProgress = seekBar.progress
		}

		override fun onStopTrackingTouch(seekBar: SeekBar) {
			if (!profiler.isConnected) {
				seekBar.progress = lastProgress
				showNotConnectedToast()
			}
		}
	}
}