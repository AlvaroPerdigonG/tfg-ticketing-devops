import type { Role } from "src/features/auth/model/types";

type JwtPayload = {
  sub?: string;
  email?: string;
  displayName?: string;
  roles?: Role[];
  exp?: number;
};

function toBase64Url(value: string): string {
  return btoa(value).replaceAll("+", "-").replaceAll("/", "_").replaceAll("=", "");
}

export function buildJwt(payload: JwtPayload): string {
  const header = toBase64Url(JSON.stringify({ alg: "HS256", typ: "JWT" }));
  const body = toBase64Url(JSON.stringify(payload));
  return `${header}.${body}.test-signature`;
}
