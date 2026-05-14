import { beforeEach, describe, expect, it } from "vitest";
import {
  AUTH_TOKEN_STORAGE_KEY,
  clearStoredAccessToken,
  getStoredAccessToken,
  persistAccessToken,
} from "./tokenStorage";

describe("tokenStorage", () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it("persists in localStorage when remember=true", () => {
    persistAccessToken("token-local", { remember: true });

    expect(localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)).toBe("token-local");
    expect(sessionStorage.getItem(AUTH_TOKEN_STORAGE_KEY)).toBeNull();
    expect(getStoredAccessToken()).toBe("token-local");
  });

  it("persists in sessionStorage when remember=false", () => {
    persistAccessToken("token-session", { remember: false });

    expect(sessionStorage.getItem(AUTH_TOKEN_STORAGE_KEY)).toBe("token-session");
    expect(localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)).toBeNull();
    expect(getStoredAccessToken()).toBe("token-session");
  });

  it("clears both storages on logout", () => {
    localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, "token-local");
    sessionStorage.setItem(AUTH_TOKEN_STORAGE_KEY, "token-session");

    clearStoredAccessToken();

    expect(localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)).toBeNull();
    expect(sessionStorage.getItem(AUTH_TOKEN_STORAGE_KEY)).toBeNull();
    expect(getStoredAccessToken()).toBeNull();
  });
});
