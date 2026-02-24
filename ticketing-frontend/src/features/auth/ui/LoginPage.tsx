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
    () => (loading ? "Procesando..." : mode === "login" ? "Iniciar sesión" : "Crear cuenta"),
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

      const from = loc.state?.from ?? "/tickets";
      nav(from, { replace: true });
    } catch (err) {
      if (err instanceof ApiError) setError(`${err.status} — ${err.message}`);
      else setError("No se pudo completar la autenticación");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div className="auth-brand">
          <h1>Ticketing Platform</h1>
          <p>Accede o crea tu cuenta para gestionar incidencias de forma eficiente.</p>
        </div>

        <div className="auth-tabs" role="tablist" aria-label="Autenticación">
          <button
            type="button"
            className={`auth-tab ${mode === "login" ? "active" : ""}`}
            onClick={() => {
              setMode("login");
              setError(null);
            }}
          >
            Iniciar sesión
          </button>
          <button
            type="button"
            className={`auth-tab ${mode === "register" ? "active" : ""}`}
            onClick={() => {
              setMode("register");
              setError(null);
            }}
          >
            Registrarse
          </button>
        </div>

        <form onSubmit={onSubmit} className="auth-form">
          <label>
            Email
            <input
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="username"
              type="email"
              required
            />
          </label>

          {mode === "register" && (
            <label>
              Nombre a mostrar
              <input
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                autoComplete="name"
                required
              />
            </label>
          )}

          <label>
            Contraseña
            <input
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
                Confirmar contraseña
                <input
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  type="password"
                  autoComplete="new-password"
                  required
                />
              </label>
              <p className="auth-password-hint">
                La contraseña debe tener mínimo 8 caracteres e incluir mayúsculas, minúsculas, número y símbolo.
              </p>
            </>
          )}

          <label className="auth-row">
            <input
              type="checkbox"
              checked={remember}
              onChange={(e) => setRemember(e.target.checked)}
            />
            Recordar sesión
          </label>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" disabled={loading} className="auth-submit">
            {submitText}
          </button>
        </form>
      </section>
    </main>
  );
}
