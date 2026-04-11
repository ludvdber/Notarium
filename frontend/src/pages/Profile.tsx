import { useState, useEffect } from 'react';
import { Typography, TextField, Button, Box, FormControlLabel, Switch, Alert } from '@mui/material';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { getCurrentUser, updateProfile, deleteAccount } from '@/api/endpoints';
import { useAuthStore } from '@/stores/useAuthStore';
import PageWrapper from '@/components/layout/PageWrapper';
import Badge from '@/components/ui/Badge';
import * as s from './Profile.styles';

export default function Profile() {
  const { t } = useTranslation();
  const { setUser, logout } = useAuthStore();
  const { data: user } = useQuery({ queryKey: ['me'], queryFn: getCurrentUser });

  const [bio, setBio] = useState('');
  const [website, setWebsite] = useState('');
  const [github, setGithub] = useState('');
  const [linkedin, setLinkedin] = useState('');
  const [discord, setDiscord] = useState('');
  const [profilePublic, setProfilePublic] = useState(false);
  const [showInCarousel, setShowInCarousel] = useState(false);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (user) {
      setBio(user.bio ?? '');
      setWebsite(user.website ?? '');
      setGithub(user.github ?? '');
      setLinkedin(user.linkedin ?? '');
      setDiscord(user.discord ?? '');
      setProfilePublic(user.profilePublic);
      setUser(user);
    }
  }, [user, setUser]);

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
          onClick={() => {
            if (confirm(t('profile.deleteConfirm'))) deleteMutation.mutate();
          }}
        >
          {t('profile.deleteAccount')}
        </Button>
      </Box>
    </PageWrapper>
  );
}
