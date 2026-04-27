export const AUTH_TOKEN_STORAGE_KEY = "ticketing_access_token";

type PersistAccessTokenOptions = {
  remember: boolean;
};

export function getStoredAccessToken(): string | null {
  return (
    localStorage.getItem(AUTH_TOKEN_STORAGE_KEY) ??
    sessionStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
  );
}

export function persistAccessToken(token: string, options: PersistAccessTokenOptions): void {
  if (options.remember) {
    localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token);
    sessionStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
    return;
  }

  sessionStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token);
  localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
}

export function clearStoredAccessToken(): void {
  localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
  sessionStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
}
