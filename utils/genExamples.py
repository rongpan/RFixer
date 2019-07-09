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
parser.add_argument('folder3', type=str,
                    help='A required string positional argument')
parser.add_argument('folder4', type=str,
                    help='A required string positional argument')
parser.add_argument('timeout', type=int,
                    help='A required int positional argument')
args = parser.parse_args()
fname1 = args.folder1
fname2 = args.folder2
fname3 = args.folder3
fname4 = args.folder4
timeout = args.timeout

files2 = []
files3 = []
files4 = []
files1 = glob.glob(fname1 + "*.txt")
res = []
count = 0
total_solve_time = 0
for name in files1:
  files2.append(fname2 + name[len(fname1):])
  files3.append(fname3 + name[len(fname1):])
  files4.append(fname4 + name[len(fname1):])

t1 = 0
t2 = 0
t3 = 0
t4 = 0

for i in range(0, len(files1)):
  time1 = timeout
  time2 = timeout
  time3 = timeout
  time4 = timeout

  #print files1[i]
  with open(files1[i], "r") as file:
    contents = file.read()
    content = contents.partition('#c#')[2]
    time = content.partition('#c#')[0]
    if len(time) > 0:
      tmp = 0
      content = contents.partition('#p#')[2]
      p = content.partition('#p#')[0]
      tmp += int(p)
      content = contents.partition('#n#')[2]
      n = content.partition('#n#')[0]
      tmp += int(n)
      if tmp > 0:
        t1 += tmp
        count += 1

  with open(files2[i], "r") as file:
    contents = file.read()
    content = contents.partition('#c#')[2]
    time = content.partition('#c#')[0]
    if len(time) > 0:
      tmp = 0
      content = contents.partition('#p#')[2]
      p = content.partition('#p#')[0]
      tmp += int(p)
      content = contents.partition('#n#')[2]
      n = content.partition('#n#')[0]
      tmp += int(n)
      if tmp > 0:
        t2 += tmp
        count += 1

  with open(files3[i], "r") as file:
    contents = file.read()
    content = contents.partition('#c#')[2]
    time = content.partition('#c#')[0]
    if len(time) > 0:
      tmp = 0
      content = contents.partition('#p#')[2]
      p = content.partition('#p#')[0]
      tmp += int(p)
      content = contents.partition('#n#')[2]
      n = content.partition('#n#')[0]
      tmp += int(n)
      if tmp > 0:
        t3 += tmp
        count += 1

  with open(files4[i], "r") as file:
    contents = file.read()
    content = contents.partition('#c#')[2]
    time = content.partition('#c#')[0]
    if len(time) > 0:
      tmp = 0
      content = contents.partition('#p#')[2]
      p = content.partition('#p#')[0]
      tmp += int(p)
      content = contents.partition('#n#')[2]
      n = content.partition('#n#')[0]
      tmp += int(n)
      if tmp > 0:
        t4 += tmp
        count += 1

print 1.0*(t1 + t2 + t3 + t4)/count
  #res.append((time1, time2))

#print 'count ' + str(count)
#print 'total solve time ' + str(total_solve_time)
#print res
#print files1
#print files2


