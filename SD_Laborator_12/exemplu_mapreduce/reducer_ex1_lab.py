#!/usr/bin/env python3
"""reducer_ex1_lab.py"""

import sys

current_letter = None
current_word_list = []
letter = None

for line in sys.stdin:
    line = line.strip()

    try:
        letter, word = line.split('\t', 1)
    except ValueError:
        continue

    
    if current_letter == letter:
        current_word_list.append(word)
    else:
        if current_letter:
            print(f"{current_letter}\t{str(current_word_list)}")

        current_letter = letter
        current_word_list = [word]

if current_letter == letter and current_letter:
    print(f"{current_letter}\t{str(current_word_list)}\n")
