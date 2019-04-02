#!/bin/bash

t="timeout 300"
output_folder="results1/tutor1base/"
count=0
stop1=$1
stop2=$2
for filename in tests/clean_AutoTutor/*; do
  #echo $filename
  #echo $output_folder${filename##*/}
  count=$((count+1))
  if [ "$count" -lt "$stop1" ]; then
    continue
  elif [ "$count" -gt "$stop2" ]; then
    continue
  else
    $t java -jar target/regfixer.jar --mode 1 -base fix --file $filename > $output_folder${filename##*/}
    #echo $count
  fi
done
