interface MediaItem {
  url: string;
  altText: string | null;
}

declare const Android: {
  openImageGallery(imagesJson: string, clickedIndex: number): void;
  showLinkDialog(href: string, text: string): void;
};

declare const Mercury: {
  parse(
    url: string | null,
    options: { html: string }
  ): Promise<{
    lead_image_url: string | null;
    content: string;
  }>;
};
