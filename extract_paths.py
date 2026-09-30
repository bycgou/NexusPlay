#!/usr/bin/env python3
import re
import pathlib
import collections

roots = [
    pathlib.Path(r"D:\ruanzhu\NexusPlay\biliPlusAdmin\dist"),
    pathlib.Path(r"D:\ruanzhu\NexusPlay\frontend\biliPlus\dist"),
]
pat = re.compile(r"""["'](/(?:api/)?[A-Za-z0-9_./%-]{2,80})["']""")
hits = collections.Counter()
files = 0
for root in roots:
    if not root.exists():
        print("missing", root)
        continue
    for p in root.rglob("*.js"):
        text = p.read_text(encoding="utf-8", errors="ignore")
        files += 1
        for m in pat.findall(text):
            if any(x in m for x in [".js", ".css", ".png", ".svg", ".woff", ".ico", ".map", ".vue", ".jpg", ".webp"]):
                continue
            if m.startswith("/node_modules") or m.startswith("/@") or m.startswith("/src/") or m.startswith("/assets/"):
                continue
            hits[m] += 1

print("files", files)
for k, v in sorted(hits.items()):
    print(f"{v:3} {k}")
