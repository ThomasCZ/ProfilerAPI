package cz.chladek.profiler.api.example.extensions

import cz.chladek.profiler.api.devices.DeviceConfig
import cz.chladek.profiler.api.layout.AbsoluteLayout
import cz.chladek.profiler.api.layout.FloatingLayout
import cz.chladek.profiler.api.utils.DeviceConfigHelper
import kotlin.reflect.KClass

operator fun FloatingLayout.plusAssign(device: DeviceConfig?) {
	addDevice(device)
}

operator fun AbsoluteLayout.set(x: Int, y: Int, device: DeviceConfig) {
	setDevice(x, y, device)
}

fun <T : DeviceConfig> Array<DeviceConfig>.findDevice(clazz: KClass<T>): T? {
	return DeviceConfigHelper.findDevice(this, clazz.java)
}

fun <T : DeviceConfig> Array<DeviceConfig>.findDevices(clazz: KClass<T>, predicate: ((T) -> Boolean)? = null): Array<T> {
	return DeviceConfigHelper.findDevices(this, clazz.java, predicate)
}