import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import SectionsGrid from '../SectionsGrid';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
    i18n: { language: 'fr', changeLanguage: vi.fn() },
  }),
}));

vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...filterDomProps(props)}>{children}</div>,
    section: ({ children, ...props }: any) => <section {...filterDomProps(props)}>{children}</section>,
  },
  AnimatePresence: ({ children }: any) => <>{children}</>,
}));

function filterDomProps(props: Record<string, any>) {
  const invalid = ['initial', 'animate', 'exit', 'transition', 'whileInView', 'whileHover', 'viewport', 'variants'];
  const clean: Record<string, any> = {};
  for (const [key, value] of Object.entries(props)) {
    if (!invalid.includes(key)) clean[key] = value;
  }
  return clean;
}

const mockSections = [
  { id: 1, name: 'Informatique', documentCount: 25 },
  { id: 2, name: 'Comptabilité', documentCount: 12 },
  { id: 3, name: 'Marketing', documentCount: 8 },
  { id: 4, name: 'Fiscalité', documentCount: 5 },
];

vi.mock('@/api/endpoints', () => ({
  getSections: vi.fn(() => Promise.resolve(mockSections)),
}));

function renderWithProviders() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <SectionsGrid />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('SectionsGrid', () => {
  it('should display all section names', async () => {
    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText('Informatique')).toBeInTheDocument();
      expect(screen.getByText('Comptabilité')).toBeInTheDocument();
      expect(screen.getByText('Marketing')).toBeInTheDocument();
      expect(screen.getByText('Fiscalité')).toBeInTheDocument();
    });
  });

  it('should display document counts', async () => {
    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText('25')).toBeInTheDocument();
      expect(screen.getByText('12')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('5')).toBeInTheDocument();
    });
  });

  it('should have section title', async () => {
    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText('sections.title')).toBeInTheDocument();
    });
  });
});
