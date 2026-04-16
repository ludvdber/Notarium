import { useState } from 'react';
import { Box, Typography, TextField, Button, Alert } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { motion, AnimatePresence } from 'framer-motion';
import GlassCard from '@/components/ui/GlassCard';

export default function Base64Converter() {
  const { t } = useTranslation();
  const [input, setInput] = useState('');
  const [output, setOutput] = useState('');
  const [error, setError] = useState('');
  const [mode, setMode] = useState<'encode' | 'decode'>('encode');

  const handleEncode = () => {
    setError('');
    setMode('encode');
    try {
      setOutput(btoa(unescape(encodeURIComponent(input))));
    } catch {
      setError(t('tools.base64.invalid'));
    }
  };

  const handleDecode = () => {
    setError('');
    setMode('decode');
    try {
      setOutput(decodeURIComponent(escape(atob(input))));
    } catch {
      setError(t('tools.base64.invalid'));
    }
  };

  return (
    <Box>
      <TextField
        fullWidth
        multiline
        rows={4}
        label={t('tools.base64.input')}
        placeholder={t('tools.base64.placeholder')}
        value={input}
        onChange={(e) => setInput(e.target.value)}
        slotProps={{ htmlInput: { style: { fontFamily: '"JetBrains Mono", monospace', fontSize: 14 } } }}
        sx={{ mb: 2 }}
      />

      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <Button variant="contained" onClick={handleEncode} sx={{ flex: 1 }}>
          {t('tools.base64.encode')}
        </Button>
        <Button variant="outlined" onClick={handleDecode} sx={{ flex: 1 }}>
          {t('tools.base64.decode')}
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <AnimatePresence mode="wait">
        {output && (
          <motion.div
            key={`${mode}-${output.slice(0, 20)}`}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
          >
            <GlassCard sx={{ p: 2.5 }}>
              <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500, mb: 1 }}>
                {t('tools.base64.output')}
              </Typography>
              <Typography
                className="mono"
                sx={{
                  fontWeight: 600,
                  color: 'primary.main',
                  wordBreak: 'break-all',
                  fontSize: 14,
                  whiteSpace: 'pre-wrap',
                }}
              >
                {output}
              </Typography>
            </GlassCard>
          </motion.div>
        )}
      </AnimatePresence>
    </Box>
  );
}
