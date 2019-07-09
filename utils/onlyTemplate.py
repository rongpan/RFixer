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

totaltemp1 = 0
totalexplored1 = 0
totaltemp2 = 0
totalexplored2 = 0
file_num = 0

for i in range(0, len(files1)):
  template1 = ''
  t1 = ''
  t2 = ''
  t3 = ''
  template2 = ''

  with open(files1[i], "r") as file:
    print files1[i]
    contents = file.read()
    template1 = contents.partition('#num#')[2]
    template1 = template1.partition('#num#')[0]

    t1 = contents.partition('#t1#')[2]
    t1 = t1.partition('#t1#')[0]

    t2 = contents.partition('#t2#')[2]
    t2 = t2.partition('#t2#')[0]

    t3 = contents.partition('#t3#')[2]
    t3 = t3.partition('#t3#')[0]
    
  with open(files2[i], "r") as file:
    print files2[i]
    contents = file.read()
    template2 = contents.partition('#num#')[2]
    template2 = template2.partition('#num#')[0]
   
  if not len(template1) == 0 and len(template2) == 0:

    totaltemp1 += (int(template1) - int(t1) - int(t2) - int(t3))
    print template1 + ',' + t1 + ',' + t2 + ',' + t3
    #totaltemp2 += int(template2)
    #totalexplored += (explored1 + explored2)
    file_num += 1

print 'file_num ' + str(file_num)
print 'totaltemp1 ' + str(totaltemp1)
#print 'totaltemp2 ' + str(totaltemp2)
print 'avg 1 ' + str(1.0*totaltemp1/file_num)
#print 'avg 2 ' + str(1.0*totaltemp2/file_num)
#print 'minus ' + str(1.0*totaltemp2/file_num - 1.0*totaltemp1/file_num)
#print 'percent ' + str((1.0*totaltemp2 - 1.0*totaltemp1)/totaltemp2)
 
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


