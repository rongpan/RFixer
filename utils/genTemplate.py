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

bad_list = ['test834.txt','test835.txt','test836.txt','test7211.txt','test7213.txt','test7214.txt','test4363.txt','test3686.txt','test2272.txt','test1965.txt','test1425.txt']
for name in files1:
  if (name[len(fname1):] in bad_list):
    files1.remove(name)
for name in files1:
  files2.append(fname2 + name[len(fname1):])

totaltemp = 0
totalexplored = 0
file_num = 0

for i in range(0, len(files1)):
  alltemp1 = timeout
  alltemp2 = timeout
  explored1 = 0
  explored2 = 0
  with open(files1[i], "r") as file:
    print files1[i]
    contents = file.read()
    time = contents.partition('#c#')[2]
    time = time.partition('#c#')[0]
    if len(time) > 0:
      alltemp1 = len(contents.split('  |  ')) - 1
      explored1 = len(contents.split('template')) - 1
  with open(files2[i], "r") as file:
    print files2[i]
    contents = file.read()
    time = contents.partition('#c#')[2]
    time = time.partition('#c#')[0]
    if len(time) > 0:
      alltemp2 = len(contents.split('  |  ')) - 1
      explored2 = len(contents.split('template')) - 1
  if not alltemp1 == timeout and not alltemp2 == timeout:
    print alltemp1
    print alltemp2
    totaltemp += (alltemp1 + alltemp2)
    totalexplored += (explored1 + explored2)
    file_num += 1

print totalexplored/file_num
print totaltemp/file_num
print totalexplored
print totaltemp
print file_num
 
    #if len(time) > 0:
      #cost2 = int(content)
    #print cost2
  #mincost = min(cost1, cost2)
  #if not mincost == timeout:
   # totalcost += mincost

  #print str(time1) + '\t' + str(time2)
  #res.append((time1, time2))

#print res
#print files1
#print files2


