import os
import argparse

parser = argparse.ArgumentParser(description='Optional app description')
parser.add_argument('input_folder', type=str,
                    help='A required string positional argument')

args = parser.parse_args()
input_dir = args.input_folder

regexes = set()
alphabets = []

for filename in os.listdir(input_dir):
	letters = set()
	digit = set()
	with open(input_dir + filename, 'r') as f:
		content = f.readlines()
		regex = content[0].strip()
		if regex not in regexes:
			regexes.add(regex)
			guard = False
			for c in regex:
				if c == '{':
					guard = True
				if c == '}':
					guard = False
				if guard:
					continue
				if c.isalpha():
					if c not in letters:
						letters.add(c)
				if unicode(c).isnumeric():
					if c not in digit:
						digit.add(c)
			alphabets.append(len(letters) + len(digit))
			#print filename
			#print 'letters: ' + str(len(letters))
			#print 'digit: ' + str(len(digit))
print sum(alphabets)/float(len(alphabets))
print alphabets.count(2)
print max(alphabets)
