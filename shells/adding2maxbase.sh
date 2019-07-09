#!/bin/bash

t="timeout 300"
output_folder="results5/adding2maxbase/"
dir="src/test/resources/dataset/"
run="java -jar targetRe/regfixer.jar"
mode="--mode 2 -max -base"

for filename in "$dir"YAGO-date/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t $run $mode fix --file $filename --testfile "$dir"YAGO-date.test > "$output_folder"YAGO-date${filename##*/}
done

for filename in "$dir"YAGO-number/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t $run $mode fix --file $filename --testfile "$dir"YAGO-number.test > "$output_folder"YAGO-number${filename##*/}
done

for filename in "$dir"enron-date/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t $run $mode fix --file $filename --testfile "$dir"enron-date.test > "$output_folder"enron-date${filename##*/}
done

for filename in "$dir"enron-phone/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t $run $mode fix --file $filename --testfile "$dir"enron-phone.test > "$output_folder"enron-phone${filename##*/}
done

for filename in "$dir"relie-coursenum/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t $run $mode fix --file $filename --testfile "$dir"relie-coursenum.test > "$output_folder"relie-coursenum${filename##*/}
done

for filename in "$dir"relie-phonenum/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t $run $mode fix --file $filename --testfile "$dir"relie-phonenum.test > "$output_folder"relie-phonenum${filename##*/}
done

for filename in "$dir"relie-softwarename/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t $run $mode fix --file $filename --testfile "$dir"relie-softwarename.test > "$output_folder"relie-softwarename${filename##*/}
done

for filename in "$dir"relie-urls/*; do
  echo $filename
  echo $output_folder${filename##*/}
  $t $run $mode fix --file $filename --testfile "$dir"relie-urls.test > "$output_folder"relie-urls${filename##*/}
done


