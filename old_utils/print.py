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
for name in files1:
    print name

#print res
#print files1
#print files2


