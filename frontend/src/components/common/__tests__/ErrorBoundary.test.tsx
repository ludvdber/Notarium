import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import ErrorBoundary from '../ErrorBoundary';

// Mock i18n — ErrorBoundary uses i18n.t() directly (class component, no hooks)
vi.mock('@/i18n/i18n', () => ({
  default: {
    t: (key: string) => {
      const map: Record<string, string> = {
        'common.errorTitle': 'Oups',
        'common.error': 'Une erreur est survenue',
        'common.errorRetry': 'Retour',
      };
      return map[key] ?? key;
    },
  },
}));

// Suppress console.error for the intentional throw
const originalError = console.error;
beforeEach(() => {
  console.error = vi.fn();
});
afterEach(() => {
  console.error = originalError;
});

function ProblemChild() {
  throw new Error('Test crash');
}

describe('ErrorBoundary', () => {
  it('should render children normally', () => {
    render(
      <ErrorBoundary>
        <div data-testid="child">OK</div>
      </ErrorBoundary>,
    );
    expect(screen.getByTestId('child')).toBeInTheDocument();
  });

  it('should render fallback UI on error', () => {
    render(
      <ErrorBoundary>
        <ProblemChild />
      </ErrorBoundary>,
    );

    expect(screen.getByText('Oups')).toBeInTheDocument();
    expect(screen.getByText(/Une erreur est survenue/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Retour/i })).toBeInTheDocument();
  });
});
