import { useState, useEffect } from 'react';
import { Typography, TextField, Button, Box, FormControlLabel, Switch, Alert, Chip, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions } from '@mui/material';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { getCurrentUser, updateProfile, deleteAccount, getDelegateHistory } from '@/api/endpoints';
import { formatDate } from '@/lib/utils';
import GlassCard from '@/components/ui/GlassCard';
import { useAuthStore } from '@/stores/useAuthStore';
import PageWrapper from '@/components/layout/PageWrapper';
import Badge from '@/components/ui/Badge';
import * as s from './Profile.styles';

export default function Profile() {
  const { t, i18n } = useTranslation();
  const { setUser, logout } = useAuthStore();
  const { data: user } = useQuery({ queryKey: ['me'], queryFn: getCurrentUser });
  const { data: delegateHistory } = useQuery({
    queryKey: ['delegate-history', user?.id],
    queryFn: () => getDelegateHistory(user!.id),
    enabled: !!user?.id,
  });

  const [bio, setBio] = useState('');
  const [website, setWebsite] = useState('');
  const [github, setGithub] = useState('');
  const [linkedin, setLinkedin] = useState('');
  const [discord, setDiscord] = useState('');
  const [profilePublic, setProfilePublic] = useState(false);
  const [showInCarousel, setShowInCarousel] = useState(false);
  const [saved, setSaved] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  useEffect(() => {
    if (!user) return;
    setBio(user.bio ?? '');
    setWebsite(user.website ?? '');
    setGithub(user.github ?? '');
    setLinkedin(user.linkedin ?? '');
    setDiscord(user.discord ?? '');
    setProfilePublic(user.profilePublic);
  }, [user]);

  const saveMutation = useMutation({
    mutationFn: () =>
      updateProfile({
        bio,
        website,
        github,
        linkedin,
        discord,
        profilePublic,
        showInCarousel,
        themePref: 'dark',
      }),
    onSuccess: (u) => {
      setUser(u);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteAccount,
    onSuccess: () => logout(),
  });

  if (!user) return null;

  return (
    <PageWrapper maxWidth="sm">
      <Helmet><title>{t('profile.title')} — Freenote</title></Helmet>
      <Typography variant="h4" sx={s.title}>
        {t('profile.title')}
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={s.subtitle}>
        {user.username} — {user.xp} XP
      </Typography>

      {user.badges.length > 0 && (
        <Box sx={s.badgesRow}>
          {user.badges.map((b) => (
            <Badge key={b} label={b} />
          ))}
        </Box>
      )}

      {delegateHistory && delegateHistory.length > 0 && (
        <GlassCard sx={{ p: 2, mb: 3 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.5 }}>
            {t('delegates.title')}
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {delegateHistory.map((dh) => (
              <Box
                key={dh.id}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1.5,
                  px: 1.5,
                  py: 1,
                  borderRadius: 1.5,
                  bgcolor: 'rgba(255,255,255,0.02)',
                }}
              >
                <Chip
                  label={dh.active ? t('admin.delegates.activeChip') : t('admin.delegates.endedChip')}
                  size="small"
                  color={dh.active ? 'success' : 'default'}
                  variant="outlined"
                  sx={{ fontSize: 11 }}
                />
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {dh.sectionName}
                </Typography>
                <Typography variant="caption" color="text.secondary" className="mono">
                  {formatDate(dh.startDate, i18n.language)}
                  {dh.endDate ? ` → ${formatDate(dh.endDate, i18n.language)}` : ` → …`}
                </Typography>
              </Box>
            ))}
          </Box>
        </GlassCard>
      )}

      {saved && (
        <Alert severity="success" sx={s.successAlert}>
          {t('common.save')} ✓
        </Alert>
      )}

      <Box sx={s.form}>
        <TextField
          label={t('profile.bio')}
          multiline
          rows={3}
          value={bio}
          onChange={(e) => setBio(e.target.value)}
          slotProps={{ htmlInput: { maxLength: 500 } }}
        />
        <TextField label={t('profile.website')} value={website} onChange={(e) => setWebsite(e.target.value)} />
        <TextField label="GitHub" value={github} onChange={(e) => setGithub(e.target.value)} />
        <TextField label="LinkedIn" value={linkedin} onChange={(e) => setLinkedin(e.target.value)} />
        <TextField label="Discord" value={discord} onChange={(e) => setDiscord(e.target.value)} />
        <FormControlLabel
          control={<Switch checked={profilePublic} onChange={(e) => setProfilePublic(e.target.checked)} />}
          label={t('profile.public')}
        />
        <FormControlLabel
          control={<Switch checked={showInCarousel} onChange={(e) => setShowInCarousel(e.target.checked)} />}
          label={t('profile.carousel')}
        />

        <Button variant="contained" onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>
          {t('profile.save')}
        </Button>

        <Button
          variant="outlined"
          color="error"
          onClick={() => setDeleteDialogOpen(true)}
        >
          {t('profile.deleteAccount')}
        </Button>
      </Box>

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle color="error">{t('profile.deleteAccount')}</DialogTitle>
        <DialogContent>
          <DialogContentText>{t('profile.deleteConfirm')}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>{t('common.cancel')}</Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => {
              setDeleteDialogOpen(false);
              deleteMutation.mutate();
            }}
          >
            {t('common.confirm')}
          </Button>
        </DialogActions>
      </Dialog>
    </PageWrapper>
  );
}
