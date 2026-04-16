import { useState } from 'react';
import { Link } from 'react-router-dom';
import { IconButton, Drawer, List, ListItemButton, ListItemText, ListItemIcon, Box, Divider } from '@mui/material';
import { Menu as MenuIcon, Close, DarkMode, LightMode, Language } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/stores/useAuthStore';
import { useThemeStore } from '@/stores/useThemeStore';
import DevLoginButton from '@/components/common/DevLoginButton';
import * as s from './MobileMenu.styles';

const LINK_KEYS = ['home', 'browse', 'leaderboard', 'news', 'tools'] as const;
const LINK_PATHS: Record<(typeof LINK_KEYS)[number], string> = {
  home: '/',
  browse: '/browse',
  leaderboard: '/leaderboard',
  news: '/news',
  tools: '/tools',
};

export default function MobileMenu() {
  const [open, setOpen] = useState(false);
  const { t, i18n } = useTranslation();
  const { token, logout } = useAuthStore();
  const { theme, toggle: toggleTheme } = useThemeStore();
  const toggleLang = () => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr');

  return (
    <>
      <IconButton onClick={() => setOpen(true)} color="inherit">
        <MenuIcon />
      </IconButton>
      <Drawer anchor="right" open={open} onClose={() => setOpen(false)}>
        <Box sx={s.drawerBox}>
          <Box sx={s.closeRow}>
            <IconButton onClick={() => setOpen(false)}>
              <Close />
            </IconButton>
          </Box>
          <List>
            {LINK_KEYS.map((key) => (
              <ListItemButton key={key} component={Link} to={LINK_PATHS[key]} onClick={() => setOpen(false)}>
                <ListItemText primary={t(`nav.${key}`)} />
              </ListItemButton>
            ))}
            {token ? (
              <>
                <ListItemButton component={Link} to="/profile" onClick={() => setOpen(false)}>
                  <ListItemText primary={t('nav.profile')} />
                </ListItemButton>
                <ListItemButton component={Link} to="/upload" onClick={() => setOpen(false)}>
                  <ListItemText primary={t('nav.upload')} />
                </ListItemButton>
                <ListItemButton
                  onClick={() => {
                    logout();
                    setOpen(false);
                  }}
                >
                  <ListItemText primary={t('nav.logout')} />
                </ListItemButton>
              </>
            ) : import.meta.env.DEV ? (
              <Box sx={{ px: 2, py: 1 }}>
                <DevLoginButton />
              </Box>
            ) : (
              <ListItemButton component="a" href="/oauth2/authorization/discord">
                <ListItemText primary={t('nav.login')} />
              </ListItemButton>
            )}
            <Divider sx={{ my: 1 }} />
            <ListItemButton onClick={toggleTheme}>
              <ListItemIcon sx={{ minWidth: 36 }}>
                {theme === 'dark' ? <LightMode fontSize="small" /> : <DarkMode fontSize="small" />}
              </ListItemIcon>
              <ListItemText primary={t(theme === 'dark' ? 'nav.lightMode' : 'nav.darkMode')} />
            </ListItemButton>
            <ListItemButton onClick={toggleLang}>
              <ListItemIcon sx={{ minWidth: 36 }}>
                <Language fontSize="small" />
              </ListItemIcon>
              <ListItemText primary={t('nav.toggleLanguage')} />
            </ListItemButton>
          </List>
        </Box>
      </Drawer>
    </>
  );
}
