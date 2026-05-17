#!/usr/bin/env python3
"""mapper_sitemap.py"""


import sys
import urllib.request
import re
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

link_pattern = re.compile(r'href=["\'](http[s]?://.*?)["\']')

for line in sys.stdin:
    url = line.strip()
    if not url.startswith('https://'):
        continue

    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})

        with urllib.request.urlopen(req, timeout=5, context=ctx) as response:
            html = response.read().decode('utf-8', errors='ignore')

            links = link_pattern.findall(html)

            for link in links:
                print(f"{url}\t{link}")
    except Exception as e:
        continue