Simple single activity application and a service to control my BLE light bulb.

A com.android.deskclock.ALARM_ALERT intent is handled to automatically connect to the light bulb and initiate a "sunrise" effect when my alarm goes off in the morning.

Main classes:
- BulbBluetoothConnection class does the low level plumbing of establishing a connection and sending bytes to the device.
- BulbController wraps it to provide an api for turning the light bulb on/off and setting the light level to a desired value. It also handles queuing of the commands so that the caller does not have to wait for all the previous commands to finish. 
- BulbAnimator uses a Handler and a BulbController to smoothly change the light level over a specified duration.
- LightBulbService handles intents to delegate to the BulbController and manages the notifications displayed when the light bulb is being controlled.

The API of the lighbulb was reverse engineered by (slightly) adapting the guide on (adafruit.com)[https://learn.adafruit.com/reverse-engineering-a-bluetooth-low-energy-light-bulb/overview] for my specific light bulb.

![Activity](meta/activity.png?raw=true "Activity")
![Notification](meta/notification.png?raw=true "Notification")

Material icons under the [CC-BY](https://creativecommons.org/licenses/by/4.0/) license.

Application icon generated with Android Asset Studio, licensed under [Creative Commons Attribution 3.0 Unported License](http://creativecommons.org/licenses/by/3.0/)


The MIT License (MIT)

Copyright (c) 2016 Marcin Oczeretko

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.