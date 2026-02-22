import type { Role, AuthUser } from "./types";

type JwtPayload = {
  sub?: string;
  roles?: Role[] | string[];
  exp?: number;
};

function base64UrlDecode(input: string): string {
  const base64 = input.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
  return decodeURIComponent(
    atob(padded)
      .split("")
      .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
      .join(""),
  );
}

export function decodeJwtPayload(token: string): JwtPayload {
  const parts = token.split(".");
  if (parts.length !== 3) throw new Error("Invalid JWT format");
  const payloadJson = base64UrlDecode(parts[1]);
  return JSON.parse(payloadJson) as JwtPayload;
}

export function toAuthUser(token: string): AuthUser {
  const payload = decodeJwtPayload(token);

  if (!payload.sub) throw new Error("JWT missing 'sub'");
  if (!payload.exp) throw new Error("JWT missing 'exp'");

  const rolesRaw = payload.roles ?? [];
  const roles = rolesRaw.map(String) as Role[];

  return {
    id: payload.sub,
    exp: payload.exp,
    roles,
  };
}

export function isExpired(expUnixSeconds: number, skewSeconds = 10): boolean {
  const now = Math.floor(Date.now() / 1000);
  return expUnixSeconds <= now + skewSeconds;
}