import type { CSSProperties } from 'react';
import type { Theme } from './particleHelpers';

const layer: CSSProperties = {
  position: 'absolute',
  inset: 0,
  overflow: 'hidden',
  pointerEvents: 'none',
};

export const nebulaeContainer: CSSProperties = {
  ...layer,
  zIndex: -2,
};

export const canvasContainer: CSSProperties = {
  ...layer,
  zIndex: -1,
};

export const orbsContainer: CSSProperties = {
  ...layer,
  zIndex: -1,
};

const circle = (
  top: string,
  left: string,
  size: number,
  color: string,
  blur: number,
  animation: string
): CSSProperties => ({
  position: 'absolute',
  top,
  left,
  width: size,
  height: size,
  borderRadius: '50%',
  background: `radial-gradient(circle, ${color} 0%, transparent 70%)`,
  filter: `blur(${blur}px)`,
  animation,
});

export const nebula = {
  one: (theme: Theme): CSSProperties => {
    const rgb = theme === 'dark' ? '0,210,255' : '0,145,179';
    return {
      position: 'absolute',
      top: '-15%',
      left: '-10%',
      width: 900,
      height: 900,
      borderRadius: '50%',
      background: `radial-gradient(circle, rgba(${rgb},0.06) 0%, rgba(${rgb},0.02) 40%, transparent 70%)`,
      filter: 'blur(80px)',
      animation: 'nebDrift1 60s ease-in-out infinite',
    };
  },
  two: (theme: Theme): CSSProperties => {
    const rgb = theme === 'dark' ? '123,47,247' : '106,27,224';
    return {
      position: 'absolute',
      bottom: '-20%',
      right: '-10%',
      width: 800,
      height: 800,
      borderRadius: '50%',
      background: `radial-gradient(circle, rgba(${rgb},0.05) 0%, rgba(${rgb},0.02) 40%, transparent 70%)`,
      filter: 'blur(90px)',
      animation: 'nebDrift2 75s ease-in-out infinite',
    };
  },
  three: (theme: Theme): CSSProperties => {
    const rgb = theme === 'dark' ? '0,210,255' : '0,145,179';
    return {
      position: 'absolute',
      top: '40%',
      left: '50%',
      width: 600,
      height: 600,
      borderRadius: '50%',
      background: `radial-gradient(circle, rgba(${rgb},0.04) 0%, transparent 65%)`,
      filter: 'blur(70px)',
      animation: 'nebDrift3 90s ease-in-out infinite',
    };
  },
};

export const orb = {
  one: (theme: Theme): CSSProperties =>
    circle(
      '10%',
      '15%',
      320,
      theme === 'dark' ? 'rgba(0,210,255,0.18)' : 'rgba(0,145,179,0.18)',
      70,
      'orbFloat1 14s ease-in-out infinite'
    ),
  two: (theme: Theme): CSSProperties => ({
    ...circle(
      '',
      '',
      280,
      theme === 'dark' ? 'rgba(123,47,247,0.18)' : 'rgba(106,27,224,0.18)',
      70,
      'orbFloat2 16s ease-in-out infinite'
    ),
    top: undefined as unknown as string,
    left: undefined as unknown as string,
    bottom: '15%',
    right: '10%',
  }),
  three: (theme: Theme): CSSProperties =>
    circle(
      '50%',
      '55%',
      220,
      theme === 'dark' ? 'rgba(0,210,255,0.12)' : 'rgba(0,145,179,0.12)',
      60,
      'orbFloat3 18s ease-in-out infinite'
    ),
};

export const KEYFRAMES = `
  @keyframes nebDrift1 {
    0%, 100% { transform: translate(0, 0) scale(1); }
    50% { transform: translate(60px, 40px) scale(1.08); }
  }
  @keyframes nebDrift2 {
    0%, 100% { transform: translate(0, 0) scale(1); }
    50% { transform: translate(-50px, -30px) scale(1.05); }
  }
  @keyframes nebDrift3 {
    0%, 100% { transform: translate(-50%, -50%) scale(1); }
    50% { transform: translate(-45%, -55%) scale(1.1); }
  }
  @keyframes orbFloat1 {
    0%, 100% { transform: translate(0, 0) scale(1); }
    50% { transform: translate(40px, -30px) scale(1.1); }
  }
  @keyframes orbFloat2 {
    0%, 100% { transform: translate(0, 0) scale(1); }
    50% { transform: translate(-35px, 25px) scale(1.05); }
  }
  @keyframes orbFloat3 {
    0%, 100% { transform: translate(0, 0) scale(1); }
    50% { transform: translate(20px, 30px) scale(0.95); }
  }
`;
