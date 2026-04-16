import { Canvas } from '@react-three/fiber';
import { useMediaQuery } from '@/hooks/useMediaQuery';
import CosmicDust from './CosmicDust';
import Nebulae from './Nebulae';
import OrbsFallback from './OrbsFallback';
import { canvasContainer } from './particles.styles';
import type { Theme } from './particleHelpers';

interface HeroBackgroundProps {
  theme: Theme;
}

export default function HeroBackground({ theme }: HeroBackgroundProps) {
  const isMobile = useMediaQuery('(max-width: 768px)');

  if (isMobile) {
    return <OrbsFallback theme={theme} />;
  }

  return (
    <>
      <Nebulae theme={theme} />
      <div style={canvasContainer}>
        <Canvas
          camera={{ position: [0, 0, 8], fov: 60 }}
          gl={{ antialias: true, alpha: true, powerPreference: 'high-performance' }}
          dpr={[1, 1.75]}
          style={{ pointerEvents: 'none' }}
        >
          <CosmicDust theme={theme} />
        </Canvas>
      </div>
    </>
  );
}
