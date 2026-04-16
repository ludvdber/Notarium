import { useRef, useMemo, useEffect } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import {
  PARTICLE_COUNT,
  buildParticleBuffers,
  makeCircleTexture,
  wrapPosition,
  type Theme,
} from './particleHelpers';

interface CosmicDustProps {
  theme: Theme;
}

export default function CosmicDust({ theme }: CosmicDustProps) {
  const pointsRef = useRef<THREE.Points>(null);
  const glowRef = useRef<THREE.Points>(null);

  const mouseNdc = useRef({ x: 0, y: 0 });
  const parallaxTarget = useRef({ x: 0, y: 0 });
  const parallaxCurrent = useRef({ x: 0, y: 0 });

  const circleTex = useMemo(() => makeCircleTexture(), []);
  const buffers = useMemo(() => buildParticleBuffers(theme), [theme]);
  const buffersRef = useRef(buffers);
  useEffect(() => { buffersRef.current = buffers; }, [buffers]);
  const { sizes, colors, glowMask } = buffers;

  const glowPositions = useMemo(() => {
    const pos = buffers.positions;
    const arr: number[] = [];
    for (let i = 0; i < PARTICLE_COUNT; i++) {
      if (glowMask[i] === 1) {
        arr.push(pos[i * 3], pos[i * 3 + 1], pos[i * 3 + 2]);
      }
    }
    return new Float32Array(arr);
  }, [buffers.positions, glowMask]);

  const glowColors = useMemo(() => {
    const arr: number[] = [];
    for (let i = 0; i < PARTICLE_COUNT; i++) {
      if (glowMask[i] === 1) {
        arr.push(colors[i * 3] * 1.5, colors[i * 3 + 1] * 1.5, colors[i * 3 + 2] * 1.5);
      }
    }
    return new Float32Array(arr);
  }, [colors, glowMask]);

  useEffect(() => {
    const onMove = (e: PointerEvent) => {
      mouseNdc.current.x = (e.clientX / window.innerWidth) * 2 - 1;
      mouseNdc.current.y = -(e.clientY / window.innerHeight) * 2 + 1;
      parallaxTarget.current.x = mouseNdc.current.x * 0.4;
      parallaxTarget.current.y = mouseNdc.current.y * 0.4;
    };
    window.addEventListener('pointermove', onMove);
    return () => window.removeEventListener('pointermove', onMove);
  }, []);

  useFrame((_, delta) => {
    const dt = Math.min(delta, 0.05);
    const { positions, velocities } = buffersRef.current;

    parallaxCurrent.current.x += (parallaxTarget.current.x - parallaxCurrent.current.x) * 0.05;
    parallaxCurrent.current.y += (parallaxTarget.current.y - parallaxCurrent.current.y) * 0.05;

    for (let i = 0; i < PARTICLE_COUNT; i++) {
      const i3 = i * 3;
      positions[i3] += velocities[i3] * dt;
      positions[i3 + 1] += velocities[i3 + 1] * dt;
      positions[i3 + 2] += velocities[i3 + 2] * dt;
      wrapPosition(positions, i3);
    }

    if (pointsRef.current) {
      pointsRef.current.geometry.attributes.position.needsUpdate = true;
      pointsRef.current.position.x = parallaxCurrent.current.x;
      pointsRef.current.position.y = parallaxCurrent.current.y;
    }

    if (glowRef.current) {
      const attr = glowRef.current.geometry.attributes.position as THREE.BufferAttribute;
      let k = 0;
      for (let i = 0; i < PARTICLE_COUNT; i++) {
        if (glowMask[i] === 1) {
          attr.array[k * 3] = positions[i * 3];
          attr.array[k * 3 + 1] = positions[i * 3 + 1];
          attr.array[k * 3 + 2] = positions[i * 3 + 2];
          k++;
        }
      }
      attr.needsUpdate = true;
      glowRef.current.position.x = parallaxCurrent.current.x * 1.6;
      glowRef.current.position.y = parallaxCurrent.current.y * 1.6;
    }
  });

  return (
    <>
      <points ref={pointsRef}>
        <bufferGeometry>
          <bufferAttribute attach="attributes-position" args={[buffers.positions, 3]} />
          <bufferAttribute attach="attributes-color" args={[colors, 3]} />
          <bufferAttribute attach="attributes-size" args={[sizes, 1]} />
        </bufferGeometry>
        <pointsMaterial
          size={0.08}
          vertexColors
          transparent
          sizeAttenuation
          depthWrite={false}
          map={circleTex}
          alphaTest={0.01}
          blending={THREE.AdditiveBlending}
        />
      </points>

      <points ref={glowRef}>
        <bufferGeometry>
          <bufferAttribute attach="attributes-position" args={[glowPositions, 3]} />
          <bufferAttribute attach="attributes-color" args={[glowColors, 3]} />
        </bufferGeometry>
        <pointsMaterial
          size={0.55}
          vertexColors
          transparent
          opacity={0.35}
          sizeAttenuation
          depthWrite={false}
          map={circleTex}
          alphaTest={0.01}
          blending={THREE.AdditiveBlending}
        />
      </points>
    </>
  );
}
