import { Component, type ErrorInfo, type ReactNode } from 'react';
import { Box, Typography, Button } from '@mui/material';
import { Refresh } from '@mui/icons-material';
import i18n from '@/i18n/i18n';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

export default class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('[ErrorBoundary]', error, info.componentStack);
  }

  render() {
    if (!this.state.hasError) return this.props.children;

    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', gap: 2, px: 3, textAlign: 'center' }}>
        <Typography variant="h4" sx={{ fontWeight: 800 }}>
          {i18n.t('common.errorTitle')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {i18n.t('common.error')}
        </Typography>
        <Button
          variant="contained"
          startIcon={<Refresh />}
          onClick={() => {
            this.setState({ hasError: false });
            window.location.href = '/';
          }}
        >
          {i18n.t('common.errorRetry')}
        </Button>
      </Box>
    );
  }
}
