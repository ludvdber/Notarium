import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ProtectedRoute from '../ProtectedRoute';
import { useAuthStore } from '@/stores/useAuthStore';

// Mock i18n — return keys as text
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
    i18n: { language: 'fr', changeLanguage: vi.fn() },
  }),
}));

function renderProtected(props: { requireVerified?: boolean; requireAdmin?: boolean } = {}) {
  return render(
    <MemoryRouter>
      <ProtectedRoute {...props}>
        <div data-testid="child-content">Protected content</div>
      </ProtectedRoute>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    useAuthStore.setState({
      user: null,
      token: null,
      isVerified: false,
      isAdmin: false,
    });
  });

  it('should redirect when no token (not authenticated)', () => {
    renderProtected();
    expect(screen.queryByTestId('child-content')).not.toBeInTheDocument();
  });

  it('should show verification message when not verified and requireVerified', () => {
    useAuthStore.setState({ token: 'cookie', isVerified: false });

    renderProtected({ requireVerified: true });

    expect(screen.getByText('auth.verificationRequired')).toBeInTheDocument();
    expect(screen.queryByTestId('child-content')).not.toBeInTheDocument();
  });

  it('should render children when verified', () => {
    useAuthStore.setState({ token: 'cookie', isVerified: true });

    renderProtected({ requireVerified: true });

    expect(screen.getByTestId('child-content')).toBeInTheDocument();
  });

  it('should redirect when not admin and requireAdmin', () => {
    useAuthStore.setState({ token: 'cookie', isVerified: true, isAdmin: false });

    renderProtected({ requireAdmin: true });

    expect(screen.queryByTestId('child-content')).not.toBeInTheDocument();
  });

  it('should render children when admin', () => {
    useAuthStore.setState({ token: 'cookie', isVerified: true, isAdmin: true });

    renderProtected({ requireAdmin: true });

    expect(screen.getByTestId('child-content')).toBeInTheDocument();
  });
});
