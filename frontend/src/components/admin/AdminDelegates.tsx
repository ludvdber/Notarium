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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControlLabel,
  Switch,
  Autocomplete,
} from '@mui/material';
import { Add, EventBusy, Delete, Edit } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { getAllMandates, assignDelegate, endDelegate, deleteMandate, updateMandate, getSections, adminSearchUsers } from '@/api/endpoints';
import { useDebounce } from '@/hooks/useDebounce';
import type { User } from '@/types';
import { formatDate, extractApiError } from '@/lib/utils';
import { STALE_15M } from '@/lib/constants';
import GlassCard from '@/components/ui/GlassCard';
import type { DelegateMember } from '@/types';

export default function AdminDelegates() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const [showForm, setShowForm] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [userQuery, setUserQuery] = useState('');
  const debouncedUserQuery = useDebounce(userQuery, 300);
  const [sectionId, setSectionId] = useState('');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [endingId, setEndingId] = useState<number | null>(null);
  const [endDateVal, setEndDateVal] = useState(new Date().toISOString().split('T')[0]);
  const [editing, setEditing] = useState<DelegateMember | null>(null);
  const [editStart, setEditStart] = useState('');
  const [editEnd, setEditEnd] = useState('');
  const [editReopen, setEditReopen] = useState(false);
  const [editError, setEditError] = useState('');

  const { data: mandates, isLoading } = useQuery({ queryKey: ['admin-mandates'], queryFn: getAllMandates });
  const { data: sections } = useQuery({ queryKey: ['sections'], queryFn: getSections, staleTime: STALE_15M });
  const { data: userOptions } = useQuery({
    queryKey: ['admin-users-search', debouncedUserQuery],
    queryFn: () => adminSearchUsers(debouncedUserQuery, 20),
    enabled: showForm,
  });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['admin-mandates'] });
    queryClient.invalidateQueries({ queryKey: ['delegates'] });
  };

  const assignMut = useMutation({
    mutationFn: () => {
      if (!selectedUser) throw new Error('User required');
      return assignDelegate({ userId: selectedUser.id, sectionId: Number(sectionId), startDate });
    },
    onSuccess: () => {
      invalidate();
      setShowForm(false);
      setSelectedUser(null);
      setUserQuery('');
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

  const editMut = useMutation({
    mutationFn: () => {
      if (!editing) throw new Error('no target');
      return updateMandate(editing.id, {
        startDate: editStart || undefined,
        endDate: editReopen ? null : (editEnd || undefined),
        clearEndDate: editReopen,
      });
    },
    onSuccess: () => {
      invalidate();
      setEditing(null);
      setEditError('');
    },
    onError: (e: unknown) => setEditError(extractApiError(e, t('common.error'))),
  });

  const openEdit = (m: DelegateMember) => {
    setEditing(m);
    setEditStart(m.startDate);
    setEditEnd(m.endDate ?? '');
    setEditReopen(false);
    setEditError('');
  };

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
            <Autocomplete<User, false, false, false>
              size="small"
              sx={{ minWidth: 240, flex: 1 }}
              options={userOptions ?? []}
              value={selectedUser}
              onChange={(_, v) => setSelectedUser(v)}
              onInputChange={(_, v) => setUserQuery(v)}
              getOptionLabel={(u) => u.displayName}
              isOptionEqualToValue={(a, b) => a.id === b.id}
              filterOptions={(opts) => opts}
              noOptionsText={t('admin.delegates.userSearchEmpty')}
              renderInput={(params) => (
                <TextField {...params} label={t('admin.delegates.user')} placeholder={t('admin.delegates.userSearchPlaceholder')} />
              )}
              renderOption={(props, u) => {
                const role = (u.role as string | null) ?? 'USER';
                const tags: string[] = [];
                if (u.verified) tags.push(t('admin.delegates.tagVerified'));
                if (u.supporter) tags.push(t('admin.delegates.tagSupporter'));
                return (
                  <li {...props} key={u.id}>
                    <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {u.displayName}
                        {u.displayName !== u.username && (
                          <Typography component="span" variant="caption" color="text.secondary" sx={{ ml: 0.5 }}>
                            @{u.username}
                          </Typography>
                        )}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {role}{tags.length > 0 ? ` · ${tags.join(' · ')}` : ''}
                      </Typography>
                    </Box>
                  </li>
                );
              }}
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
              disabled={!selectedUser || !sectionId || assignMut.isPending}
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
              onEdit={() => openEdit(m)}
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
                onEdit={() => openEdit(m)}
                isPending={false}
                t={t}
              />
            ))}
          </Box>
        </GlassCard>
      )}

      <Dialog open={Boolean(editing)} onClose={() => setEditing(null)} maxWidth="xs" fullWidth>
        <DialogTitle>{t('admin.delegates.editTitle')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          {editing && (
            <Typography variant="body2" color="text.secondary">
              {editing.username}
            </Typography>
          )}
          {editError && <Alert severity="error" onClose={() => setEditError('')}>{editError}</Alert>}
          <TextField
            label={t('admin.delegates.startDate')}
            type="date"
            value={editStart}
            onChange={(e) => setEditStart(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label={t('admin.delegates.endDateOptional')}
            type="date"
            value={editReopen ? '' : editEnd}
            onChange={(e) => setEditEnd(e.target.value)}
            disabled={editReopen}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          {editing?.endDate && (
            <FormControlLabel
              control={<Switch checked={editReopen} onChange={(e) => setEditReopen(e.target.checked)} />}
              label={t('admin.delegates.reopen')}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditing(null)}>{t('common.cancel')}</Button>
          <Button variant="contained" onClick={() => editMut.mutate()} disabled={editMut.isPending}>
            {t('common.save')}
          </Button>
        </DialogActions>
      </Dialog>
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
  onEdit: () => void;
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
  onEdit,
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
        {!isEnding && (
          <Tooltip title={t('admin.delegates.edit')}>
            <IconButton size="small" onClick={onEdit}>
              <Edit fontSize="small" />
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
