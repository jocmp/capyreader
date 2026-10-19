import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

SOURCE_STRINGS = Path("app/src/main/res/values/strings.xml")
LOCALE_FILE = re.compile(r"app/src/main/res/values-([^/]+)/strings\.xml$")
FASTLANE_FILE = re.compile(r"fastlane/metadata/android/([^/]+)/(.+)$")
STRING = re.compile(r'<string name="([^"]+)"([^>]*)>(.*)</string>')
PLURALS_OPEN = re.compile(r'<plurals name="([^"]+)"')
ITEM = re.compile(r'<item quantity="([^"]+)">(.*)</item>')
PLACEHOLDER = re.compile(r"%(?:(\d+)\$)?[-#+ 0,(]*\d*(?:\.\d+)?([a-zA-Z%])")
TAG = re.compile(r"</?([a-zA-Z]+)[^>]*>")
URL = re.compile(r"(?:https?://|www\.)\S+|[\w.+-]+@[\w-]+\.[\w.]+|\b[\w-]+\.(?:com|org|net|io|app|ru|cn|xyz|me|ly)\b", re.I)
INVISIBLE = re.compile("[­​-‏‪-‮⁠-⁩﻿]")
UNESCAPED_APOSTROPHE = re.compile(r"(?<!\\)'")
COMPACT_SCRIPTS = ("ja", "zh", "ko")
BRANDS = ["Capy Reader", "Feedbin", "FreshRSS", "Miniflux", "Google Reader", "OPML", "RSS", "E Ink"]


def read_source():
    text = SOURCE_STRINGS.read_text()
    strings = {}
    untranslatable = set()
    for match in STRING.finditer(text):
        name, attrs, value = match.groups()
        strings[name] = value
        if 'translatable="false"' in attrs:
            untranslatable.add(name)
    for block in re.finditer(r'<plurals name="([^"]+)"[^>]*>(.*?)</plurals>', text, re.S):
        name, body = block.groups()
        for quantity, value in ITEM.findall(body):
            strings[f"{name}#{quantity}"] = value
        strings.setdefault(f"{name}#other", strings.get(f"{name}#one", ""))
    return strings, untranslatable


def parse_diff(lines):
    changes = defaultdict(dict)
    fastlane = defaultdict(list)
    path = None
    plural = None
    for line in lines:
        if line.startswith("diff --git"):
            path = line.split(" b/", 1)[-1].strip()
            plural = None
            continue
        if line.startswith(("+++", "---", "@@", "index ")):
            continue
        sign, body = line[:1], line[1:]
        if sign not in "+- ":
            continue
        fastlane_match = FASTLANE_FILE.search(path or "")
        if fastlane_match and sign == "+":
            fastlane[path].append(body)
            continue
        locale_match = LOCALE_FILE.search(path or "")
        if not locale_match:
            continue
        locale = locale_match.group(1)
        if opened := PLURALS_OPEN.search(body):
            plural = opened.group(1)
        key = None
        value = None
        if match := STRING.search(body):
            key, value = match.group(1), match.group(3)
        elif plural and (match := ITEM.search(body)):
            key, value = f"{plural}#{match.group(1)}", match.group(2)
        if "</plurals>" in body:
            plural = None
        if key is None or sign == " ":
            continue
        entry = changes[locale].setdefault(key, {})
        if sign == "+":
            entry["new"] = value
        else:
            entry["old"] = value
    return changes, fastlane


def placeholders(value):
    found = Counter()
    position = 0
    for index, kind in PLACEHOLDER.findall(value):
        if kind == "%":
            continue
        if not index:
            position += 1
            index = str(position)
        found[(index, kind)] += 1
    return found


def flags_for(locale, key, new, source, untranslatable):
    base = key.split("#")[0]
    english = source.get(key) or source.get(f"{base}#other")
    flags = []
    if base in untranslatable:
        flags.append("translatable=false in source")
    if english is None:
        flags.append("key not in English source")
        return english, flags
    if new is None:
        flags.append("removed")
        return english, flags
    if placeholders(new) != placeholders(english):
        flags.append(f"placeholders {dict(placeholders(english))} -> {dict(placeholders(new))}")
    if sorted(TAG.findall(new)) != sorted(TAG.findall(english)):
        flags.append("markup differs")
    added_links = set(URL.findall(new)) - set(URL.findall(english))
    if added_links:
        flags.append(f"new link/domain {sorted(added_links)}")
    if INVISIBLE.search(new):
        flags.append("invisible/bidi chars " + ",".join(f"U+{ord(c):04X}" for c in INVISIBLE.findall(new)))
    if UNESCAPED_APOSTROPHE.search(new) and not (new.startswith('"') and new.endswith('"')):
        flags.append("unescaped apostrophe")
    missing_brands = [brand for brand in BRANDS if brand in english and brand not in new]
    if missing_brands:
        flags.append(f"brand missing {missing_brands}")
    if new.strip() == english.strip() and english.strip() not in BRANDS:
        flags.append("same as English")
    if not locale.startswith(COMPACT_SCRIPTS) and len(english) >= 8 and not 0.3 <= len(new) / len(english) <= 3.0:
        flags.append(f"length x{len(new) / len(english):.1f}")
    return english, flags


def main():
    source, untranslatable = read_source()
    changes, fastlane = parse_diff(sys.stdin.read().splitlines())
    flagged_total = 0
    for locale in sorted(changes):
        entries = changes[locale]
        print(f"## {locale} ({len(entries)} changed)\n")
        for key in sorted(entries):
            entry = entries[key]
            english, flags = flags_for(locale, key, entry.get("new"), source, untranslatable)
            flagged_total += bool(flags)
            marker = "!" if flags else " "
            print(f"{marker} {key}")
            print(f"    en:  {english}")
            if "old" in entry:
                print(f"    old: {entry['old']}")
            print(f"    new: {entry.get('new')}")
            if flags:
                print(f"    flags: {'; '.join(flags)}")
        print()
    for path in sorted(fastlane):
        print(f"## {path}\n")
        body = "\n".join(fastlane[path])
        print(body)
        links = URL.findall(body)
        if links:
            print(f"\n! links: {sorted(set(links))}")
        if INVISIBLE.search(body):
            print("\n! invisible/bidi chars present")
        print()
    print(f"-- {sum(len(e) for e in changes.values())} strings across {len(changes)} locales, {flagged_total} flagged; {len(fastlane)} fastlane files")


if __name__ == "__main__":
    main()
