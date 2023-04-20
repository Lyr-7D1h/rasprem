#!/bin/bash

set -e 
set -u 

if [ $# != 3 ]; then
  echo "./boot {SSID} {SSID_PASS} {DEVICE}"
  exit 1
fi
SSID=$1
PASS=$2
DEVICE=$3

MOUNT="/tmp/rasprem/boot"

mkdir -p $MOUNT
mount $DEVICE $MOUNT

echo -n "ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1
country=NL

network={
 ssid=\"$1\"
 psk=\"$2\"
}
" > $MOUNT/wpa_supplicant.conf

touch $MOUNT/ssh

umount $MOUNT