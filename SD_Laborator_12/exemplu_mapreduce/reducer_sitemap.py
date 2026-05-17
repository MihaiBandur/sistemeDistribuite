#!/usr/bin/env python3
"""reducer_sitemap.py"""


import sys
current_url = None
links_list = []

for line in sys.stdin:
    line  = line.strip()

    try:
        url, link = line.split('\t',1)
    except Exception as e:
        continue

    if current_url == url:
        links_list.append(link)
    else:
        if current_url:
            unique_links = list(set(links_list))
            print(f"{current_url}\t{unique_links}")
        
        current_url = url
        links_list = [link]

if current_url:
    unique_links = list(set(links_list))
    print(f"{current_url}\t{unique_links}")