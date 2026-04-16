import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, IconButton, Box, Avatar, Menu, MenuItem, Tooltip } from '@mui/material';
import { DarkMode, LightMode, Language } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/stores/useAuthStore';
import { useThemeStore } from '@/stores/useThemeStore';
import { useMediaQuery } from '@/hooks/useMediaQuery';
import { DISCORD_OAUTH_URL, DISCORD_INVITE_URL } from '@/lib/constants';
import DevLoginButton from '@/components/common/DevLoginButton';
import NotificationBell from './NotificationBell';
import MobileMenu from './MobileMenu';
import { NAV_LINKS } from './Navbar.data';
import * as s from './Navbar.styles';

export default function Navbar() {
  const { t, i18n } = useTranslation();
  const { user, token, logout } = useAuthStore();
  const { theme, toggle } = useThemeStore();
  const isMobile = useMediaQuery('(max-width: 768px)');
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const toggleLang = () => {
    i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr');
  };

  return (
    <AppBar position="sticky" component="nav" aria-label="Main navigation" sx={s.appBar}>
      <Toolbar sx={s.toolbar}>
        <Typography variant="h6" component={Link} to="/" sx={s.logo}>
          Free<Box component="span" sx={s.logoGradient}>note</Box>
        </Typography>

        {isMobile ? (
          <MobileMenu />
        ) : (
          <Box sx={s.actionsRow}>
            {NAV_LINKS.map((link) => {
              const active = location.pathname === link.to;
              return (
                <Button
                  key={link.key}
                  component={Link}
                  to={link.to}
                  color="inherit"
                  size="small"
                  aria-current={active ? 'page' : undefined}
                  sx={s.navButton(active)}
                >
                  {t(`nav.${link.key}`)}
                </Button>
              );
            })}

            <Tooltip title={t('nav.toggleTheme')}>
              <IconButton onClick={toggle} size="small" color="inherit" aria-label={t('nav.toggleTheme')}>
                {theme === 'dark' ? <LightMode fontSize="small" /> : <DarkMode fontSize="small" />}
              </IconButton>
            </Tooltip>

            <Tooltip title={t('nav.toggleLanguage')}>
              <IconButton onClick={toggleLang} size="small" color="inherit" aria-label={t('nav.toggleLanguage')}>
                <Language fontSize="small" />
              </IconButton>
            </Tooltip>

            {token && <NotificationBell />}

            {token && (
              <Tooltip title="Discord ISFCE">
                <IconButton
                  component="a"
                  href={DISCORD_INVITE_URL}
                  target="_blank"
                  rel="noopener noreferrer"
                  size="small"
                  color="inherit"
                  aria-label="Discord ISFCE"
                  sx={{ fontSize: 16 }}
                >
                  💬
                </IconButton>
              </Tooltip>
            )}

            {token ? (
              <>
                <IconButton
                  onClick={(e) => setAnchorEl(e.currentTarget)}
                  size="small"
                  aria-label={t('nav.profile')}
                  aria-expanded={Boolean(anchorEl)}
                  aria-haspopup="menu"
                >
                  <Avatar sx={s.avatar}>{user?.username?.charAt(0).toUpperCase() ?? '?'}</Avatar>
                </IconButton>
                <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
                  <MenuItem
                    onClick={() => {
                      setAnchorEl(null);
                      navigate('/profile');
                    }}
                  >
                    {t('nav.profile')}
                  </MenuItem>
                  <MenuItem
                    onClick={() => {
                      setAnchorEl(null);
                      navigate('/upload');
                    }}
                  >
                    {t('nav.upload')}
                  </MenuItem>
                  <MenuItem
                    onClick={() => {
                      setAnchorEl(null);
                      logout();
                    }}
                  >
                    {t('nav.logout')}
                  </MenuItem>
                </Menu>
              </>
            ) : import.meta.env.DEV ? (
              <DevLoginButton />
            ) : (
              <Button variant="contained" size="small" href={DISCORD_OAUTH_URL}>
                {t('nav.login')}
              </Button>
            )}
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
}
