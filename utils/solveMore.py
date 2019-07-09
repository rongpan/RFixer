import glob
import sys
import re
import os
import argparse
  
parser = argparse.ArgumentParser(description='Optional app description')
parser.add_argument('folder1', type=str,
                    help='A required string positional argument')
parser.add_argument('folder2', type=str,
                    help='A required string positional argument')
parser.add_argument('timeout', type=int,
                    help='A required int positional argument')
args = parser.parse_args()
fname1 = args.folder1
fname2 = args.folder2
timeout = args.timeout

files2 = []
files1 = glob.glob(fname1 + "*.txt")
res = []
count = 0
count2 = 0
total_solve_time = 0

total_1 = 0
total_2 = 0
pcount = 0

for name in files1:
  files2.append(fname2 + name[len(fname1):])

for i in range(0, len(files1)):
  time1 = timeout
  time2 = timeout
  #print files1[i]
  with open(files1[i], "r") as file:
    contents = file.read()
    content = contents.partition('#c#')[2]
    time = content.partition('#c#')[0]
    if len(time) > 0:
      time1 = int(time)

  with open(files2[i], "r") as file:
    contents = file.read()
    content = contents.partition('#c#')[2]
    time = content.partition('#c#')[0]
    if len(time) > 0:
      time2 = int(time)

  if not time1 == timeout and time2 == timeout:
    count += 1
    total_solve_time += min(time1, time2)
  #print str(time1) + ',' + str(time2)

  if time1 == timeout and not time2 == timeout:
    count2 += 1

  if not time1 == timeout and not time2 == timeout:
    pcount += 1
    total_1 += time1
    total_2 += time2
  #res.append((time1, time2))

print str(count)

#print 'total solve time ' + str(total_solve_time)
#print 'avg time' + str(total_solve_time/count)
#print res
#print files1
#print files2


