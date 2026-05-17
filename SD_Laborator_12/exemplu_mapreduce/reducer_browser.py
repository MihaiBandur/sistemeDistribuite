#!/usr/bin/env python3
"""mapper_history.py"""

import sys

current_host = None
current_sum = 0

for line in sys.stdin:
    line = line.strip()
    try:
        host, count = line.split('\t', 1)
        count = int(count)
    except ValueError:
        continue

    if current_host == host:
        current_sum += count
    else:
        if current_host:
            print(f"{current_host}\t{current_sum}")
        current_host = host
        current_sum = count

if current_host:
    print(f"{current_host}\t{current_sum}")

