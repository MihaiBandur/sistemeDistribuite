#!/usr/bin/env python3
'''reducer_grep.py'''

import sys

toate_liniile = []

for linie in sys.stdin:
    linie = linie.strip()

    try:
        cheie, valoare = linie.split('\t',1)
        toate_liniile.append(valoare)
    except ValueError:
        continue

string_final = " | ".join(toate_liniile)

if len(toate_liniile) > 0:
    print(f"REZULTAT_FINAL\t{string_final}")