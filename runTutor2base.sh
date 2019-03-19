#!/bin/bash

t="timeout 300"
output_folder="results1/tutor2base/"
for filename in tests/clean_AutoTutor/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t java -jar target/regfixer.jar --mode 2 -base fix --file $filename > $output_folder${filename##*/}
done
