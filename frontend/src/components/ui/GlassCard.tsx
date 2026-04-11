import { Card, type CardProps } from '@mui/material';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export default function GlassCard({ sx, children, ...props }: CardProps<any>) {
  return (
    <Card
      sx={{
        // Force transparent glass — the theme already sets this but we repeat it
        // so inline overrides can't silently break the glass effect.
        backgroundColor: (t) =>
          t.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.04)' : 'rgba(255, 255, 255, 0.7)',
        backgroundImage: 'none',
        backdropFilter: 'blur(24px)',
        WebkitBackdropFilter: 'blur(24px)',
        border: (t) =>
          t.palette.mode === 'dark'
            ? '1px solid rgba(255, 255, 255, 0.07)'
            : '1px solid rgba(0, 0, 0, 0.08)',
        borderRadius: 4,
        boxShadow: 'none',
        transition: 'transform 0.2s, box-shadow 0.2s, border-color 0.2s',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 8px 32px rgba(0, 210, 255, 0.08)',
        },
        ...sx,
      }}
      {...props}
    >
      {children}
    </Card>
  );
}
