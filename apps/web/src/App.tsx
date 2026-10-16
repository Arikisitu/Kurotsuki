import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Layout } from './components/layout/Layout';
import { HomePage } from './pages/HomePage';
import { ExplorePage } from './pages/ExplorePage';
import { SearchPage } from './pages/SearchPage';
import { AnimeDetailPage } from './pages/AnimeDetailPage';
import { MangaDetailPage } from './pages/MangaDetailPage';
import { LibraryPage } from './pages/LibraryPage';
import { SchedulePage } from './pages/SchedulePage';
import { SettingsPage } from './pages/SettingsPage';
import { WatchPage } from './pages/WatchPage';
import { ReadPage } from './pages/ReadPage';
import { NotFoundPage } from './pages/NotFoundPage';

// Configure TanStack Query Client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 1000 * 60 * 5, // 5 minutes cache
    },
  },
});

export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<HomePage />} />
            <Route path="explore" element={<ExplorePage />} />
            <Route path="search" element={<SearchPage />} />
            <Route path="anime/:id" element={<AnimeDetailPage />} />
            <Route path="watch/:id" element={<WatchPage />} />
            <Route path="watch/:id/:episode" element={<WatchPage />} />
            <Route path="manga/:id" element={<MangaDetailPage />} />
            <Route path="read/:id" element={<ReadPage />} />
            <Route path="read/:id/:chapter" element={<ReadPage />} />
            <Route path="library" element={<LibraryPage />} />
            <Route path="schedule" element={<SchedulePage />} />
            <Route path="settings" element={<SettingsPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
