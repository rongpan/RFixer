import glob
import sys
import re
import os
import argparse
  
parser = argparse.ArgumentParser(description='Optional app description')
parser.add_argument('file1', type=str,
                    help='A required string positional argument')
parser.add_argument('file2', type=str,
                    help='A required string positional argument')
args = parser.parse_args()
fname1 = args.file1
fname2 = args.file2


with open(fname1) as xh:
  with open(fname2) as yh:
      xlines = xh.readlines()
      ylines = yh.readlines()
      for i in range(len(xlines)):
        print xlines[i].strip().partition(',')[0] + ' ' + ylines[i].strip().partition(',')[0]


