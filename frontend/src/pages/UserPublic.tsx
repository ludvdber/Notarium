import { Typography, Box, Avatar, Chip, Grid } from '@mui/material';
import { GitHub, LinkedIn } from '@mui/icons-material';
import { Coffee } from 'lucide-react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { getUserById, getDelegateHistory, getDocumentsByUser } from '@/api/endpoints';
import { formatDate } from '@/lib/utils';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';
import Badge from '@/components/ui/Badge';
import DocumentCard from '@/components/common/DocumentCard';
import OrbitalLoader from '@/components/ui/OrbitalLoader';

export default function UserPublic() {
  const { t, i18n } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const userId = Number(id);

  const { data: user, isLoading } = useQuery({
    queryKey: ['user', userId],
    queryFn: () => getUserById(userId),
    enabled: !Number.isNaN(userId),
  });

  const { data: delegateHistory } = useQuery({
    queryKey: ['delegate-history', userId],
    queryFn: () => getDelegateHistory(userId),
    enabled: !!user,
  });

  const { data: docs } = useQuery({
    queryKey: ['user-docs', userId],
    queryFn: () => getDocumentsByUser(userId),
    enabled: !!user,
  });

  if (isLoading) {
    return (
      <PageWrapper maxWidth="md">
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <OrbitalLoader size={48} />
        </Box>
      </PageWrapper>
    );
  }

  if (!user) {
    return (
      <PageWrapper maxWidth="md">
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>{t('userPublic.notFound')}</Typography>
        </Box>
      </PageWrapper>
    );
  }

  const activeDelegation = delegateHistory?.find((d) => d.active);

  return (
    <PageWrapper maxWidth="md">
      <GlassCard sx={{ p: { xs: 3, md: 4 }, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2.5, flexWrap: 'wrap' }}>
          <Avatar sx={{ width: 64, height: 64, fontSize: 24, bgcolor: 'primary.main' }}>
            {user.username.charAt(0).toUpperCase()}
          </Avatar>
          <Box sx={{ flex: 1, minWidth: 200 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
              <Typography variant="h5" sx={{ fontWeight: 800 }}>
                {user.username}
              </Typography>
              {user.supporter && (
                <Coffee size={18} color="#ffd93d" />
              )}
            </Box>
            <Typography variant="body2" color="text.secondary" className="mono">
              {user.xp} XP — {user.documentCount} {t('stats.docs').toLowerCase()}
            </Typography>
            {activeDelegation && (
              <Chip
                label={`${t('delegates.title')} — ${activeDelegation.sectionName}`}
                size="small"
                color="primary"
                variant="outlined"
                sx={{ mt: 0.5, fontSize: 11 }}
              />
            )}
          </Box>

          <Box sx={{ display: 'flex', gap: 1 }}>
            {user.github && (
              <Chip
                icon={<GitHub sx={{ fontSize: 16 }} />}
                label="GitHub"
                size="small"
                variant="outlined"
                component="a"
                href={`https://github.com/${user.github}`}
                target="_blank"
                rel="noopener noreferrer"
                clickable
              />
            )}
            {user.linkedin && (
              <Chip
                icon={<LinkedIn sx={{ fontSize: 16 }} />}
                label="LinkedIn"
                size="small"
                variant="outlined"
                component="a"
                href={user.linkedin}
                target="_blank"
                rel="noopener noreferrer"
                clickable
              />
            )}
          </Box>
        </Box>

        {user.bio && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2, lineHeight: 1.7 }}>
            {user.bio}
          </Typography>
        )}

        {user.badges.length > 0 && (
          <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mt: 2 }}>
            {user.badges.map((b) => (
              <Badge key={b} label={b} />
            ))}
          </Box>
        )}
      </GlassCard>

      {delegateHistory && delegateHistory.length > 0 && (
        <GlassCard sx={{ p: 2.5, mb: 3 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.5 }}>
            {t('delegates.title')}
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.75 }}>
            {delegateHistory.map((dh) => (
              <Box key={dh.id} sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Chip
                  label={dh.active ? t('admin.delegates.activeChip') : t('admin.delegates.endedChip')}
                  size="small"
                  color={dh.active ? 'success' : 'default'}
                  variant="outlined"
                  sx={{ fontSize: 10, minWidth: 60 }}
                />
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {dh.sectionName}
                </Typography>
                <Typography variant="caption" color="text.secondary" className="mono">
                  {formatDate(dh.startDate, i18n.language)}{dh.endDate ? ` → ${formatDate(dh.endDate, i18n.language)}` : ' → …'}
                </Typography>
              </Box>
            ))}
          </Box>
        </GlassCard>
      )}

      {docs && docs.content.length > 0 && (
        <>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
            {t('profile.documents')}
          </Typography>
          <Grid container spacing={2}>
            {docs.content.map((doc) => (
              <Grid key={doc.id} size={{ xs: 12, sm: 6 }}>
                <DocumentCard document={doc} />
              </Grid>
            ))}
          </Grid>
        </>
      )}
    </PageWrapper>
  );
}
