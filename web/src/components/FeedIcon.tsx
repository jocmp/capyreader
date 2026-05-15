import { useState } from "react";
import { Rss } from "lucide-react";
import { useFeedIcon } from "@/api/queries";
import { cn } from "@/lib/cn";

interface FeedIconProps {
  iconId?: number | null;
  title: string;
  className?: string;
  // Tailwind size token (e.g. "h-4 w-4"). Defaults to a tight 16px badge.
  sizeClassName?: string;
}

export default function FeedIcon({
  iconId,
  title,
  className,
  sizeClassName = "h-4 w-4",
}: FeedIconProps) {
  const { data, isError } = useFeedIcon(iconId);
  const [imgFailed, setImgFailed] = useState(false);

  // Miniflux returns the icon body as `image/png;base64,<data>` — prefixing
  // with `data:` produces the data URL the browser can render directly.
  const src = data ? `data:${data.data}` : null;

  const wrapper = cn(
    "flex flex-shrink-0 items-center justify-center overflow-hidden rounded-sm bg-white/95 dark:bg-white/85 ring-1 ring-black/5",
    sizeClassName,
    className,
  );

  if (src && !imgFailed && !isError) {
    return (
      <span className={wrapper} aria-hidden="true">
        <img
          src={src}
          alt=""
          className="h-full w-full object-contain"
          onError={() => setImgFailed(true)}
          loading="lazy"
          decoding="async"
        />
      </span>
    );
  }

  // Fall back to the feed's first character so the sidebar stays scannable
  // when Miniflux hasn't fetched an icon yet. WCAG: bump to text-xs and use
  // `text-foreground/90` so the glyph clears AA on `bg-muted`.
  const initial = (title ?? "").trim().slice(0, 1).toUpperCase();
  return (
    <span
      className={cn(
        wrapper,
        "bg-muted text-xs font-bold uppercase text-foreground/90 ring-0",
      )}
      aria-hidden="true"
    >
      {initial || <Rss className="h-3 w-3" />}
    </span>
  );
}
