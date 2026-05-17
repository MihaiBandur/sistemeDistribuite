#!/usr/bin/env python3
"""mapper_history.py"""

import sys
from urllib.parse import urlparse

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue

    try:
        url, count = line.split('\t',1)
        parsed_url = urlparse(url)
        host = parsed_url.netloc

        if host:
            print(f"{host}\t{count}")
    except Exception:
        continue

