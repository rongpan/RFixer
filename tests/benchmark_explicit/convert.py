#! /usr/bin/env python3
import sys
import re
import os

def read (filename):
  with open(filename, "r") as file:
    contents = file.read()
    file.close()
    return contents

def parse (contents):
  lines = contents.split("\n")
  i = 0
  regex = lines[i]
  i += 1
  assert lines[i] == "---"
  i += 1
  pos = []
  pattern = re.compile(r"\((\d+):(\d+)\)")
  while lines[i] != "---":
    p = lines[i]
    i += 1
    match = pattern.match(p)
    s = int(match.group(1))
    e = int(match.group(2))
    pos += [(s,e)]
  assert lines[i] == "---"
  i += 1
  corpus = "\n".join(lines[i:])
  return (regex, pos, corpus)

def printRange (rng):
  print("(%d:%d)"%(rng[0],rng[1]))

def allRangesStartingAt (s, corpus):
  rng = []
  for e in range(s+1, len(corpus)+1):
    rng += [(s,e)]
  return rng

def mostRangesStartingAt (s, e, corpus):
  rng = []
  for e in range(e+1, len(corpus)+1):
    rng += [(s,e)]
  return rng

filenames = sys.argv[1:]
if len(sys.argv) <= 1:
  print("usage...")
else:
  for filename in filenames:
    regex, pos, corpus = parse(read(filename))

    neg = []
    posToCheck = pos[:]
    for i in range(0, len(corpus)+1):
      if (len(posToCheck) == 0):
        neg += allRangesStartingAt(i, corpus)
      else:
        s,e = posToCheck[0]
        if i < s:
          neg += allRangesStartingAt(i, corpus)
        elif i >= s and i < e:
          neg += mostRangesStartingAt(i, e, corpus)
        elif i >= e:
          neg += allRangesStartingAt(i, corpus)
          posToCheck.pop(0)


    print(regex)
    print('+++')
    for i in range(0,len(pos)):
      printRange(pos[i])
    print('---')
    for i in range(0,len(neg)):
      printRange(neg[i])
    print('---')
    print(corpus)
