// Suppress THREE.Clock deprecation warning from R3F v9 — fixed in R3F v10 (not yet stable)
const _warn = console.warn;
console.warn = (...args: unknown[]) => {
  if (typeof args[0] === 'string' && args[0].includes('THREE.Clock')) return;
  _warn(...args);
};

import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HelmetProvider } from 'react-helmet-async';
import '@/i18n/i18n';
import '@/styles/globals.css';
import ErrorBoundary from '@/components/common/ErrorBoundary';
import ThemedApp from '@/components/ThemedApp';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 2 * 60 * 1000, // 2 minutes
      retry: 1,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <HelmetProvider>
      <QueryClientProvider client={queryClient}>
        <ErrorBoundary>
          <ThemedApp />
        </ErrorBoundary>
      </QueryClientProvider>
    </HelmetProvider>
  </StrictMode>
);
