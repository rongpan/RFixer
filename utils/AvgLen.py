import glob
import sys
import re
import os
import argparse

parser = argparse.ArgumentParser(description='Optional app description')
parser.add_argument('folder1', type=str,
                    help='A required string positional argument')
args = parser.parse_args()
fname1 = args.folder1

files1 = glob.glob(fname1 + "*.txt")
res = []

bad_list = ['test834.txt','test835.txt','test836.txt','test7211.txt','test7213.txt','test7214.txt','test4363.txt','test3686.txt','test2272.txt','test1965.txt','test1425.txt']
for name in files1:
  if (name[len(fname1):] in bad_list):
    files1.remove(name)

sum = 0

for i in range(0, len(files1)):
  with open(files1[i], "r") as file:
    i#print files1[i]
    contents = file.readlines()
    sum += len(contents[0].strip())

print sum/len(files1)
