import { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Chip,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Add, EventBusy, Delete } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { getAllMandates, assignDelegate, endDelegate, deleteMandate, getSections } from '@/api/endpoints';
import { formatDate } from '@/lib/utils';
import GlassCard from '@/components/ui/GlassCard';
import type { DelegateMember } from '@/types';

export default function AdminDelegates() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { data: mandates, isLoading } = useQuery({ queryKey: ['admin-mandates'], queryFn: getAllMandates });
  const { data: sections } = useQuery({ queryKey: ['sections'], queryFn: getSections });

  const [showForm, setShowForm] = useState(false);
  const [userId, setUserId] = useState('');
  const [sectionId, setSectionId] = useState('');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [endingId, setEndingId] = useState<number | null>(null);
  const [endDateVal, setEndDateVal] = useState(new Date().toISOString().split('T')[0]);

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['admin-mandates'] });
    queryClient.invalidateQueries({ queryKey: ['delegates'] });
  };

  const assignMut = useMutation({
    mutationFn: () =>
      assignDelegate({ userId: Number(userId), sectionId: Number(sectionId), startDate }),
    onSuccess: () => {
      invalidate();
      setShowForm(false);
      setUserId('');
      setSectionId('');
    },
  });

  const endMut = useMutation({
    mutationFn: (id: number) => endDelegate(id, { endDate: endDateVal }),
    onSuccess: () => {
      invalidate();
      setEndingId(null);
    },
  });

  const deleteMut = useMutation({
    mutationFn: (id: number) => deleteMandate(id),
    onSuccess: invalidate,
  });

  const active = mandates?.filter((m) => !m.endDate) ?? [];
  const past = mandates?.filter((m) => m.endDate) ?? [];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      {/* Header + Add button */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h5" sx={{ fontWeight: 800 }}>
          {t('admin.delegates.title')}
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => setShowForm((v) => !v)}
          size="small"
        >
          {t('admin.delegates.assign')}
        </Button>
      </Box>

      {/* Assign form */}
      {showForm && (
        <GlassCard sx={{ p: 2.5 }}>
          <Typography variant="subtitle2" sx={{ mb: 2, fontWeight: 700 }}>
            {t('admin.delegates.assignTitle')}
          </Typography>

          {assignMut.isError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {(assignMut.error as Error).message || t('common.error')}
            </Alert>
          )}

          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <TextField
              label={t('admin.delegates.userId')}
              type="number"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              size="small"
              sx={{ width: 120 }}
            />
            <FormControl size="small" sx={{ minWidth: 180 }}>
              <InputLabel>{t('admin.delegates.section')}</InputLabel>
              <Select
                value={sectionId}
                label={t('admin.delegates.section')}
                onChange={(e) => setSectionId(e.target.value)}
              >
                {sections?.map((s) => (
                  <MenuItem key={s.id} value={String(s.id)}>{s.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label={t('admin.delegates.startDate')}
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              size="small"
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <Button
              variant="contained"
              onClick={() => assignMut.mutate()}
              disabled={!userId || !sectionId || assignMut.isPending}
              size="small"
            >
              {assignMut.isPending ? t('common.loading') : t('admin.delegates.confirm')}
            </Button>
          </Box>
        </GlassCard>
      )}

      {/* Active mandates */}
      <GlassCard sx={{ p: 2.5 }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>
          {t('admin.delegates.active')} ({active.length})
        </Typography>
        {isLoading && <Typography color="text.secondary">{t('common.loading')}</Typography>}
        {!isLoading && active.length === 0 && (
          <Typography color="text.secondary" variant="body2">
            {t('admin.delegates.noActive')}
          </Typography>
        )}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          {active.map((m) => (
            <MandateRow
              key={m.id}
              mandate={m}
              isEnding={endingId === m.id}
              endDateVal={endDateVal}
              onEndDateChange={setEndDateVal}
              onStartEnding={() => {
                setEndingId(m.id);
                setEndDateVal(new Date().toISOString().split('T')[0]);
              }}
              onConfirmEnd={() => endMut.mutate(m.id)}
              onCancelEnd={() => setEndingId(null)}
              onDelete={() => deleteMut.mutate(m.id)}
              isPending={endMut.isPending}
              t={t}
            />
          ))}
        </Box>
      </GlassCard>

      {/* Past mandates */}
      {past.length > 0 && (
        <GlassCard sx={{ p: 2.5 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>
            {t('admin.delegates.history')} ({past.length})
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {past.map((m) => (
              <MandateRow
                key={m.id}
                mandate={m}
                isEnding={false}
                endDateVal=""
                onEndDateChange={() => {}}
                onStartEnding={() => {}}
                onConfirmEnd={() => {}}
                onCancelEnd={() => {}}
                onDelete={() => deleteMut.mutate(m.id)}
                isPending={false}
                t={t}
              />
            ))}
          </Box>
        </GlassCard>
      )}
    </Box>
  );
}

interface MandateRowProps {
  mandate: DelegateMember;
  isEnding: boolean;
  endDateVal: string;
  onEndDateChange: (val: string) => void;
  onStartEnding: () => void;
  onConfirmEnd: () => void;
  onCancelEnd: () => void;
  onDelete: () => void;
  isPending: boolean;
  t: (key: string) => string;
}

function MandateRow({
  mandate: m,
  isEnding,
  endDateVal,
  onEndDateChange,
  onStartEnding,
  onConfirmEnd,
  onCancelEnd,
  onDelete,
  isPending,
  t,
}: MandateRowProps) {
  const { i18n } = useTranslation();
  const isActive = !m.endDate;

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1.5,
        px: 2,
        py: 1.5,
        borderRadius: 2,
        bgcolor: 'rgba(255,255,255,0.02)',
        border: '1px solid rgba(255,255,255,0.05)',
        flexWrap: 'wrap',
      }}
    >
      <Typography variant="body2" sx={{ fontWeight: 700, minWidth: 100 }}>
        {m.username}
      </Typography>
      <Chip
        label={isActive ? t('admin.delegates.activeChip') : t('admin.delegates.endedChip')}
        size="small"
        color={isActive ? 'success' : 'default'}
        variant="outlined"
        sx={{ fontSize: 11 }}
      />
      <Typography variant="caption" color="text.secondary" className="mono">
        {formatDate(m.startDate, i18n.language)}
        {m.endDate ? ` → ${formatDate(m.endDate, i18n.language)}` : ` → …`}
      </Typography>

      <Box sx={{ ml: 'auto', display: 'flex', gap: 0.5, alignItems: 'center' }}>
        {isActive && !isEnding && (
          <Tooltip title={t('admin.delegates.end')}>
            <IconButton size="small" onClick={onStartEnding} color="warning">
              <EventBusy fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
        {isEnding && (
          <>
            <TextField
              type="date"
              value={endDateVal}
              onChange={(e) => onEndDateChange(e.target.value)}
              size="small"
              slotProps={{ inputLabel: { shrink: true } }}
              sx={{ width: 150 }}
            />
            <Button size="small" variant="contained" onClick={onConfirmEnd} disabled={isPending}>
              OK
            </Button>
            <Button size="small" onClick={onCancelEnd}>{t('common.cancel')}</Button>
          </>
        )}
        <Tooltip title={t('admin.delegates.delete')}>
          <IconButton size="small" onClick={onDelete} color="error">
            <Delete fontSize="small" />
          </IconButton>
        </Tooltip>
      </Box>
    </Box>
  );
}
