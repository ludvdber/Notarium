import type { Theme } from './particleHelpers';
import * as s from './particles.styles';

interface OrbsFallbackProps {
  theme: Theme;
}

export default function OrbsFallback({ theme }: OrbsFallbackProps) {
  return (
    <div aria-hidden style={s.orbsContainer}>
      <div style={s.orb.one(theme)} />
      <div style={s.orb.two(theme)} />
      <div style={s.orb.three(theme)} />
      <style>{s.KEYFRAMES}</style>
    </div>
  );
}
