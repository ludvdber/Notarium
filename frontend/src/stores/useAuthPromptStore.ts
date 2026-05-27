import { create } from 'zustand';

/**
 * Minimal global snackbar used to prompt an anonymous user to log in when they try to
 * activate a protected navigation link. Decoupled from React Router so *any* component
 * can trigger it (Navbar, MobileMenu, footer CTAs, home "Voir tout" links…).
 */
interface AuthPromptState {
  open: boolean;
  /** Translation key (or raw message) to display. Default: `auth.loginRequired`. */
  messageKey: string;
  show: (messageKey?: string) => void;
  close: () => void;
}

export const useAuthPromptStore = create<AuthPromptState>((set) => ({
  open: false,
  messageKey: 'auth.loginRequired',
  show: (messageKey = 'auth.loginRequired') => set({ open: true, messageKey }),
  close: () => set({ open: false }),
}));
