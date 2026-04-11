import { CardContent, Typography, Box, Avatar } from '@mui/material';
import { GitHub, Link as LinkIcon } from '@mui/icons-material';
import type { ProfileCardResponse } from '@/types';
import GlassCard from '@/components/ui/GlassCard';
import Badge from '@/components/ui/Badge';
import * as s from './ProfileCard.styles';

interface Props {
  profile: ProfileCardResponse;
}

export default function ProfileCard({ profile }: Props) {
  return (
    <GlassCard>
      <CardContent sx={s.content}>
        <Avatar sx={s.avatar}>{profile.username.charAt(0).toUpperCase()}</Avatar>
        <Typography variant="subtitle1" sx={s.name}>
          {profile.username}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {profile.role}
        </Typography>

        <Box sx={s.socialRow}>
          {profile.github && (
            <a
              href={`https://github.com/${profile.github}`}
              target="_blank"
              rel="noopener noreferrer"
              aria-label={`GitHub — ${profile.username}`}
            >
              <GitHub sx={s.socialIcon} />
            </a>
          )}
          {profile.linkedin && (
            <a
              href={profile.linkedin}
              target="_blank"
              rel="noopener noreferrer"
              aria-label={`LinkedIn — ${profile.username}`}
            >
              <LinkIcon sx={s.socialIcon} />
            </a>
          )}
        </Box>

        {profile.badges.length > 0 && (
          <Box sx={s.badgesRow}>
            {profile.badges.map((b) => (
              <Badge key={b} label={b} />
            ))}
          </Box>
        )}
      </CardContent>
    </GlassCard>
  );
}
