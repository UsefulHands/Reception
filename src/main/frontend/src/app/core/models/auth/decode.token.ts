export interface DecodedToken {
  sub: string;
  roles?: string[];
  role?: string;
  exp: number;
}
