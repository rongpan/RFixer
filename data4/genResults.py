import glob
import sys
import re
import os
import argparse
  
parser = argparse.ArgumentParser(description='Optional app description')
parser.add_argument('file1', type=str,
                    help='A required string positional argument')
args = parser.parse_args()
fname1 = args.file1


with open(fname1) as xh:
    xlines = xh.readlines()
    for i in range(len(xlines)):
      xline = xlines[i]
      x1 = xline.partition('\t')[0]
      x2 = xline.partition('\t')[2]
      x2 = x2.partition('\n')[0]
      if int(x1) < int(x2):
        print x1
      else:
        print x2
		


