# RaspRem - Raspberry Remote

Made and tested with Raspberry pi zero W

Always wanted access to a network without sitting there with your laptop the whole day. Then this project is just for you!

You can configure the pi through an Wireless Access Point. Here you fill in the credentials you want the pi to connect  to .
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

## Usage

You can either setup the bootable image locally using chroot and downloading raspios from the web or you can copy `./setup` to your raspberry and execute it on the device.

### Making a bootable image

```sh
./boot.sh {DEVICE} {SSID} {SSID_PASS} 
```

DEVICE: What device is your sd card on? (eg. /dev/sdd) 

SSID: Your wireless network name.

SSID_PASS: Your plain wireless network password

### Setup script

```sh
./setup {SSID} {SSID_PASS} 
```

SSID: The name of the access point

PASS: The password of the access point


### Roadmap

- Remote pi hole
