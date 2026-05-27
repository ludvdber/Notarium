import { useMemo, useState } from 'react';
import { Box, Typography, TextField, Alert, Chip, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { Lock, LockOpen } from '@mui/icons-material';
import GlassCard from '@/components/ui/GlassCard';

type DecodedJwt =
  | { status: 'empty' }
  | { status: 'invalid'; reason: string }
  | { status: 'ok'; header: Record<string, unknown>; payload: Record<string, unknown>; signature: string };

function base64UrlToJson(part: string): unknown {
  const padded = part.replace(/-/g, '+').replace(/_/g, '/');
  // Add required '=' padding so atob accepts the input
  const pad = padded.length % 4 === 0 ? '' : '='.repeat(4 - (padded.length % 4));
  const binary = atob(padded + pad);
  // Handle UTF-8 correctly (atob yields Latin-1)
  const decoded = decodeURIComponent(Array.from(binary)
    .map((c) => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`)
    .join(''));
  return JSON.parse(decoded);
}

function decode(token: string): DecodedJwt {
  const trimmed = token.trim();
  if (!trimmed) return { status: 'empty' };
  const parts = trimmed.split('.');
  if (parts.length !== 3) return { status: 'invalid', reason: 'parts' };
  try {
    const header = base64UrlToJson(parts[0]) as Record<string, unknown>;
    const payload = base64UrlToJson(parts[1]) as Record<string, unknown>;
    return { status: 'ok', header, payload, signature: parts[2] };
  } catch {
    return { status: 'invalid', reason: 'decode' };
  }
}

function formatTimestamp(value: unknown): string | null {
  if (typeof value !== 'number') return null;
  try {
    return new Date(value * 1000).toLocaleString();
  } catch {
    return null;
  }
}

export default function JwtDecoder() {
  const { t } = useTranslation();
  const [token, setToken] = useState('');

  const decoded = useMemo(() => decode(token), [token]);

  const { expiresAt, issuedAt, isExpired } = useMemo(() => {
    if (decoded.status !== 'ok') return { expiresAt: null, issuedAt: null, isExpired: null };
    const exp = decoded.payload.exp;
    const iat = decoded.payload.iat;
    // Comparing the token's exp to the real wall-clock time is the whole point of the decoder —
    // intentional time dependency, recomputed each time the token changes.
    // eslint-disable-next-line react-hooks/purity
    const nowSec = Date.now() / 1000;
    return {
      expiresAt: formatTimestamp(exp),
      issuedAt: formatTimestamp(iat),
      isExpired: typeof exp === 'number' ? exp < nowSec : null,
    };
  }, [decoded]);

  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}>
      <GlassCard sx={{ p: 3 }}>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {t('tools.jwt.help')}
        </Typography>

        <TextField
          fullWidth
          multiline
          rows={4}
          label={t('tools.jwt.tokenLabel')}
          placeholder="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMifQ.xxxxx"
          value={token}
          onChange={(e) => setToken(e.target.value)}
          sx={{ mb: 2, '& textarea': { fontFamily: 'JetBrains Mono, monospace', fontSize: 13 } }}
        />

        {decoded.status === 'invalid' && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {decoded.reason === 'parts'
              ? t('tools.jwt.invalidFormat')
              : t('tools.jwt.invalidContent')}
          </Alert>
        )}

        {decoded.status === 'ok' && (
          <Stack spacing={2}>
            <Box>
              <Stack direction="row" spacing={1} sx={{ mb: 1, flexWrap: 'wrap' }}>
                {isExpired === true && (
                  <Chip size="small" color="error" icon={<Lock />} label={t('tools.jwt.expired')} />
                )}
                {isExpired === false && (
                  <Chip size="small" color="success" icon={<LockOpen />} label={t('tools.jwt.valid')} />
                )}
                {issuedAt && <Chip size="small" variant="outlined" label={`${t('tools.jwt.issuedAt')}: ${issuedAt}`} />}
                {expiresAt && <Chip size="small" variant="outlined" label={`${t('tools.jwt.expiresAt')}: ${expiresAt}`} />}
              </Stack>
            </Box>

            <Box>
              <Typography variant="subtitle2" sx={{ mb: 0.5, fontWeight: 700 }}>
                {t('tools.jwt.header')}
              </Typography>
              <Box
                component="pre"
                sx={{
                  m: 0,
                  p: 1.5,
                  borderRadius: 1.5,
                  bgcolor: (th) => th.palette.mode === 'dark' ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.04)',
                  fontFamily: 'JetBrains Mono, monospace',
                  fontSize: 12,
                  overflow: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all',
                }}
              >
                {JSON.stringify(decoded.header, null, 2)}
              </Box>
            </Box>

            <Box>
              <Typography variant="subtitle2" sx={{ mb: 0.5, fontWeight: 700 }}>
                {t('tools.jwt.payload')}
              </Typography>
              <Box
                component="pre"
                sx={{
                  m: 0,
                  p: 1.5,
                  borderRadius: 1.5,
                  bgcolor: (th) => th.palette.mode === 'dark' ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.04)',
                  fontFamily: 'JetBrains Mono, monospace',
                  fontSize: 12,
                  overflow: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all',
                }}
              >
                {JSON.stringify(decoded.payload, null, 2)}
              </Box>
            </Box>

            <Box>
              <Typography variant="subtitle2" sx={{ mb: 0.5, fontWeight: 700 }}>
                {t('tools.jwt.signature')}
              </Typography>
              <Typography
                sx={{
                  fontFamily: 'JetBrains Mono, monospace',
                  fontSize: 12,
                  opacity: 0.7,
                  wordBreak: 'break-all',
                }}
              >
                {decoded.signature}
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                {t('tools.jwt.signatureNote')}
              </Typography>
            </Box>
          </Stack>
        )}
      </GlassCard>
    </motion.div>
  );
}
