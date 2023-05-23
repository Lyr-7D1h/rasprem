#!/usr/bin/env bash
#
# Setup sd card with all needed software
#

set -eu

if [[ ! -f /usr/bin/qemu-arm-static ]]; then
  echo "Be sure to have installed requirements: qemu qemu-user-static-binfmt"
  exit 1
fi

OS_DOWNLOAD_LINK="https://downloads.raspberrypi.org/raspios_lite_armhf_latest"

help() {
  echo "./boot {DEVICE} {SSID} {PASS}"
  echo ""
  echo "Example:"
  echo './boot /dev/sdd "Lyrs Monitor" asdfasdfasdf'
  exit 1
}
if [ $# != 3 ]; then
  help
fi
if [ ${#2} -lt 5 ]; then
  echo "SSID needs to be larger than 5"
  help
fi
if [ ${#3} -lt 8 ]; then
  echo "PASS needs to be larger than 8"
  help
fi

DEVICE=$1
SSID=$2
PASS=$3

echo "Going to directory of script"
cd "$(dirname "$0")"

echo "Making temp folder"
CWD="/tmp/rasprem" 
# CWD=`mktemp -d`
mkdir -p $CWD

echo "Moving files to $CWD"
cp ./setup $CWD 
cp -r ./web $CWD

echo "Moving into temporary directory $CWD"
cd $CWD 

if [[ ! -f raspios_lite_armhf_latest ]]; then
  echo "Downloading image"
  wget $OS_DOWNLOAD_LINK --output-document=raspios_lite_armhf_latest.xz
  echo Unpacking raspios_lite_armhf_latest.xz
  unxz raspios_lite_armhf_latest.xz
fi

echo "Writing image to $DEVICE"
sudo dd bs=4M if=./raspios_lite_armhf_latest of=$DEVICE conv=fsync oflag=direct status=progress
sync

MOUNT="$CWD/mount"

if [[ -d $MOUNT ]]; then
  sudo umount -R $MOUNT
  sudo rm -rf $MOUNT 
fi

mkdir -p $MOUNT


#
# Setup chroot https://gist.github.com/htruong/7df502fb60268eeee5bca21ef3e436eb
#
echo Mounting $DEVICE to $MOUNT
sudo mount -o rw ${DEVICE}2  $MOUNT
sudo mount -o rw ${DEVICE}1 $MOUNT/boot

# mount binds
sudo mount --bind /dev $MOUNT/dev/
sudo mount --bind /sys $MOUNT/sys/
sudo mount --bind /proc $MOUNT/proc/
sudo mount --bind /dev/pts $MOUNT/dev/pts


#FIXME check for default settings
# echo "Enabling peripherals"
# echo "dtparam=spi=on" > /boot/config.txt

echo Adding remsetup executable
sudo mv $CWD/setup $MOUNT/usr/bin/remsetup

echo Adding web interface
sudo mkdir -p $MOUNT/var/www/html/
sudo mv $CWD/web $MOUNT/var/www/html/

echo Enabling remote ssh
sudo touch $MOUNT/boot/ssh

echo -n "ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1
country=NL

network={
 ssid=\"401 Unauthorized\"
 psk=\"r8g9hghnybza86r\"
}
" | sudo tee $MOUNT/boot/wpa_supplicant.conf

# ld.so.preload fix
sudo sed -i 's/^/#CHROOT /g' $MOUNT/etc/ld.so.preload

# copy qemu binary
sudo cp /usr/bin/qemu-arm-static $MOUNT/usr/bin/

# chroot to raspbian
echo "Chrooting into $MOUNT and executing 'remsetup "$SSID" "$PASS"'"
sudo chroot $MOUNT /bin/bash -x <<EOF
/usr/bin/remsetup "$SSID" "$PASS"
EOF

# revert ld.so.preload fix
sudo sed -i 's/^#CHROOT //g' $MOUNT/etc/ld.so.preload

# unmount everything
echo Unmounting tmp mount
sudo umount $MOUNT/{dev/pts,dev,sys,proc,boot,} 

echo Cleaning up temporary file
rm -rf $MOUNT
rm -rf $CWD
