# SmartThings-HTTP-Online-Sensor
A SmartThings device handler that regularly GETs an HTTP endpoint, including local network addresses.  It reveals a status of OFFLINE or ONLINE.

## Background

While my home automation is done through SmartThings, I like Apple HomeKit as a usable dashboard.
Therefore I have a Raspberry Pi running HomeBridge, and exposing all my SmartThings devices to HomeKit.
The problems were that:
- The Raspberry Pi might lose wifi
- When my router rebooted, the Pi didn't always reconnect
- Sometimes HomeBridge itself would hang, breaking my HomeKit dashboard.

The easiest fix for all of this is just a reboot of the Raspberry Pi.

### Solution:
1) Plug the Pi into a smart z-wave plug/outlet.
2) Create a rule in CoRE (or WebCoRE or whatever rules engine you use) to cycle the Pi's power if it is detected that the Pi is offline
3) Create a Device Handler type that could detect if the Pi was offline.

Next problem:  You can't do a real PING of a local network device from a SmartThings hub.  You can only do simple HTTP GET requests.  However, HomeBridge (and specifically the SmartThings plugin for it) exposed a very simple endpoint at port 8000 that would just return OK if you get it.  So that would work for a ping-substitute.  The SmartThings hub just needs to regularly GET http://192.168.1.xxx:8000.  If a couple requests are missed in a row, consider it offline.

So that's the reasoning behind this device handler.  Hopefully it will help your home automation solutions too.

## Installation

This handler supports SmartThings/GitHub integration.

1) Log in to https://account.smartthings.com
2) Click "My Device Handlers"
3) Click "Settings"
4) Add a new Git repo:
  a) Owner: joelwetzel
  b) Name: SmartThings-HTTP-Online-Sensor
  c) Branch: master
  d) Click "Save"
5) Click Update from Repo
6) Click SmartThings-HTTP-Online-Sensor
7) Check the http-online-sensor.groovy file
8) Check "Publish"
9) Click "Execute Update"

Now the device handler should be installed.  Next, you have to add and configure the device itself.

1) Click "My Devices"
2) Click "+ New Device"
3) Enter "Name"
4) Enter "Device Network Id".  Use your desired IP address and port.  Example: 192.168.1.91:8000
5) Set "Type" to "HTTP Online Sensor"
6) Set Location
7) Set Hub
8) Click Create

Final step:  The Device Network Id actually needs to be hex encoded.  To find and set this encoding:
1) Click "Live Logging"
2) Watch for an error message from the device you just added.  It will say that you need to use the HEX encoded value and show it.
3) Copy the HEX encoded value
4) Go back to the edit screen for your device and paste the HEX encoded value as the new "Device Network Id"

Now, if you go back to live logging, you should see messages about your device being ONLINE or OFFLINE, depending on if it can be reached.  Your device will show up in the SmartThings mobile apps and you'll be able to run automation based on its state.

Note that the status values to trigger off of are "ONLINE" and "OFFLINE".  

