import glob
import sys
import re
import os
import argparse
import numpy
  
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
files5 = []
files1 = glob.glob(fname1 + "*")

files1.sort()

res = []
count = 0
total_solve_time = 0
for name in files1:
  files2.append(fname2 + name[len(fname1):])
  files5.append('results5/compared/' + name[len(fname1):])

better_base= 0
same_base = 0
worse_base = 0

better_re= 0
same_re = 0
worse_re = 0

our_total = 0
base_total = 0
re_total = 0

our_list = []
base_list = []
re_list = []

str1 = ''

total_percent = 0
count_times = 0
#files1.sort()

for i in range(0, len(files1)):
  time1 = timeout
  time2 = timeout
  f1max1 = 0
  f1max2 = 0
  f1o = 0
  f1re = 0
  f11 = 0
  f12 = 0
  #print files1[i]
  with open(files1[i], "r") as file:
    contents = file.read()
    content = contents.partition('#c#')[2]
    time = content.partition('#c#')[0]
    if len(time) > 0:
      time1 = int(time)

    content = contents.partition('F1 max score:')[2]
    content = content.partition('#')[0]
    if len(content) > 0:
      f1max1 = float(content)

    content = contents.partition('F1 score:')[2]
    content = content.partition('#')[0]
    if len(content) > 0:
      f11 = float(content)

  with open(files2[i], "r") as file:
    contents = file.read()
    content = contents.partition('#c#')[2]
    time = content.partition('#c#')[0]
    if len(time) > 0:
      time2 = int(time)

    content = contents.partition('F1 max score:')[2]
    content = content.partition('#')[0]
    if len(content) > 0:
      f1max2 = float(content)

    content = contents.partition('F1 score:')[2]
    content = content.partition('#')[0]
    if len(content) > 0:
      f12 = float(content)

  with open(files5[i], "r") as file:
    contents = file.read()
    f1o = contents.partition(',')[0]
    f1re = contents.partition(',')[2]


  if ((not f1max1 == 0 and not str(f1max1) == 'nan') or \
     (not f1max2 == 0 and not str(f1max2) == 'nan')) and \
     (not f1o == 'TO' and not f1re == 'TO'):
    
    if not f1max1 == 0 and not str(f1max1) == 'nan':
      total_percent += (f1max1 - f11)/f1max1
      count_times += 1

    if not f1max2 == 0 and not str(f1max2) == 'nan':
      total_percent += (f1max2 - f12)/f1max2
      count_times += 1

print total_percent/count_times

'''
    avg = (float(f1max1) + float(f1max2))/2
    f1o = float(f1o)
    f1re = float(f1re)
    if avg > f1o:
      better_base += 1
    elif avg == f1o:
      same_base += 1
    else:
      worse_base += 1 

    if avg > f1re:
      better_re += 1
    elif avg == f1re:
      same_re += 1
    else:
      worse_re += 1 

    our_total += avg
    base_total += f1o
    re_total += f1re

    our_list.append(avg)
    base_list.append(f1o)
    re_list.append(f1re)

print better_base
print same_base
print worse_base

print better_re
print same_re
print worse_re

our_avg = our_total/count
base_avg = base_total/count
re_avg = re_total/count

print 'our_avg: ' + str(our_avg)
print 'base_avg: ' + str(base_avg)
print 're_avg: ' + str(re_avg)

print 'abs_impv_base: ' + str(our_avg - base_avg)
print 'abs_impv_re: ' + str(our_avg - re_avg)

impv_base_total = 0
impv_re_total = 0

for i in range(0, len(our_list)):
  impv_base_total += (our_list[i] - base_list[i])/base_list[i]
  impv_re_total += (our_list[i] - re_list[i])/re_list[i]

print 'impv_base: ' + str(impv_base_total/count)
print 'impv_re: ' + str(impv_re_total/count)

print numpy.array(our_list).std()
print numpy.array(base_list).std()
print numpy.array(re_list).std()
'''


'''
  if not time1 == timeout or not time2 == timeout or not time3 == timeout or not time4 == timeout:
    count += 1
    total_solve_time += min(time1, time2, time3, time4)
  print str(time1) + ',' + str(time2) + ',' + str(time3) + ',' + str(time4)
'''
  #res.append((time1, time2))

#print 'count ' + str(count)
#print 'total solve time ' + str(total_solve_time)
#print res
#print files1
#print files2


