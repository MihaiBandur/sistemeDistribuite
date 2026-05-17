#!/usr/bin/env python3
"""mapper_ex1_lab.py"""


import sys
import string

for line in sys.stdin:

    line = line.strip()

    words = line.split()

    for word in words:
        clean_word = word.strip(string.punctuation + "—’‘“”")
        if len(clean_word) > 0:
            first_letter = clean_word[0].lower()
            if first_letter.isalpha():
                print('%s\t%s' % (first_letter, clean_word))

