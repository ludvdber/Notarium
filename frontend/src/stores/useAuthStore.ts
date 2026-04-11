import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '@/types';

interface AuthState {
  user: User | null;
  token: string | null;
  isVerified: boolean;
  isAdmin: boolean;
  login: (token: string) => void;
  logout: () => void;
  setUser: (user: User) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isVerified: false,
      isAdmin: false,

      login: (token: string) => {
        set({ token });
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          set({
            isVerified: payload.verified ?? false,
            isAdmin: payload.role === 'ADMIN',
          });
        } catch {
          set({ isVerified: false, isAdmin: false });
        }
      },

      logout: () => set({ user: null, token: null, isVerified: false, isAdmin: false }),

      setUser: (user: User) => set({ user }),
    }),
    {
      name: 'notarium-auth',
    }
  )
);
