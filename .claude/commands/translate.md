Translate missing Android string resources.

## Arguments
- $ARGUMENTS: Optional locale code (e.g. `fr`, `de`) or `--dry-run` to preview missing keys

## Context
- Base strings: `app/src/main/res/values/strings.xml`
- Locale files: `app/src/main/res/values-{locale}/strings.xml`
- Skip any base string with `translatable="false"`

## Locale codes and languages
ar=Arabic, bg=Bulgarian, b+sr+Latn=Serbian (Latin), cs=Czech, cy=Welsh, da=Danish, de=German, el=Greek, es=Spanish, et=Estonian, fr=French, gl=Galician, hu=Hungarian, in=Indonesian, it=Italian, iw=Hebrew, ja=Japanese, lv=Latvian, ml=Malayalam, nb-rNO=Norwegian Bokmål, ne=Nepali, nl=Dutch, pl=Polish, pt=Portuguese, pt-rBR=Brazilian Portuguese, ro=Romanian, ru=Russian, sv=Swedish, ta=Tamil, tr=Turkish, uk=Ukrainian, zh-rCN=Simplified Chinese, zh-rTW=Traditional Chinese

## Steps

1. **Read the base `values/strings.xml`** and collect all translatable `<string>` and `<plurals>` entries (skip `translatable="false"`).

2. **Process one locale at a time** (or just the one specified in $ARGUMENTS):
   - Read the locale's `strings.xml` (only this file and the base — never load multiple locales at once)
   - Identify string names and plural names present in the base but missing in the locale
   - If no missing keys, skip to the next locale
   - If `--dry-run`, just list the missing keys and move to the next locale

3. **Translate missing strings** for the current locale:
   - Rules for translation:
     - Preserve format specifiers exactly: `%1$s`, `%1$d`, `%d`, `%2$s`, `%1$d`, etc.
     - Preserve escaped characters exactly: `\'`, `\n`
     - Match the tone of the English text — prefer informal but polite over formal conjugation
     - Do NOT mix languages
   - Insert the new `<string>` and `<plurals>` elements before the closing `</resources>` tag using Edit
   - Use the same indentation style as existing entries in that file (typically 4 spaces)

4. **Sequentially process locales** — finish one locale completely (read, translate, edit) before starting the next. This keeps context small by only holding the base strings and one locale file at a time.

5. **Print a summary** of how many strings were added per locale.
