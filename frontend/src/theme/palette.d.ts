import type { Tokens } from './tokens';

declare module '@mui/material/styles' {
  interface Palette {
    tokens: Tokens;
  }
  interface PaletteOptions {
    tokens?: Tokens;
  }
}
