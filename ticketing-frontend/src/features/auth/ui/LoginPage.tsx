import React, { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ApiError } from "../../../shared/api/errors";
import { useAuth } from "../hooks/useAuth";
import "./LoginPage.css";

type AuthMode = "login" | "register";

export function LoginPage() {
  const { login, register } = useAuth();
  const nav = useNavigate();
  const loc = useLocation() as { state?: { from?: string } };

  const [mode, setMode] = useState<AuthMode>("login");
  const [email, setEmail] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [remember, setRemember] = useState(true);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submitText = useMemo(
    () => (loading ? "Processing..." : mode === "login" ? "Sign in" : "Create account"),
    [loading, mode],
  );

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (mode === "login") {
        await login(email, password, remember);
      } else {
        await register({ email, displayName, password, confirmPassword, remember });
      }

      const from = loc.state?.from ?? "/";
      nav(from, { replace: true });
    } catch (err) {
      if (err instanceof ApiError) setError(`${err.status} — ${err.message}`);
      else setError("Could not complete authentication");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div className="auth-brand">
          <h1>Ticketing Platform</h1>
          <p>Sign in or create your account to manage support tickets efficiently.</p>
        </div>

        <div className="auth-tabs" role="tablist" aria-label="Authentication">
          <button
            type="button"
            className={`auth-tab ${mode === "login" ? "active" : ""}`}
            onClick={() => {
              setMode("login");
              setError(null);
            }}
          >
            Sign in
          </button>
          <button
            type="button"
            className={`auth-tab ${mode === "register" ? "active" : ""}`}
            onClick={() => {
              setMode("register");
              setError(null);
            }}
          >
            Sign up
          </button>
        </div>

        <form onSubmit={onSubmit} className="auth-form">
          <label>
            Email
            <input
              data-testid="auth-email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="username"
              type="email"
              required
            />
          </label>

          {mode === "register" && (
            <label>
              Display name
              <input
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                autoComplete="name"
                required
              />
            </label>
          )}

          <label>
            Password
            <input
              data-testid="auth-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              type="password"
              autoComplete={mode === "login" ? "current-password" : "new-password"}
              required
            />
          </label>

          {mode === "register" && (
            <>
              <label>
                Confirm password
                <input
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  type="password"
                  autoComplete="new-password"
                  required
                />
              </label>
              <p className="auth-password-hint">
                Password must be at least 8 characters and include uppercase, lowercase, a number,
                and a symbol.
              </p>
            </>
          )}

          <label className="auth-row">
            <input
              type="checkbox"
              checked={remember}
              onChange={(e) => setRemember(e.target.checked)}
            />
            Remember session
          </label>

          {error && (
            <p className="auth-error" data-testid="auth-error">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="auth-submit"
            data-testid="auth-submit"
          >
            {submitText}
          </button>
        </form>
      </section>
    </main>
  );
}
