# Rasprem - Raspberry Remote

Made and tested for Raspberry pi zero W

Setup an Access Point to configure and setup the pi through a web interface.

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
