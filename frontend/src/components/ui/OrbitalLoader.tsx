import { Box, keyframes } from '@mui/material';

const orbit = keyframes`
  0% { transform: rotate(0deg) translateX(18px) rotate(0deg); }
  100% { transform: rotate(360deg) translateX(18px) rotate(-360deg); }
`;

const core = keyframes`
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
`;

interface Props {
  size?: number;
  label?: string;
}

export default function OrbitalLoader({ size = 48, label = 'Loading' }: Props) {
  const scale = size / 48;
  return (
    <Box
      role="status"
      aria-live="polite"
      aria-label={label}
      sx={{
        position: 'relative',
        width: size,
        height: size,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      {/* Central core — violet */}
      <Box
        sx={{
          position: 'absolute',
          width: 8 * scale,
          height: 8 * scale,
          borderRadius: '50%',
          background: (t) => t.palette.secondary.main,
          boxShadow: (t) => `0 0 12px ${t.palette.secondary.main}`,
          animation: `${core} 1.6s ease-in-out infinite`,
        }}
      />
      {/* 3 orbiting dots, staggered */}
      {[0, 1, 2].map((i) => (
        <Box
          key={i}
          sx={{
            position: 'absolute',
            width: 5 * scale,
            height: 5 * scale,
            borderRadius: '50%',
            background: (t) => t.palette.primary.main,
            boxShadow: (t) => `0 0 6px ${t.palette.primary.main}`,
            animation: `${orbit} 1.4s linear infinite`,
            animationDelay: `${i * -0.47}s`,
          }}
        />
      ))}
    </Box>
  );
}
