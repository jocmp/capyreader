import { useMemo } from "react";
import { useFeeds } from "@/api/queries";
import type { Feed } from "@/api/types";

export interface FeedColor {
  hue: number;
  saturation: number;
}

// Spread feeds inside each category around the hue wheel so neighbouring rows
// look meaningfully different even when there are only a handful of items. We
// rotate each category's starting hue by the golden angle so two adjacent
// categories don't collide on the same palette, then place the feeds inside
// the category at evenly spaced offsets. Hundreds of feeds globally will
// inevitably reuse hues, but within any one category the colours stay
// distinct, which is what the user actually scans by.
const GOLDEN_ANGLE = 137.508;

function buildPalette(feeds: Feed[]): Map<number, FeedColor> {
  const byCategory = new Map<number, Feed[]>();
  for (const feed of feeds) {
    const list = byCategory.get(feed.category.id) ?? [];
    list.push(feed);
    byCategory.set(feed.category.id, list);
  }

  // Sort categories by id so the assignment is deterministic across reloads.
  const categoryIds = Array.from(byCategory.keys()).sort((a, b) => a - b);

  const colors = new Map<number, FeedColor>();
  categoryIds.forEach((categoryId, categoryIndex) => {
    const list = (byCategory.get(categoryId) ?? [])
      .slice()
      .sort((a, b) => a.id - b.id);
    const span = list.length;
    const categoryOffset = (categoryIndex * GOLDEN_ANGLE) % 360;
    list.forEach((feed, feedIndex) => {
      const hue =
        (categoryOffset + (feedIndex * 360) / Math.max(span, 1)) % 360;
      // Slight saturation jitter so two feeds that happen to share a hue
      // (rare, but possible across categories) still look distinguishable.
      const saturation = 55 + ((feed.id % 4) - 2) * 4;
      colors.set(feed.id, { hue, saturation });
    });
  });

  return colors;
}

export function useFeedColors(): Map<number, FeedColor> {
  const feedsQ = useFeeds();
  return useMemo(() => buildPalette(feedsQ.data ?? []), [feedsQ.data]);
}

/**
 * Soft tint suitable for a row background. Uses alpha so the colour blends
 * legibly against both the light and dark theme backgrounds without us having
 * to ship a second palette per theme.
 */
export function feedTint(
  color: FeedColor | undefined,
  emphasis: "subtle" | "strong" = "subtle",
): string | undefined {
  if (!color) return undefined;
  const alpha = emphasis === "strong" ? 0.22 : 0.11;
  return `hsla(${color.hue.toFixed(1)}, ${color.saturation}%, 55%, ${alpha})`;
}
