import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import StatsSection from '../StatsSection';

// Mock i18n
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
    i18n: { language: 'fr', changeLanguage: vi.fn() },
  }),
}));

// Mock framer-motion to skip animations
vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...filterDomProps(props)}>{children}</div>,
    section: ({ children, ...props }: any) => <section {...filterDomProps(props)}>{children}</section>,
  },
  AnimatePresence: ({ children }: any) => <>{children}</>,
}));

// Filter out framer-motion specific props that aren't valid DOM attributes
function filterDomProps(props: Record<string, any>) {
  const invalid = ['initial', 'animate', 'exit', 'transition', 'whileInView', 'whileHover', 'viewport', 'variants'];
  const clean: Record<string, any> = {};
  for (const [key, value] of Object.entries(props)) {
    if (!invalid.includes(key)) clean[key] = value;
  }
  return clean;
}

// Mock useCountUp to return the target value immediately (no animation)
vi.mock('@/hooks/useCountUp', () => ({
  useCountUp: (target: number) => target,
}));

// Mock API
vi.mock('@/api/endpoints', () => ({
  getStats: vi.fn(() =>
    Promise.resolve({
      totalDocs: 42,
      totalDownloads: 500,
      totalContributors: 15,
      totalCourses: 8,
      weekUploads: 5,
    }),
  ),
}));

function renderWithProviders() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <StatsSection />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('StatsSection', () => {
  it('should display all four stat values after loading', async () => {
    renderWithProviders();

    await waitFor(() => {
      // Values under 1000 are displayed as-is by formatNumber
      expect(screen.getByText('42')).toBeInTheDocument();
      expect(screen.getByText('500')).toBeInTheDocument();
      expect(screen.getByText('15')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
    });
  });

  it('should display stat labels', async () => {
    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText('stats.docs')).toBeInTheDocument();
      expect(screen.getByText('stats.downloads')).toBeInTheDocument();
      expect(screen.getByText('stats.contributors')).toBeInTheDocument();
      expect(screen.getByText('stats.courses')).toBeInTheDocument();
    });
  });

  it('should display weekly upload counter', async () => {
    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText('+5')).toBeInTheDocument();
      expect(screen.getByText('stats.weekUploads')).toBeInTheDocument();
    });
  });
});
