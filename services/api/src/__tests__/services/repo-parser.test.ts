import { describe, expect, it } from 'vitest';
import { RepoParserService, ALLOWED_LANGUAGES } from '../../services/repo-parser';

describe('RepoParserService', () => {
  const service = new RepoParserService();

  it('filters JSON repository extensions strictly to ["en", "hi", "ja", "all"]', () => {
    const rawCatalogue = [
      { name: 'AnimePahe', pkg: 'eu.kanade.tachiyomi.animeextension.en.animepahe', lang: 'en', code: 1, version: '1.0' },
      { name: 'YoMovies', pkg: 'eu.kanade.tachiyomi.animeextension.hi.yomovies', lang: 'hi', code: 1, version: '1.0' },
      { name: 'Anichi', pkg: 'eu.kanade.tachiyomi.animeextension.ja.anichi', lang: 'ja', code: 1, version: '1.0' },
      { name: 'AnimeOnsen', pkg: 'eu.kanade.tachiyomi.animeextension.all.animeonsen', lang: 'all', code: 1, version: '1.0' },
      // The following must be strictly filtered out:
      { name: 'AnimeSama', pkg: 'eu.kanade.tachiyomi.animeextension.fr.animesama', lang: 'fr', code: 1, version: '1.0' },
      { name: 'AnimeFLV', pkg: 'eu.kanade.tachiyomi.animeextension.es.animeflv', lang: 'es', code: 1, version: '1.0' },
      { name: 'Anitube', pkg: 'eu.kanade.tachiyomi.animeextension.pt.anitube', lang: 'pt-BR', code: 1, version: '1.0' },
      { name: 'AnimeGO', pkg: 'eu.kanade.tachiyomi.animeextension.ru.animego', lang: 'ru', code: 1, version: '1.0' },
      { name: 'AnimeWorld IT', pkg: 'eu.kanade.tachiyomi.animeextension.it.animeworld', lang: 'it', code: 1, version: '1.0' },
      { name: 'Anicloud', pkg: 'eu.kanade.tachiyomi.animeextension.de.anicloud', lang: 'de', code: 1, version: '1.0' },
    ];

    const parsed = service.parseJsonIndex(JSON.stringify(rawCatalogue), 'Test Repo', 'https://example.com/index.min.json');

    expect(parsed.totalExtensions).toBe(10);
    expect(parsed.filteredCount).toBe(4);
    expect(parsed.extensions.map((e) => e.lang).sort()).toEqual(['all', 'en', 'hi', 'ja']);

    // Ensure rejected languages are not present
    const rejectedFound = parsed.extensions.some((e) =>
      ['fr', 'es', 'pt-BR', 'ru', 'it', 'de'].includes(e.lang)
    );
    expect(rejectedFound).toBe(false);
  });

  it('validates ALLOWED_LANGUAGES constants', () => {
    expect(ALLOWED_LANGUAGES).toEqual(['en', 'hi', 'ja', 'all']);
  });
});
