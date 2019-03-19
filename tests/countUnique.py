import os
import argparse

parser = argparse.ArgumentParser(description='Optional app description')
parser.add_argument('input_folder', type=str,
                    help='A required string positional argument')

args = parser.parse_args()
input_dir = args.input_folder

regexes = set()

for filename in os.listdir(input_dir):
	with open(input_dir + filename, 'r') as f:
		content = f.readlines()
		regex = content[0].strip()
		if regex not in regexes:
			regexes.add(regex) 

print len(regexes)