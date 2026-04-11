import { Container, Box } from '@mui/material';
import type { ReactNode } from 'react';

interface PageWrapperProps {
  children: ReactNode;
  maxWidth?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
}

export default function PageWrapper({ children, maxWidth = 'lg' }: PageWrapperProps) {
  return (
    <Container maxWidth={maxWidth}>
      <Box sx={{ py: 4, minHeight: 'calc(100vh - 200px)' }}>
        {children}
      </Box>
    </Container>
  );
}
