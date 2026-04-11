import type { Theme } from './particleHelpers';
import * as s from './particles.styles';

interface NebulaeProps {
  theme: Theme;
}

export default function Nebulae({ theme }: NebulaeProps) {
  return (
    <div aria-hidden style={s.nebulaeContainer}>
      <div style={s.nebula.one(theme)} />
      <div style={s.nebula.two(theme)} />
      <div style={s.nebula.three(theme)} />
      <style>{s.KEYFRAMES}</style>
    </div>
  );
}
