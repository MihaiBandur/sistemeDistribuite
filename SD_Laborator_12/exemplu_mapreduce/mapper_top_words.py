#!/usr/bin/env python3
"""mapper_top_words.py"""


import sys
import urllib.request
import ssl
import re
from collections import Counter

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE


stopwords = {'the', 'and', 'of', 'to', 'a', 'in', 'is', 'that', 'for', 'it', 'on', 'with', 'as', 'this', 'by', 'are', 'from', 'be', 'or', 'an', 'at'}

for line in sys.stdin:
    url = line.strip()
    if not url.startswith('https://'):
        continue


    try:
        req = urllib.request.Request(url=url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=5, context=ctx) as response:
            html = response.read().decode('utf-8', errors='ignore')

            text = re.sub(r'<script.*?>.*?</script>', ' ', html, flags=re.DOTALL|re.IGNORECASE)
            text = re.sub(r'<style.*?>.*?</style>', ' ', text, flags=re.DOTALL|re.IGNORECASE)
            text = re.sub(r'<[^>]+>', ' ', text)

            words = re.findall(r'\b[a-z]{3,}\b', text.lower())
            words = [w for w in words if w not in stopwords]


            page_counter = Counter(words)
            
            top5_page = page_counter.most_common(5)
            print(f"PAGINA:{url}\t{top5_page}")
            
            # Emitere cuvinte individuale pentru calculul global posterior
            for word, count in page_counter.items():
                print(f"GLOBAL:{word}\t{count}")

    except Exception: 
        continue