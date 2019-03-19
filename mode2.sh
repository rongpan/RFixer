#!/bin/bash

t="timeout 300"
output_folder="results1/mode2/"
for filename in tests/benchmark_explicit/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t java -jar target/regfixer.jar --mode 2 fix --file $filename > $output_folder${filename##*/}
done
