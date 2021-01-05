# Rasprem - Raspberry Remote

Made and tested with Raspberry pi zero W

Always wanted access to a network without sitting there with your laptop the whole day. Then this project is just for you!

You can configure the pi through an Wireless Access Point with the name configured.
Setup the pi through a web interface.
Leave the pi in the targeted network and use the pi as connection to the network.

# Note

The project is currently on hold. Due to me having to work on other projects. Might revisit this project in the future.

TODO:

- The complete remsetup might not work fully. (Not well tested)
- The web interface to connect to a network does not copy to the pi yet.
- The web interface is not accessable through the pi yet.
- Lower power mode has not been implemented.
- A obscured reverse shell has not been setup.

# Setup

## Requirements

- ssh
- sshpass

## Boot Partition

Before plugging in the sd card run this script so the setup script can be run later. This is used to setup the pi through your own network

```sh
./boot.sh {SSID} {SSID_PASS} {DEVICE}
```

SSID: Your wireless network name.
SSID_PASS: Your plain wireless network password
DEVICE: What device is your boot paritition? (eg. /dev/sdd1)

## Command

```sh
ssh pi@192.168.2.7 "sudo bash -s" -- < ./setup.sh {AP_SSID} {AP_PASS}
```

AP_SSID: The name for the AP (eg. `RaspRem`)
AP_PASS: The password used to connect to the AP and for the `rem` user.(eg. `ThisIsAPasswordForRemoteAccess`)
