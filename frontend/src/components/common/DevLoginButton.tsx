import { useState } from 'react';
import { Button, Menu, MenuItem, ListItemText, Typography } from '@mui/material';
import { devLogin, getCurrentUser } from '@/api/endpoints';
import { useAuthStore } from '@/stores/useAuthStore';

const DEV_USERS = [
  { username: 'admin', label: 'Admin (ADMIN)' },
  { username: 'sophie_m', label: 'Sophie (VERIFIED)' },
  { username: 'thomas_d', label: 'Thomas (VERIFIED)' },
  { username: 'newcomer', label: 'Newcomer (USER)' },
] as const;

export default function DevLoginButton() {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [loading, setLoading] = useState(false);
  const { loginFromUser } = useAuthStore();

  const handleLogin = async (username: string) => {
    setAnchorEl(null);
    setLoading(true);
    try {
      const res = await devLogin(username);
      // Cookie is set by the backend — now fetch the full user
      const user = await getCurrentUser();
      loginFromUser(user);
      // Update admin/verified from the dev login response
      useAuthStore.setState({
        isAdmin: res.role === 'ADMIN',
        isVerified: res.verified === 'true',
      });
    } catch (e) {
      console.error('Dev login failed:', e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Button
        variant="contained"
        size="small"
        onClick={(e) => setAnchorEl(e.currentTarget)}
        disabled={loading}
      >
        {loading ? '...' : 'Dev Login'}
      </Button>
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
        <MenuItem disabled>
          <Typography variant="caption" color="text.secondary">Choisir un compte :</Typography>
        </MenuItem>
        {DEV_USERS.map((u) => (
          <MenuItem key={u.username} onClick={() => handleLogin(u.username)}>
            <ListItemText primary={u.label} />
          </MenuItem>
        ))}
      </Menu>
    </>
  );
}
