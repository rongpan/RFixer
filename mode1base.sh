#!/bin/bash

t="timeout 300"
output_folder="results1/mode1base/"
for filename in tests/benchmark_explicit/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t java -jar target/regfixer.jar --mode 1 -base fix --file $filename > $output_folder${filename##*/}
done
