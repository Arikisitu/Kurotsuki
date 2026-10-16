/**
 * SAHASRA - MangaDex API Client
 * Provides genuine chapter feeds and page manifests from api.mangadex.org
 */

export interface MangaDexManga {
  id: string;
  title: string;
  description: string;
}

export interface MangaDexChapter {
  id: string;
  chapter: string;
  title: string;
  pages: number;
  publishAt: string;
}

export interface MangaDexChapterPages {
  chapterId: string;
  baseUrl: string;
  hash: string;
  pageUrls: string[];
  pageCount: number;
}

const MANGADEX_API_BASE = 'https://api.mangadex.org';

/**
 * Search MangaDex by manga title
 */
export async function searchMangaDex(title: string): Promise<MangaDexManga[]> {
  try {
    const cleanTitle = title.replace(/[^\w\s]/gi, ' ').trim();
    const res = await fetch(
      `${MANGADEX_API_BASE}/manga?title=${encodeURIComponent(cleanTitle)}&limit=5&order[relevance]=desc`,
      { headers: { 'Accept': 'application/json' } }
    );
    if (!res.ok) return [];
    const data = await res.json();
    return (data.data || []).map((item: any) => ({
      id: item.id,
      title: item.attributes.title.en || Object.values(item.attributes.title)[0] || 'Unknown',
      description: item.attributes.description?.en || '',
    }));
  } catch {
    return [];
  }
}

/**
 * Get available chapters for a MangaDex manga ID
 */
export async function getMangaDexChapters(
  mangaDexId: string,
  language = 'en'
): Promise<MangaDexChapter[]> {
  try {
    const res = await fetch(
      `${MANGADEX_API_BASE}/manga/${mangaDexId}/feed?translatedLanguage[]=${language}&order[chapter]=asc&limit=100`,
      { headers: { 'Accept': 'application/json' } }
    );
    if (!res.ok) return [];
    const data = await res.json();
    const chapters: MangaDexChapter[] = [];
    const seenChapters = new Set<string>();

    for (const item of data.data || []) {
      const chNum = item.attributes.chapter || '1';
      if (!seenChapters.has(chNum)) {
        seenChapters.add(chNum);
        chapters.push({
          id: item.id,
          chapter: chNum,
          title: item.attributes.title || `Chapter ${chNum}`,
          pages: item.attributes.pages || 0,
          publishAt: item.attributes.publishAt || '',
        });
      }
    }

    return chapters.sort((a, b) => parseFloat(a.chapter) - parseFloat(b.chapter));
  } catch {
    return [];
  }
}

/**
 * Get genuine page image URLs for a chapter using MangaDex At-Home Server
 */
export async function getMangaDexChapterPages(
  chapterId: string,
  useDataSaver = false
): Promise<MangaDexChapterPages | null> {
  try {
    const res = await fetch(`${MANGADEX_API_BASE}/at-home/server/${chapterId}`, {
      headers: { 'Accept': 'application/json' },
    });
    if (!res.ok) return null;
    const data = await res.json();
    const baseUrl = data.baseUrl;
    const hash = data.chapter.hash;
    const files: string[] = useDataSaver ? data.chapter.dataSaver : data.chapter.data;

    const pageUrls = files.map(
      (filename: string) =>
        `${baseUrl}/${useDataSaver ? 'data-saver' : 'data'}/${hash}/${filename}`
    );

    return {
      chapterId,
      baseUrl,
      hash,
      pageUrls,
      pageCount: pageUrls.length,
    };
  } catch {
    return null;
  }
}
