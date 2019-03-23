#!/bin/bash
to="gtimeout 300"

for filename in tests/benchmark_explicit/*.txt; do
  newname=${filename//tests\/benchmark_explicit\/}
  echo $newname
  $to java -jar target/regfixer.jar fix --limit 400000 --file $filename > results/$newname
done
