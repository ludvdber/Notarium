import { ThemeProvider, CssBaseline } from '@mui/material';
import { useThemeStore } from '@/stores/useThemeStore';
import { darkTheme, lightTheme } from '@/theme/muiTheme';
import { useAuthInit } from '@/hooks/useAuthInit';
import { useNotificationsStream } from '@/hooks/useNotificationsStream';
import App from '@/App';

export default function ThemedApp() {
  const theme = useThemeStore((s) => s.theme);
  useAuthInit();
  useNotificationsStream();

  return (
    <ThemeProvider theme={theme === 'dark' ? darkTheme : lightTheme}>
      <CssBaseline />
      <App />
    </ThemeProvider>
  );
}
