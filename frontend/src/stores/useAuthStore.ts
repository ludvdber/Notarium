import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { logoutApi } from '@/api/endpoints';
import type { User } from '@/types';

interface AuthState {
  user: User | null;
  token: string | null; // kept for backward compat checks (non-null = logged in)
  isVerified: boolean;
  isAdmin: boolean;
  login: (token: string) => void;
  loginFromUser: (user: User) => void;
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

      loginFromUser: (user: User) => {
        set({
          user,
          token: 'cookie', // sentinel — real JWT is in HttpOnly cookie
          isVerified: true, // if we can call /users/me the cookie is valid
          isAdmin: false, // will be set from the JWT claims once we have them
        });
      },

      logout: () => {
        set({ user: null, token: null, isVerified: false, isAdmin: false });
        logoutApi().catch(() => {});
      },

      setUser: (user: User) => set({ user }),
    }),
    {
      name: 'freenote-auth',
    }
  )
);
