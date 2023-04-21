#!/usr/bin/env bash
#
# Setup sd card with all needed software
#

set -eu

echo "Be sure to have installed requirements: qemu qemu-user-static binfmt-support"
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
PWD=`mktemp -d`

echo "Moving files to $PWD"
cp ./setup $PWD 
cp -r ./web $PWD

echo "Moving into temporary directory $PWD"
cd $PWD 

echo "Downloading image"
wget $OS_DOWNLOAD_LINK --output-document=raspios_lite_armhf_latest.xz
echo Unpacking raspios_lite_armhf_latest.xz
unxz raspios_lite_armhf_latest.xz
echo "Writing image to $DEVICE"
sudo dd bs=4M if=./raspios_lite_armhf_latest of=$DEVICE conv=fsync oflag=direct status=progress
sync

MOUNT="$PWD/mount"
mkdir -p $MOUNT


#
# Setup chroot https://gist.github.com/htruong/7df502fb60268eeee5bca21ef3e436eb
#
echo Mounting $DEVICE to $MOUNT
sudo mount -o rw ${DEVICE}2  $MOUNT
sudo mount -o rw ${DEVICE}1 $MOUNT/boot

echo Setting owner of $MOUNT to $USER
sudo chown -R $USER:$USER $MOUNT

# mount binds
sudo mount --bind /dev $MOUNT/dev/
sudo mount --bind /sys $MOUNT/sys/
sudo mount --bind /proc $MOUNT/proc/
sudo mount --bind /dev/pts $MOUNT/dev/pts

echo Adding remsetup executable
mv $PWD/setup $MOUNT/usr/bin/remsetup

echo Enabling remote ssh
touch $MOUNT/boot/ssh

# ld.so.preload fix
sed -i 's/^/#CHROOT /g' $MOUNT/etc/ld.so.preload

# copy qemu binary
cp /usr/bin/qemu-arm-static $MOUNT/usr/bin/

# chroot to raspbian
echo "Chrooting into $MOUNT and executing 'remsetup $SSID $PASS'"
chroot $MOUNT /bin/bash -x <<EOF
/usr/bin/remsetup "$SSID" "$PASS"
EOF

# ----------------------------
# Clean up
# revert ld.so.preload fix
sed -i 's/^#CHROOT //g' $MOUNT/etc/ld.so.preload

# unmount everything
umount $MOUNT/{dev/pts,dev,sys,proc,boot,} 

# echo -n "ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
# update_config=1
# country=NL
#
# network={
#  ssid=\"$1\"
#  psk=\"$2\"
# }
# " > $MOUNT/boot/wpa_supplicant.conf



echo Unmounting tmp mount
umount -R $MOUNT

echo Cleaning up temporary file
rm -rf $MOUNT