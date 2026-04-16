import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAuthStore } from '../useAuthStore';
import type { User } from '@/types';

// Prevent actual fetch calls in logout
vi.stubGlobal('fetch', vi.fn(() => Promise.resolve()));

const mockUser: User = {
  id: 1,
  username: 'testuser',
  xp: 100,
  bio: null,
  website: null,
  github: null,
  linkedin: null,
  discord: null,
  badges: [],
  documentCount: 5,
  profilePublic: true,
  supporter: false,
  termsAccepted: true,
};

describe('useAuthStore', () => {
  beforeEach(() => {
    // Reset store to initial state
    useAuthStore.setState({
      user: null,
      token: null,
      isVerified: false,
      isAdmin: false,
    });
  });

  it('should set user and token on loginFromUser', () => {
    useAuthStore.getState().loginFromUser(mockUser);

    const state = useAuthStore.getState();
    expect(state.user).toEqual(mockUser);
    expect(state.token).toBe('cookie');
    expect(state.isVerified).toBe(true);
  });

  it('should clear user on logout', () => {
    useAuthStore.getState().loginFromUser(mockUser);
    useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
    expect(state.isVerified).toBe(false);
    expect(state.isAdmin).toBe(false);
  });

  it('should decode JWT claims on login(token)', () => {
    // Craft a JWT with verified=true and role=ADMIN in payload
    const payload = { verified: true, role: 'ADMIN', sub: '1' };
    const b64 = btoa(JSON.stringify(payload));
    const fakeJwt = `header.${b64}.signature`;

    useAuthStore.getState().login(fakeJwt);

    const state = useAuthStore.getState();
    expect(state.token).toBe(fakeJwt);
    expect(state.isVerified).toBe(true);
    expect(state.isAdmin).toBe(true);
  });

  it('should set isAdmin=false for non-admin role', () => {
    const payload = { verified: true, role: 'USER', sub: '1' };
    const b64 = btoa(JSON.stringify(payload));
    const fakeJwt = `header.${b64}.signature`;

    useAuthStore.getState().login(fakeJwt);

    expect(useAuthStore.getState().isAdmin).toBe(false);
  });

  it('should update user via setUser', () => {
    useAuthStore.getState().loginFromUser(mockUser);
    const updatedUser = { ...mockUser, username: 'updated' };
    useAuthStore.getState().setUser(updatedUser);

    expect(useAuthStore.getState().user?.username).toBe('updated');
  });
});
