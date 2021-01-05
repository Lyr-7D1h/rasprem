#!/bin/bash
# Required sshpass
# Connect to remote and execute setup

# Error handling
set -e # Exit immediatly when command fails
set -u # Exit when unset paramter is extended

# Input validation
if [ $# != 3 ]; then
  echo "./setup {IP} {AP_SSID} {AP_PASS}"
  exit 1
fi

if [ ${#1} -lt 10 ]; then
  echo "IP needs to be larger than 10"
  exit 1
fi
if [ ${#2} -lt 5 ]; then
  echo "AP_SSID needs to be larger than 5"
  exit 1
fi
if [ ${#3} -lt 8 ]; then
  echo "AP_PASS needs to be larger than 8"
  exit 1
fi

IP=$1
AP_SSID=$2
AP_PASS=$3

sshpass -p "raspberry" ssh -oStrictHostKeyChecking=no pi@$IP "sudo bash -s" -- < setup.sh $AP_SSID $AP_PASS