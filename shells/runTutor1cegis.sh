#!/bin/bash

t="timeout 300"
output_folder="results1/tutor1cegis/"
for filename in tests/clean_AutoTutorWithTrue/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t java -jar target/regfixer.jar --mode 1 -c -t fix --file $filename > $output_folder${filename##*/}
done
