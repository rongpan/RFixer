import glob
import sys
import re
import os
import argparse

name = ''
oritime = ''
retime = ''

with open('results5/filenames.txt', 'r') as file1:
  contents1 = file1.readlines()
  for index in range(0, len(contents1)):
    line1 = contents1[index].strip()
    name = line1     
    with open('results5/f1ori.txt', 'r') as file2:
      contents2 = file2.readlines()
      line2 = contents2[index].strip()
      oritime = line2
    with open('results5/f1rebele.txt', 'r') as file3:
      contents3 = file3.readlines()
      line3 = contents3[index].strip()
      retime = line3
    with open('results5/compared/' + name, 'w+') as wfile:
      wfile.write(oritime + ',' + retime)
