# ProfilerAPI

Lightweight API that allows integrate [Profiler](https://play.google.com/store/apps/details?id=cz.chladek.profiler) functionality into your application. Library mediates communication with the Profiler application.

#### Profiler application features
* Real-time component monitoring in floating window.
* Allows to monitor following components (depends on data provided by a device). Check compatibility list [here](https://profiler.chladektomas.eu/app/devices).
    * CPU load and frequency
    * GPU load
    * RAM usage
    * Network traffic
    * Temperature
    * Battery voltage, temperature and current

#### API allows to set
* custom layout built by AbsoluteLayout or FloatingLayout
* labels, formats and colors
* anchor of window transitions
* specific location in portrait and landscape
* chart size
* window and background alpha

#### Sample application

The sample application uses all features allowed by the API.

[![Screenshot1](http://postimg.cz/images/xWMNt.png)](http://postimg.cz/images/xWj64.png) ![](http://postimg.cz/images/xWv2B.png) [![Screenshot2](http://postimg.cz/images/xWSPY.png)](http://postimg.cz/images/xWqdJ.png) ![](http://postimg.cz/images/xWv2B.png) [![Screenshot3](http://postimg.cz/images/xWiQa.png)](http://postimg.cz/images/xWaKi.png)

[![Get it on Google Play](http://www.postimg.cz/images/xW3yA.png)](https://play.google.com/store/apps/details?id=cz.chladek.profiler.api.example)

## Quick start

**1)** Add maven repository into root *build.gradle* file.

```gradle
maven {
    url "https://dl.bintray.com/chladektomas/maven"
}
```

**2)** Add this library as a dependency in your application's *build.gradle* file.

```gradle
dependencies {
    implementation 'cz.chladek:profiler-api:1.2.0'
}
```

**3)** Initialize Profiler, create layout and show window.

```java
final ProfilerAPI profiler = new ProfilerAPI(context);
profiler.setListener(new ProfilerEventListenerAdapter() {

    @Override
    public void onConnected() {
        DeviceConfig[] devices = profiler.getSupportedDevices();
        FloatingLayout layout = new FloatingLayout();

        CPUDeviceConfig[] cpuLoad = DeviceConfigHelper.findDevices(devices, CPUDeviceConfig.class, device -> device.getMode() == Mode.LOAD);
        GPUDeviceConfig gpu = DeviceConfigHelper.findDevice(devices, GPUDeviceConfig.class);
        RAMDeviceConfig ram = DeviceConfigHelper.findDevice(devices, RAMDeviceConfig.class);

        for (CPUDeviceConfig cpu : cpuLoad)
            layout.addDevice(cpu);

        if (gpu != null)
            layout.addDevice(gpu);

        if (ram != null)
            layout.addDevice(ram);

        layout.pack(Direction.RIGHT, 4, 3);

        profiler.setLayout(layout);
        profiler.setChartScale(0.75f);
        profiler.setWindowAnchor(Orientation.LANDSCAPE, Anchor.TOP_LEFT);
        profiler.setLocation(Orientation.LANDSCAPE, 50, 50);
        profiler.setBackgroundAlpha(0.4f);

        profiler.setVisible(true, true);
    }
});
profiler.connect();
```

**4)** Handle Activity or Fragment lifecycle. See the sample application sources for more details.

```java
@Override
protected void onResume() {
    super.onResume();
    profiler.getLifecycleHelper().onResume();
}

...
```

## About

Copyright 2018 Tomas Chladek, licenced under the [Apache Licence, Version 2.0](LICENCE.txt).