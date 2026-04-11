import * as THREE from 'three';

export const PARTICLE_COUNT = 350;
export const BOUNDS_X = 14;
export const BOUNDS_Y = 10;
export const BOUNDS_Z = 6;

export type Theme = 'dark' | 'light';

export interface ParticleBuffers {
  positions: Float32Array;
  velocities: Float32Array;
  sizes: Float32Array;
  colors: Float32Array;
  glowMask: Float32Array;
}

/**
 * Canvas-based radial gradient texture — gives points a soft circular falloff
 * instead of the default square sprite.
 */
export function makeCircleTexture(): THREE.CanvasTexture {
  const size = 64;
  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext('2d')!;
  const gradient = ctx.createRadialGradient(size / 2, size / 2, 0, size / 2, size / 2, size / 2);
  gradient.addColorStop(0, 'rgba(255,255,255,1)');
  gradient.addColorStop(0.4, 'rgba(255,255,255,0.8)');
  gradient.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = gradient;
  ctx.fillRect(0, 0, size, size);
  const tex = new THREE.CanvasTexture(canvas);
  tex.needsUpdate = true;
  return tex;
}

/**
 * Picks a color from the cyan/violet/white palette (inverted for light theme).
 */
export function pickColor(theme: Theme): THREE.Color {
  const r = Math.random();
  if (theme === 'dark') {
    if (r < 0.4) return new THREE.Color('#00d2ff');
    if (r < 0.75) return new THREE.Color('#7b2ff7');
    return new THREE.Color('#ffffff');
  }
  if (r < 0.45) return new THREE.Color('#0091b3');
  if (r < 0.85) return new THREE.Color('#6a1be0');
  return new THREE.Color('#1a1a2e');
}

/**
 * Generates the typed arrays that drive the particle system. Size distribution:
 * 70% tiny dust, 25% medium specks, 5% large glowing stars.
 */
export function buildParticleBuffers(theme: Theme): ParticleBuffers {
  const positions = new Float32Array(PARTICLE_COUNT * 3);
  const velocities = new Float32Array(PARTICLE_COUNT * 3);
  const sizes = new Float32Array(PARTICLE_COUNT);
  const colors = new Float32Array(PARTICLE_COUNT * 3);
  const glowMask = new Float32Array(PARTICLE_COUNT);

  for (let i = 0; i < PARTICLE_COUNT; i++) {
    positions[i * 3] = (Math.random() - 0.5) * BOUNDS_X * 2;
    positions[i * 3 + 1] = (Math.random() - 0.5) * BOUNDS_Y * 2;
    positions[i * 3 + 2] = (Math.random() - 0.5) * BOUNDS_Z * 2;

    velocities[i * 3] = (Math.random() - 0.5) * 0.04;
    velocities[i * 3 + 1] = (Math.random() - 0.5) * 0.04;
    velocities[i * 3 + 2] = (Math.random() - 0.5) * 0.02;

    const sizeRoll = Math.random();
    if (sizeRoll < 0.7) {
      sizes[i] = 0.02 + Math.random() * 0.03;
    } else if (sizeRoll < 0.95) {
      sizes[i] = 0.06 + Math.random() * 0.05;
    } else {
      sizes[i] = 0.13 + Math.random() * 0.08;
    }
    glowMask[i] = sizeRoll >= 0.95 ? 1 : 0;

    const col = pickColor(theme);
    const opacity = 0.1 + Math.random() * 0.5;
    colors[i * 3] = col.r * opacity;
    colors[i * 3 + 1] = col.g * opacity;
    colors[i * 3 + 2] = col.b * opacity;
  }

  return { positions, velocities, sizes, colors, glowMask };
}

/**
 * Wraps a particle position around the scene bounds so drift is infinite.
 */
export function wrapPosition(positions: Float32Array, i3: number): void {
  if (positions[i3] > BOUNDS_X) positions[i3] = -BOUNDS_X;
  else if (positions[i3] < -BOUNDS_X) positions[i3] = BOUNDS_X;
  if (positions[i3 + 1] > BOUNDS_Y) positions[i3 + 1] = -BOUNDS_Y;
  else if (positions[i3 + 1] < -BOUNDS_Y) positions[i3 + 1] = BOUNDS_Y;
  if (positions[i3 + 2] > BOUNDS_Z) positions[i3 + 2] = -BOUNDS_Z;
  else if (positions[i3 + 2] < -BOUNDS_Z) positions[i3 + 2] = BOUNDS_Z;
}
