#!/bin/bash

program=$1

for cluster in "c0-0 0" "c0-1 20" "c0-2 40" "c0-3 60" "c0-4 80" "c0-5 100"
do
  set -- $cluster
  ssh $1 "nohup ./runCommunication.sh $program $2 2> 2_$2.log 1> 2_$2_1.log < /dev/null &"
  echo "assigned to $1"
done
