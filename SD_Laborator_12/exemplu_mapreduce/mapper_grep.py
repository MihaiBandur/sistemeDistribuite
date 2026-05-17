#!/usr/bin/env python3
'''mapper_grep.py'''

import sys
import subprocess
import re

if len(sys.argv) > 1:
    regex_pattern = sys.argv[1]
else:
    regex_pattern = '.*'

pattern = re.compile(regex_pattern)

for line in sys.stdin:
    command_str = line.strip()
    if not command_str:
        continue
    
    try:
        output = subprocess.check_output(command_str, shell=True, text=True)

        for out_line in output.split('\n'):
            if pattern.search(out_line):
                print(f"GREP\t{out_line}")
    except Exception as e:
        continue