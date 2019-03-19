import os
import argparse

parser = argparse.ArgumentParser(description='Optional app description')
parser.add_argument('input_folder', type=str,
                    help='A required string positional argument')

args = parser.parse_args()
input_dir = args.input_folder


res = []
for filename in os.listdir(input_dir):
	#print filename
	examples = 0
	with open(input_dir + filename, 'r') as f:
		content = f.readlines()
		count = 0
		for line in content:
			if count == 0:
				if line.startswith('+++'):
					count = 1
					continue
				else:
					continue
			if count == 1:
				if line.startswith('---'):
					count = 2
					continue
				elif len(line.strip()) > 0:
					examples += 1
					continue
				else:
					continue
			if count == 2:
				if len(line.strip()) > 0:
					examples += 1
					continue
	res.append(examples)

print res