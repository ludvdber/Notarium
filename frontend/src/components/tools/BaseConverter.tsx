import { useState, useMemo } from 'react';
import {
  Box, Typography, TextField, Grid, Select, MenuItem,
  FormControl, InputLabel, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { motion, AnimatePresence } from 'framer-motion';
import GlassCard from '@/components/ui/GlassCard';
import {
  convertBase, isValidForBase, generateAsciiTable,
  type Base, BASE_NAMES,
} from '@/lib/baseConverter';

const BASES: Base[] = [2, 8, 10, 16];

function AsciiTable({ onSelect }: { onSelect: (dec: number) => void }) {
  const { t } = useTranslation();
  const entries = useMemo(() => generateAsciiTable(), []);
  const [filter, setFilter] = useState<'all' | 'printable' | 'control'>('printable');

  const filtered = entries.filter((e) => {
    if (filter === 'printable') return e.dec >= 33 && e.dec <= 126;
    if (filter === 'control') return e.dec < 33 || e.dec === 127;
    return true;
  });

  return (
    <GlassCard sx={{ mt: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 2, pb: 1 }}>
        <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
          {t('tools.base.asciiTable')}
        </Typography>
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          {(['printable', 'control', 'all'] as const).map((f) => (
            <Chip
              key={f}
              label={f.charAt(0).toUpperCase() + f.slice(1)}
              size="small"
              variant={filter === f ? 'filled' : 'outlined'}
              color={filter === f ? 'primary' : 'default'}
              onClick={() => setFilter(f)}
              sx={{ fontSize: 11, cursor: 'pointer' }}
            />
          ))}
        </Box>
      </Box>
      <TableContainer sx={{ maxHeight: 400 }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 700, fontSize: 11 }}>{t('tools.base.char')}</TableCell>
              <TableCell sx={{ fontWeight: 700, fontSize: 11 }}>{t('tools.base.dec')}</TableCell>
              <TableCell sx={{ fontWeight: 700, fontSize: 11 }}>{t('tools.base.hexCol')}</TableCell>
              <TableCell sx={{ fontWeight: 700, fontSize: 11 }}>{t('tools.base.octCol')}</TableCell>
              <TableCell sx={{ fontWeight: 700, fontSize: 11 }}>{t('tools.base.binCol')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filtered.map((entry) => (
              <TableRow
                key={entry.dec}
                hover
                onClick={() => onSelect(entry.dec)}
                sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
              >
                <TableCell sx={{ fontFamily: '"JetBrains Mono", monospace', fontSize: 12, fontWeight: 600 }}>
                  {entry.char}
                </TableCell>
                <TableCell className="mono" sx={{ fontSize: 12 }}>{entry.dec}</TableCell>
                <TableCell className="mono" sx={{ fontSize: 12 }}>{entry.hex}</TableCell>
                <TableCell className="mono" sx={{ fontSize: 12 }}>{entry.oct}</TableCell>
                <TableCell className="mono" sx={{ fontSize: 12 }}>{entry.bin}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </GlassCard>
  );
}

export default function BaseConverter() {
  const { t } = useTranslation();
  const [input, setInput] = useState('42');
  const [base, setBase] = useState<Base>(10);

  const valid = isValidForBase(input, base);
  const results = useMemo(() => {
    if (!input || !valid) return null;
    return convertBase(input, base);
  }, [input, base, valid]);

  const handleInput = (val: string) => {
    setInput(val);
  };

  const handleBaseChange = (newBase: Base) => {
    // Convert current value to new base before switching
    if (results && input) {
      setInput(results[newBase]);
    }
    setBase(newBase);
  };

  const handleAsciiSelect = (dec: number) => {
    setBase(10);
    setInput(dec.toString());
  };

  const baseLabel = (b: Base): string => t(`tools.base.${BASE_NAMES[b]}`);

  return (
    <Box>
      {/* Input row */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 7 }}>
          <TextField
            fullWidth
            label={t('tools.base.input')}
            value={input}
            onChange={(e) => handleInput(e.target.value)}
            error={input.length > 0 && !valid}
            helperText={input.length > 0 && !valid ? t('tools.base.invalidChar') : ' '}
            slotProps={{ htmlInput: { style: { fontFamily: '"JetBrains Mono", monospace', fontSize: 18 } } }}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 5 }}>
          <FormControl fullWidth>
            <InputLabel>{t('tools.base.sourceBase')}</InputLabel>
            <Select
              value={base}
              label={t('tools.base.sourceBase')}
              onChange={(e) => handleBaseChange(e.target.value as Base)}
            >
              {BASES.map((b) => (
                <MenuItem key={b} value={b}>{baseLabel(b)} (base {b})</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>
      </Grid>

      {/* Results */}
      <AnimatePresence mode="wait">
        {results && (
          <motion.div
            key={`${input}-${base}`}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
          >
            <GlassCard sx={{ p: 0 }}>
              {BASES.filter((b) => b !== base).map((b, i, arr) => (
                <Box
                  key={b}
                  sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    px: 2.5,
                    py: 2,
                    borderBottom: i < arr.length - 1 ? '1px solid' : 'none',
                    borderColor: 'divider',
                  }}
                >
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                      {baseLabel(b)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      base {b}
                    </Typography>
                  </Box>
                  <Typography
                    className="mono"
                    sx={{
                      fontWeight: 700,
                      color: 'primary.main',
                      fontSize: { xs: 16, md: 20 },
                      wordBreak: 'break-all',
                      textAlign: 'right',
                      maxWidth: '60%',
                    }}
                  >
                    {results[b]}
                  </Typography>
                </Box>
              ))}
            </GlassCard>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ASCII Table */}
      <AsciiTable onSelect={handleAsciiSelect} />
    </Box>
  );
}
