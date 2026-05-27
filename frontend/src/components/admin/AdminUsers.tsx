import { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  Chip,
  Tooltip,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import { Verified, GppBad, Shield, DeleteForever } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  adminSearchUsers,
  adminVerifyUser,
  adminUnverifyUser,
  adminUpdateUserRole,
  adminDeleteUser,
} from '@/api/endpoints';
import GlassCard from '@/components/ui/GlassCard';
import type { User } from '@/types';

type Role = 'USER' | 'VERIFIED' | 'ADMIN';

export default function AdminUsers() {
  const { t } = useTranslation();
  const qc = useQueryClient();

  const [query, setQuery] = useState('');
  const [error, setError] = useState('');
  const [deleteCandidate, setDeleteCandidate] = useState<User | null>(null);

  const { data: users, isLoading } = useQuery({
    queryKey: ['admin-users', query],
    queryFn: () => adminSearchUsers(query, 50),
  });

  const invalidate = () => qc.invalidateQueries({ queryKey: ['admin-users'] });

  const verifyMut = useMutation({
    mutationFn: adminVerifyUser,
    onSuccess: invalidate,
    onError: (e) => setError(extractError(e)),
  });

  const unverifyMut = useMutation({
    mutationFn: adminUnverifyUser,
    onSuccess: invalidate,
    onError: (e) => setError(extractError(e)),
  });

  const roleMut = useMutation({
    mutationFn: ({ id, role }: { id: number; role: Role }) => adminUpdateUserRole(id, role),
    onSuccess: invalidate,
    onError: (e) => setError(extractError(e)),
  });

  const deleteMut = useMutation({
    mutationFn: adminDeleteUser,
    onSuccess: () => {
      invalidate();
      setDeleteCandidate(null);
    },
    onError: (e) => setError(extractError(e)),
  });

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h6" sx={{ fontWeight: 700 }}>
        {t('admin.users.title')}
      </Typography>

      {error && <Alert severity="error" onClose={() => setError('')}>{error}</Alert>}

      <TextField
        size="small"
        placeholder={t('admin.users.searchPlaceholder')}
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      {isLoading && <Typography color="text.secondary">{t('common.loading')}</Typography>}
      {!isLoading && !users?.length && (
        <GlassCard sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="text.secondary">{t('admin.users.empty')}</Typography>
        </GlassCard>
      )}

      {users?.map((u: User) => (
        <GlassCard key={u.id} sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
          <Box sx={{ flex: 1, minWidth: 180 }}>
            <Typography variant="body2" sx={{ fontWeight: 700 }}>{u.displayName}</Typography>
            {u.displayName !== u.username && (
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                @{u.username}
              </Typography>
            )}
            <Typography variant="caption" color="text.secondary">
              {t('admin.users.xp', { xp: u.xp })}
            </Typography>
          </Box>

          {u.verified ? (
            <Chip size="small" color="success" icon={<Verified />} label={t('admin.users.verified')} />
          ) : (
            <Chip size="small" color="default" icon={<GppBad />} label={t('admin.users.unverified')} />
          )}

          <FormControl size="small" sx={{ minWidth: 130 }}>
            <InputLabel>{t('admin.users.role')}</InputLabel>
            <Select
              value={(u.role as Role | null) ?? 'USER'}
              label={t('admin.users.role')}
              onChange={(e) => roleMut.mutate({ id: u.id, role: e.target.value as Role })}
            >
              <MenuItem value="USER">USER</MenuItem>
              <MenuItem value="VERIFIED">VERIFIED</MenuItem>
              <MenuItem value="ADMIN">ADMIN</MenuItem>
            </Select>
          </FormControl>

          {u.verified ? (
            <Tooltip title={t('admin.users.unverify')}>
              <IconButton size="small" color="warning" onClick={() => unverifyMut.mutate(u.id)}>
                <Shield fontSize="small" />
              </IconButton>
            </Tooltip>
          ) : (
            <Button
              size="small"
              variant="contained"
              color="success"
              startIcon={<Verified />}
              onClick={() => verifyMut.mutate(u.id)}
            >
              {t('admin.users.verify')}
            </Button>
          )}
          <Tooltip title={t('admin.users.delete')}>
            <IconButton size="small" color="error" onClick={() => setDeleteCandidate(u)}>
              <DeleteForever fontSize="small" />
            </IconButton>
          </Tooltip>
        </GlassCard>
      ))}

      <Dialog open={Boolean(deleteCandidate)} onClose={() => setDeleteCandidate(null)}>
        <DialogTitle color="error">{t('admin.users.deleteTitle')}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {t('admin.users.deleteConfirm', { username: deleteCandidate?.username })}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteCandidate(null)}>{t('common.cancel')}</Button>
          <Button
            color="error"
            variant="contained"
            disabled={deleteMut.isPending}
            onClick={() => deleteCandidate && deleteMut.mutate(deleteCandidate.id)}
          >
            {t('common.confirm')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

function extractError(e: unknown): string {
  if (typeof e === 'object' && e !== null) {
    const err = e as { response?: { data?: { message?: string } } };
    return err.response?.data?.message ?? 'Error';
  }
  return 'Error';
}
