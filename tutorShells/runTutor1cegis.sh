#!/bin/bash

t="timeout 300"
output_folder="results1/tutor1cegis/"
count=0
stop1=$1
stop2=$2
for filename in tests/clean_AutoTutorWithTrue/*; do
  #echo $filename
  #echo $output_folder${filename##*/}
  count=$((count+1))
  if [ "$count" -lt "$stop1" ]; then
    continue
  elif [ "$count" -gt "$stop2" ]; then
    continue
  else
    $t java -jar target/regfixer.jar --mode 1 -c -t fix --file $filename > $output_folder${filename##*/}
    #echo $count
  fi
done
