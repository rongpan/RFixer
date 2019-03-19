counter=0
stop=20
while true; do
  if [ "$counter" -gt "$stop" ]; then
       exit 1
  else
       counter=$((counter+1))
       echo $counter
  fi
done
