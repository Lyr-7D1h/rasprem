#!/bin/bash
# Setup Access Point
# Mimic samsung phone
# Creates 'rem' user with AP_PASS
# Removes default pi
# https://github.com/lukicdarkoo/rpi-wifi/blob/master/configure
# https://www.raspberrypi.org/documentation/configuration/wireless/access-point-routed.md

# Error handling
set -e # Exit immediatly when command fails
set -u # Exit when unset paramter is extended

# Input validation
if [ $# != 2 ]; then
  echo "./setup {AP_SSID} {AP_PASS}"
  exit 1
fi
if [ ${#1} -lt 5 ]; then
  echo "AP_SSID needs to be larger than 5"
  exit 1
fi
if [ ${#2} -lt 8 ]; then
  echo "AP_PASS needs to be larger than 8"
  exit 1
fi

AP_SSID=$1
AP_PASS=$2

AP_IP='192.168.10.1'
AP_IP_BEGIN=`echo "${AP_IP}" | sed -e 's/\.[0-9]\{1,3\}$//g'`
#TODO: use randomized mac
MAC_ADDRESS="$(cat /sys/class/net/wlan0/address)"

# Prevent any interactive installs from blocking setup
export DEBIAN_FRONTEND=noninteractive 

# Update machine
apt-get update && apt-get upgrade -y

# Install needed packages
apt-get install -y macchanger dnsmasq hostapd

# Not sure if needed
# From https://www.raspberrypi.org/documentation/configuration/wireless/access-point-routed.md
# apt-get install -y netfilter-persistent iptables-persistent

# Change hostname
echo "Galaxy S10+" > /etc/hostname

# Populate `/etc/udev/rules.d/70-persistent-net.rules`
# TODO: might not be needed
# TODO: should work with macchanger
bash -c 'cat > /etc/udev/rules.d/70-persistent-net.rules' << EOF
SUBSYSTEM=="ieee80211", ACTION=="add|change", ATTR{macaddress}=="${MAC_ADDRESS}", KERNEL=="phy0", \
 RUN+="/sbin/iw phy phy0 interface add ap0 type __ap", \
 RUN+="/bin/ip link set ap0 address ${MAC_ADDRESS}
EOF

# Populate `/etc/dnsmasq.conf`
# TODO: look more into dnsmasq
sudo bash -c 'cat > /etc/dnsmasq.conf' << EOF
interface=lo,ap0
no-dhcp-interface=lo,wlan0
bind-interfaces
server=8.8.8.8
domain-needed
bogus-priv
dhcp-range=${AP_IP_BEGIN}.50,${AP_IP_BEGIN}.150,12h
EOF

# Populate `/etc/network/interfaces`
bash -c 'cat > /etc/network/interfaces' << EOF
source-directory /etc/network/interfaces.d
auto lo
auto ap0
auto wlan0
iface lo inet loopback
allow-hotplug ap0
iface ap0 inet static
   address ${AP_IP}
   netmask 255.255.255.0
   hostapd /etc/hostapd/hostapd.conf
allow-hotplug wlan0
iface wlan0 inet manual
   wpa-roam /etc/wpa_supplicant/wpa_supplicant.conf
iface AP1 inet dhcp
EOF

# Setup Hostapd
systemctl unmask hostapd
systemctl enable hostapd
echo -n "
country_code=NL
interface=wlan0
ssid=$AP_SSID
hw_mode=g
channel=7
macaddr_acl=0
auth_algs=1
ignore_broadcast_ssid=0
wpa=2
wpa_passphrase=$AP_PASS
wpa_key_mgmt=WPA-PSK
wpa_pairwise=TKIP
rsn_pairwise=CCMP
" >> /etc/hostapd/hostapd.conf
bash -c 'cat > /etc/default/hostapd' << EOF
DAEMON_CONF="/etc/hostapd/hostapd.conf"
EOF

# Make sure Wifi radio is not blocked
rfkill unblock wlan

# Create rem user
PASS_CRYPT=`perl -e "print crypt($AP_PASS,'sa');"`
useradd -m -p $PASS_CRYPT rem
usermod -aG sudo rem
# Remove default user
userdel -rf pi

# Populate `/bin/start_wifi.sh`
#TODO: remove routing of internet
#TODO: macchanger on startup
bash -c 'cat > /bin/rpi-wifi.sh' << EOF
echo 'Starting Wifi AP and client...'
sleep 30
sudo ifdown --force wlan0
sudo ifdown --force ap0
sudo ifup ap0
sudo ifup wlan0
$([ "${NO_INTERNET-}" != "true" ] && echo "sudo sysctl -w net.ipv4.ip_forward=1")
$([ "${NO_INTERNET-}" != "true" ] && echo "sudo iptables -t nat -A POSTROUTING -s ${AP_IP_BEGIN}.0/24 ! -d ${AP_IP_BEGIN}.0/24 -j MASQUERADE")
$([ "${NO_INTERNET-}" != "true" ] && echo "sudo systemctl restart dnsmasq")
EOF
chmod +x /bin/rpi-wifi.sh

# Set crontab for rem
# su - rem
crontab -l | { cat; echo "@reboot /bin/rpi-wifi.sh"; } | crontab -
# exit

# Samsung Electronics Co.,Ltd (BREAKS CONNECTION)
# TODO: Randomize last bit
# macchanger -m 50:85:69:63:13:82 wlan0 

# Reboot
systemctl reboot --now