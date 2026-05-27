import { Snackbar, Alert } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useAuthPromptStore } from '@/stores/useAuthPromptStore';

/** Mounted once near the app root. Listens to the global auth-prompt store and surfaces
 *  a toast whenever someone tries to activate a protected link while logged out. */
export default function AuthPromptSnackbar() {
  const { t } = useTranslation();
  const { open, messageKey, close } = useAuthPromptStore();

  return (
    <Snackbar
      open={open}
      autoHideDuration={4000}
      onClose={close}
      anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
    >
      <Alert onClose={close} severity="info" variant="filled">
        {t(messageKey)}
      </Alert>
    </Snackbar>
  );
}
