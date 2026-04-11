export interface IPv4Result {
  network: string;
  broadcast: string;
  firstHost: string;
  lastHost: string;
  hostCount: number;
  ipClass: string;
  wildcard: string;
  cidr: string;
  maskDecimal: string;
  ipBinary: string;
  maskBinary: string;
}

export function parseIp(ip: string): number[] | null {
  const parts = ip.split('.');
  if (parts.length !== 4) return null;
  const octets = parts.map(Number);
  if (octets.some((o) => isNaN(o) || o < 0 || o > 255)) return null;
  return octets;
}

export function isValidIp(ip: string): boolean {
  return parseIp(ip) !== null;
}

export function cidrToMaskOctets(cidr: number): number[] {
  const mask = cidr === 0 ? 0 : (~0 << (32 - cidr)) >>> 0;
  return [
    (mask >>> 24) & 0xff,
    (mask >>> 16) & 0xff,
    (mask >>> 8) & 0xff,
    mask & 0xff,
  ];
}

export function maskOctetsToCidr(octets: number[]): number {
  const num = ((octets[0] << 24) | (octets[1] << 16) | (octets[2] << 8) | octets[3]) >>> 0;
  let count = 0;
  let n = num;
  while (n & 0x80000000) {
    count++;
    n = (n << 1) >>> 0;
  }
  return count;
}

export function parseMaskDecimal(mask: string): number | null {
  const octets = mask.split('.').map(Number);
  if (octets.length !== 4) return null;
  if (octets.some((o) => isNaN(o) || o < 0 || o > 255)) return null;
  // Validate it's a contiguous mask
  const num = ((octets[0] << 24) | (octets[1] << 16) | (octets[2] << 8) | octets[3]) >>> 0;
  const inverted = (~num) >>> 0;
  if ((inverted & (inverted + 1)) !== 0) return null;
  return maskOctetsToCidr(octets);
}

function octetsToNum(octets: number[]): number {
  return ((octets[0] << 24) | (octets[1] << 16) | (octets[2] << 8) | octets[3]) >>> 0;
}

function numToOctets(num: number): number[] {
  return [
    (num >>> 24) & 0xff,
    (num >>> 16) & 0xff,
    (num >>> 8) & 0xff,
    num & 0xff,
  ];
}

function octetsToStr(octets: number[]): string {
  return octets.join('.');
}

function octetToBinary(o: number): string {
  return o.toString(2).padStart(8, '0');
}

function getIpClass(firstOctet: number): string {
  if (firstOctet < 128) return 'A';
  if (firstOctet < 192) return 'B';
  if (firstOctet < 224) return 'C';
  if (firstOctet < 240) return 'D';
  return 'E';
}

export function calculateIPv4(ip: string, cidr: number): IPv4Result | null {
  const ipOctets = parseIp(ip);
  if (!ipOctets) return null;
  if (cidr < 0 || cidr > 32) return null;

  const ipNum = octetsToNum(ipOctets);
  const maskOctets = cidrToMaskOctets(cidr);
  const maskNum = octetsToNum(maskOctets);
  const wildcardNum = (~maskNum) >>> 0;
  const wildcardOctets = numToOctets(wildcardNum);

  const networkNum = (ipNum & maskNum) >>> 0;
  const broadcastNum = (networkNum | wildcardNum) >>> 0;
  const networkOctets = numToOctets(networkNum);
  const broadcastOctets = numToOctets(broadcastNum);

  let firstHostNum: number;
  let lastHostNum: number;
  let hostCount: number;

  if (cidr === 32) {
    firstHostNum = networkNum;
    lastHostNum = networkNum;
    hostCount = 1;
  } else if (cidr === 31) {
    firstHostNum = networkNum;
    lastHostNum = broadcastNum;
    hostCount = 2;
  } else {
    firstHostNum = networkNum + 1;
    lastHostNum = broadcastNum - 1;
    hostCount = Math.pow(2, 32 - cidr) - 2;
  }

  return {
    network: octetsToStr(networkOctets),
    broadcast: octetsToStr(broadcastOctets),
    firstHost: octetsToStr(numToOctets(firstHostNum)),
    lastHost: octetsToStr(numToOctets(lastHostNum)),
    hostCount: Math.max(hostCount, 0),
    ipClass: getIpClass(ipOctets[0]),
    wildcard: octetsToStr(wildcardOctets),
    cidr: `/${cidr}`,
    maskDecimal: octetsToStr(maskOctets),
    ipBinary: ipOctets.map(octetToBinary).join(''),
    maskBinary: maskOctets.map(octetToBinary).join(''),
  };
}
