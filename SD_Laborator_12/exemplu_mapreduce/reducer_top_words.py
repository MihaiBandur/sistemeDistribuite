#!/usr/bin/env python3
"""reducer_top_words.py"""



import sys
from collections import Counter

global_counter = Counter()

for line in sys.stdin:
    line = line.strip()
    try:
        cheie, valoare = line.split('\t', 1)
    except ValueError:
        continue

    if cheie.startswith("PAGINA:"):
        nume_url = cheie.split('PAGINA:')[1]
        print(F"TOP 5 cuvinte pe pagina -> {nume_url}\t{valoare}")
    elif cheie.startswith("GLOBAL:"):
        cuvant = cheie.split('GLOBAL:')[1]
        try:
            global_counter[cuvant] += int(valoare)
        except ValueError:
            continue

top5_global = global_counter.most_common(5)
print("=" * 60)
print(f"TOP 5 pe toate site-urile -> {top5_global}")
print("=" * 60)