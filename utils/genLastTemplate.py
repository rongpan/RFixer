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

args = parser.parse_args()
folder1 = args.folder1
folder2 = args.folder2

files2 = []
files1 = glob.glob(folder1 + "*.txt")

for name in files1:
  files2.append(folder2 + name[len(folder1):])

for i in range(0, len(files1)):
  sol = ''
  max_sol = ''
  with open(files1[i], "r") as file:
    contents = file.read()
    content = contents.partition('#sol#')[2]
    sol = content.partition('#sol#')[0]
    template = contents.split('  |  ')[-1]
    template = template.split(' ')
    print template
    
    content = contents.partition('#m#')[2]
    max_sol = content.partition('#m#')[0]
'''
  with open(files2[i], "a") as file:
    file.write('\n#sol2#')
    file.write(sol)
    file.write('\n#m2#')
    file.write(max_sol)
    ''' 
  #res.append((time1, time2))

#print res
#print files1
#print files2


