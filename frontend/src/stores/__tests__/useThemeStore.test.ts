import { describe, it, expect, beforeEach } from 'vitest';
import { useThemeStore } from '../useThemeStore';

describe('useThemeStore', () => {
  beforeEach(() => {
    useThemeStore.setState({ theme: 'dark' });
  });

  it('should default to dark theme', () => {
    expect(useThemeStore.getState().theme).toBe('dark');
  });

  it('should toggle to light', () => {
    useThemeStore.getState().toggle();
    expect(useThemeStore.getState().theme).toBe('light');
  });

  it('should toggle back to dark', () => {
    useThemeStore.getState().toggle();
    useThemeStore.getState().toggle();
    expect(useThemeStore.getState().theme).toBe('dark');
  });
});
