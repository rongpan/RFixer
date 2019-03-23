#!/bin/bash

# time ls ../regfixer-data/output/*.txt | head -n 10 | xargs ./test.sh

var=1
echo "name,size,solution,timeTotal,timeToFirstSol,templatesToFirstSol,templatesTotal,costOfFirstSol,timeSATSolver,timeDotTest,timeDotStarTest,timeEmptySetTest,totalDotTests,totalDotStarTests,totalEmptySetTests,totalDotTestsRejects,totalDotStarTestsRejects,totalEmptySetTestsRejects,maximumRoutes,totalPositiveExamples,lengthOfPositiveExamples,lengthOfCorpus"
for filename in "$@"
do
  (>&2 echo "$var -- $filename")
  ((var++))
  gtimeout 1m java -jar target/regfixer.jar fix --output csv --debug none --file "$filename"
  if [ $? -eq 124 ]; then
    (>&1 echo "\"$filename\",,,timeout")
  fi
done
