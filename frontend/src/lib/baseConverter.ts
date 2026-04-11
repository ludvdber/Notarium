export type Base = 2 | 8 | 10 | 16;

export const BASE_NAMES: Record<Base, string> = {
  2: 'binary',
  8: 'octal',
  10: 'decimal',
  16: 'hex',
};

const VALID_CHARS: Record<Base, RegExp> = {
  2: /^[01]*$/,
  8: /^[0-7]*$/,
  10: /^[0-9]*$/,
  16: /^[0-9a-fA-F]*$/,
};

export function isValidForBase(value: string, base: Base): boolean {
  if (value === '') return true;
  return VALID_CHARS[base].test(value);
}

export function convertBase(value: string, from: Base): Record<Base, string> | null {
  if (!value || !isValidForBase(value, from)) return null;

  const decimal = parseInt(value, from);
  if (isNaN(decimal) || decimal < 0) return null;

  return {
    2: decimal.toString(2),
    8: decimal.toString(8),
    10: decimal.toString(10),
    16: decimal.toString(16).toUpperCase(),
  };
}

export interface AsciiEntry {
  dec: number;
  hex: string;
  oct: string;
  bin: string;
  char: string;
}

const CONTROL_NAMES: Record<number, string> = {
  0: 'NUL', 1: 'SOH', 2: 'STX', 3: 'ETX', 4: 'EOT', 5: 'ENQ', 6: 'ACK', 7: 'BEL',
  8: 'BS', 9: 'TAB', 10: 'LF', 11: 'VT', 12: 'FF', 13: 'CR', 14: 'SO', 15: 'SI',
  16: 'DLE', 17: 'DC1', 18: 'DC2', 19: 'DC3', 20: 'DC4', 21: 'NAK', 22: 'SYN', 23: 'ETB',
  24: 'CAN', 25: 'EM', 26: 'SUB', 27: 'ESC', 28: 'FS', 29: 'GS', 30: 'RS', 31: 'US',
  32: 'SP', 127: 'DEL',
};

export function generateAsciiTable(): AsciiEntry[] {
  const entries: AsciiEntry[] = [];
  for (let i = 0; i <= 127; i++) {
    entries.push({
      dec: i,
      hex: i.toString(16).toUpperCase().padStart(2, '0'),
      oct: i.toString(8).padStart(3, '0'),
      bin: i.toString(2).padStart(8, '0'),
      char: CONTROL_NAMES[i] ?? String.fromCharCode(i),
    });
  }
  return entries;
}
