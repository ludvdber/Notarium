import { useState } from 'react';
import { IconButton, Badge as MuiBadge, Menu, MenuItem, Typography, Box } from '@mui/material';
import { Notifications } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { useNotificationStore } from '@/stores/useNotificationStore';

export default function NotificationBell() {
  const { t } = useTranslation();
  const { notifications, markAllRead, unreadCount } = useNotificationStore();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const count = unreadCount();

  const handleOpen = (e: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(e.currentTarget);
    markAllRead();
  };

  return (
    <>
      <IconButton
        size="small"
        color="inherit"
        onClick={handleOpen}
        aria-label={t('notifications.title')}
      >
        <MuiBadge badgeContent={count} color="error" max={9}>
          <Notifications fontSize="small" />
        </MuiBadge>
      </IconButton>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={() => setAnchorEl(null)}
        slotProps={{ paper: { sx: { minWidth: 280, maxHeight: 360 } } }}
      >
        {notifications.length === 0 ? (
          <MenuItem disabled>
            <Typography variant="body2" color="text.secondary">
              {t('notifications.empty')}
            </Typography>
          </MenuItem>
        ) : (
          notifications.slice(0, 10).map((n) => (
            <MenuItem key={n.id} sx={{ whiteSpace: 'normal', py: 1 }}>
              <Box>
                <Typography variant="body2">
                  {t(n.messageKey, n.params ?? {})}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {new Date(n.createdAt).toLocaleTimeString()}
                </Typography>
              </Box>
            </MenuItem>
          ))
        )}
      </Menu>
    </>
  );
}
