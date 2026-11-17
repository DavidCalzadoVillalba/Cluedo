import { useState } from "react";
import { Link } from "react-router-dom";
import {
    FaEye,
    FaEyeSlash,
    FaLock,
    FaSignInAlt,
    FaUser,
    FaUserPlus,
} from "react-icons/fa";
import * as authApi from "../../services/auth";
import tokenService from "../../services/token.service";
import "./login.css";

export default function Login() {
    const [identifier, setIdentifier] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    function validate() {
        const trimmed = identifier.trim();
        if (!trimmed) {
            return "Por favor, introduce tu usuario o correo electrónico.";
        }
        if (!password) {
            return "Por favor, introduce tu contraseña.";
        }
        return null;
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");

        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setSubmitting(true);
        try {
            const data = await authApi.signin({
                username: identifier.trim(),
                password,
            });

            tokenService.setUser(data);
            tokenService.updateLocalAccessToken(data.token);
            window.location.href = "/";
        } catch (err) {
            const rawMessage = typeof err.response?.data === "string"
                ? err.response.data
                : (err.response?.data?.message || err.response?.data?.detail || "");

            if (
                rawMessage.includes("Bad Credentials") ||
                rawMessage.includes("Bad credentials") ||
                err.response?.status === 400 ||
                err.response?.status === 401
            ) {
                setError("Usuario, correo electrónico o contraseña incorrectos.");
            } else if (rawMessage) {
                setError(rawMessage);
            } else {
                setError("No se ha podido iniciar sesión. Comprueba tus datos e inténtalo de nuevo.");
            }
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="login-page">
            <div className="login-container">
                <h1 className="login-plaque" id="login-heading">
                    <FaUser className="login-plaque-icon" aria-hidden="true" />
                    <span>Inicio de sesión</span>
                </h1>

                <section className="login-card" aria-labelledby="login-heading">
                    {error ? (
                        <div className="login-error-banner" role="alert">
                            {error}
                        </div>
                    ) : null}

                    <form onSubmit={handleSubmit} noValidate>
                        <div className="login-form-group">
                            <label htmlFor="login-identifier" className="login-label">
                                <FaUser className="login-label-icon" aria-hidden="true" />
                                <span>Usuario o correo electrónico *</span>
                            </label>
                            <div className="login-input-wrapper">
                                <input
                                    id="login-identifier"
                                    name="identifier"
                                    type="text"
                                    className="login-input"
                                    placeholder="Introduce tu usuario o correo electrónico"
                                    value={identifier}
                                    onChange={(e) => {
                                        setIdentifier(e.target.value);
                                        if (error) setError("");
                                    }}
                                    autoComplete="username"
                                    required
                                />
                            </div>
                        </div>

                        <div className="login-form-group">
                            <label htmlFor="login-password" className="login-label">
                                <FaLock className="login-label-icon" aria-hidden="true" />
                                <span>Contraseña *</span>
                            </label>
                            <div className="login-input-wrapper login-password-wrapper">
                                <input
                                    id="login-password"
                                    name="password"
                                    type={showPassword ? "text" : "password"}
                                    className="login-input login-password-input"
                                    placeholder="Introduce tu contraseña"
                                    value={password}
                                    onChange={(e) => {
                                        setPassword(e.target.value);
                                        if (error) setError("");
                                    }}
                                    autoComplete="current-password"
                                    required
                                />
                                <button
                                    type="button"
                                    className="login-password-toggle"
                                    onClick={() => setShowPassword((prev) => !prev)}
                                    aria-label={showPassword ? "Ocultar contraseña" : "Ver contraseña"}
                                    title={showPassword ? "Ocultar contraseña" : "Ver contraseña"}
                                >
                                    {showPassword ? <FaEyeSlash aria-hidden="true" /> : <FaEye aria-hidden="true" />}
                                </button>
                            </div>
                        </div>

                        <div className="login-action-section">
                            <button
                                type="submit"
                                className="login-submit-btn"
                                disabled={submitting}
                            >
                                <FaSignInAlt className="login-submit-icon" aria-hidden="true" />
                                <span>{submitting ? "Iniciando sesión..." : "Iniciar sesión"}</span>
                            </button>
                        </div>

                        <div className="login-divider" aria-hidden="true">
                            <span className="login-divider-line" />
                            <span className="login-divider-text">O</span>
                            <span className="login-divider-line" />
                        </div>

                        <div className="login-footer-section">
                            <Link to="/register" className="login-register-link-btn">
                                <FaUserPlus className="login-register-btn-icon" aria-hidden="true" />
                                <span>¿No tienes cuenta? Regístrate</span>
                            </Link>
                        </div>
                    </form>
                </section>
            </div>
        </main>
    );
}
