import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, IconButton, Box, Avatar, Menu, MenuItem } from '@mui/material';
import { DarkMode, LightMode, Language } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/stores/useAuthStore';
import { useThemeStore } from '@/stores/useThemeStore';
import { useMediaQuery } from '@/hooks/useMediaQuery';
import MobileMenu from './MobileMenu';
import { NAV_LINKS } from './Navbar.data';
import * as s from './Navbar.styles';

export default function Navbar() {
  const { t, i18n } = useTranslation();
  const { user, token, logout } = useAuthStore();
  const { theme, toggle } = useThemeStore();
  const isMobile = useMediaQuery('(max-width: 768px)');
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const toggleLang = () => {
    i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr');
  };

  return (
    <AppBar position="sticky" sx={s.appBar}>
      <Toolbar sx={s.toolbar}>
        <Typography variant="h6" component={Link} to="/" sx={s.logo}>
          Notarium
        </Typography>

        {isMobile ? (
          <MobileMenu />
        ) : (
          <Box sx={s.actionsRow}>
            {NAV_LINKS.map((link) => (
              <Button key={link.key} component={Link} to={link.to} color="inherit" size="small">
                {t(`nav.${link.key}`)}
              </Button>
            ))}

            <IconButton onClick={toggle} size="small" color="inherit">
              {theme === 'dark' ? <LightMode fontSize="small" /> : <DarkMode fontSize="small" />}
            </IconButton>

            <IconButton onClick={toggleLang} size="small" color="inherit">
              <Language fontSize="small" />
            </IconButton>

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
            ) : (
              <Button variant="contained" size="small" href="/oauth2/authorization/discord">
                {t('nav.login')}
              </Button>
            )}
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
}
