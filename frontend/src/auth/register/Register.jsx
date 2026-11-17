import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
    FaArrowLeft,
    FaEnvelope,
    FaEye,
    FaEyeSlash,
    FaImage,
    FaLock,
    FaUser,
    FaUserPlus,
} from "react-icons/fa";
import { CHARACTERS, CHARACTER_IMAGES } from "../../game/gameConstants";
import * as authApi from "../../services/auth";
import tokenService from "../../services/token.service";
import "./register.css";

const DEFAULT_AVATAR = CHARACTER_IMAGES.PRADO || "/images/personajes/Prado.png";

export default function Register() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [avatar, setAvatar] = useState(DEFAULT_AVATAR);

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [showAvatarChoices, setShowAvatarChoices] = useState(false);

    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    function validate() {
        const trimmedUser = username.trim();
        const trimmedEmail = email.trim();

        if (!trimmedUser) {
            return "El nombre de usuario es obligatorio.";
        }
        if (trimmedUser.length > 255) {
            return "El nombre de usuario no puede superar los 255 caracteres.";
        }
        if (!trimmedEmail) {
            return "El correo electrónico es obligatorio.";
        }
        if (trimmedEmail.length > 100) {
            return "El correo electrónico no puede superar los 100 caracteres.";
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(trimmedEmail)) {
            return "Por favor, introduce un correo electrónico válido.";
        }
        if (!password) {
            return "La contraseña es obligatoria.";
        }
        if (password.length < 4) {
            return "La contraseña debe tener al menos 4 caracteres.";
        }
        if (password !== confirmPassword) {
            return "Las contraseñas no coinciden.";
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
        const payload = {
            username: username.trim(),
            email: email.trim(),
            password,
            avatar,
            authority: "PLAYER",
        };

        try {
            await authApi.signup(payload);
            const loginData = await authApi.signin({
                username: payload.username,
                password: payload.password,
            });

            tokenService.setUser(loginData);
            tokenService.updateLocalAccessToken(loginData.token);
            window.location.href = "/";
        } catch (err) {
            const rawMessage = typeof err.response?.data === "string"
                ? err.response.data
                : (err.response?.data?.message || err.response?.data?.detail || "");
            if (rawMessage.includes("Username is already taken") || rawMessage.includes("nombre de usuario ya está")) {
                setError("El nombre de usuario ya está registrado por otro jugador.");
            } else if (rawMessage.includes("Email is already registered") || rawMessage.includes("correo electrónico ya está")) {
                setError("El correo electrónico ya está registrado por otro jugador.");
            } else if (rawMessage) {
                setError(rawMessage);
            } else {
                setError("No se ha podido completar el registro. Comprueba los datos e inténtalo de nuevo.");
            }
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="register-page">
            <div className="register-container">
                <div className="register-plaque" id="register-heading">
                    <FaUserPlus className="register-plaque-icon" aria-hidden="true" />
                    <span>Registro de usuario</span>
                </div>

                <section className="register-card" aria-labelledby="register-heading">
                    <div className="register-avatar-section">
                        <div className="register-avatar-preview" title="Avatar seleccionado">
                            {avatar ? (
                                <img
                                    src={avatar}
                                    alt="Avatar seleccionado"
                                    className="register-avatar-img"
                                />
                            ) : (
                                <FaUser className="register-avatar-placeholder" aria-hidden="true" />
                            )}
                        </div>

                        <button
                            type="button"
                            className="register-avatar-btn-toggle"
                            onClick={() => setShowAvatarChoices((prev) => !prev)}
                            aria-expanded={showAvatarChoices}
                            aria-controls="register-avatar-grid"
                        >
                            <FaImage aria-hidden="true" />
                            <span>{showAvatarChoices ? "Ocultar avatares" : "Cambiar avatar"}</span>
                        </button>

                        {showAvatarChoices && (
                            <div
                                id="register-avatar-grid"
                                className="register-avatar-grid"
                                role="group"
                                aria-label="Selección de avatar de personaje"
                            >
                                {CHARACTERS.map((character) => {
                                    const src = CHARACTER_IMAGES[character.apiValue];
                                    const isSelected = avatar === src;
                                    return (
                                        <button
                                            type="button"
                                            key={character.id}
                                            className={`register-avatar-choice-btn ${isSelected ? "is-active" : ""}`}
                                            onClick={() => {
                                                setAvatar(src);
                                                setShowAvatarChoices(false);
                                            }}
                                            aria-label={`Seleccionar avatar de ${character.name}`}
                                            title={character.name}
                                        >
                                            <img
                                                src={src}
                                                alt={character.name}
                                                className="register-avatar-choice-img"
                                            />
                                        </button>
                                    );
                                })}
                            </div>
                        )}
                    </div>

                    {error && (
                        <div className="register-alert register-alert-error" role="alert">
                            {error}
                        </div>
                    )}

                    <form className="register-form" onSubmit={handleSubmit} noValidate>
                        <div className="register-input-group">
                            <label htmlFor="register-username" className="register-input-label">
                                <FaUser aria-hidden="true" />
                                <span>Nombre de usuario *</span>
                            </label>
                            <div className="register-input-wrapper">
                                <input
                                    id="register-username"
                                    name="username"
                                    type="text"
                                    className="register-input"
                                    placeholder="Introduce tu nombre de usuario"
                                    autoComplete="username"
                                    maxLength={255}
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <div className="register-input-group">
                            <label htmlFor="register-email" className="register-input-label">
                                <FaEnvelope aria-hidden="true" />
                                <span>Correo electrónico *</span>
                            </label>
                            <div className="register-input-wrapper">
                                <input
                                    id="register-email"
                                    name="email"
                                    type="email"
                                    className="register-input"
                                    placeholder="Introduce tu correo electrónico"
                                    autoComplete="email"
                                    maxLength={100}
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <div className="register-input-group">
                            <label htmlFor="register-password" className="register-input-label">
                                <FaLock aria-hidden="true" />
                                <span>Contraseña *</span>
                            </label>
                            <div className="register-input-wrapper">
                                <input
                                    id="register-password"
                                    name="password"
                                    type={showPassword ? "text" : "password"}
                                    className="register-input register-input-with-eye"
                                    placeholder="Introduce tu contraseña"
                                    autoComplete="new-password"
                                    maxLength={100}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                                <button
                                    type="button"
                                    className="register-eye-btn"
                                    onClick={() => setShowPassword((prev) => !prev)}
                                    aria-label={showPassword ? "Ocultar contraseña" : "Ver contraseña"}
                                    tabIndex={-1}
                                >
                                    {showPassword ? <FaEyeSlash aria-hidden="true" /> : <FaEye aria-hidden="true" />}
                                </button>
                            </div>
                        </div>

                        <div className="register-input-group">
                            <label htmlFor="register-confirm-password" className="register-input-label">
                                <FaLock aria-hidden="true" />
                                <span>Confirmar contraseña *</span>
                            </label>
                            <div className="register-input-wrapper">
                                <input
                                    id="register-confirm-password"
                                    name="confirmPassword"
                                    type={showConfirmPassword ? "text" : "password"}
                                    className="register-input register-input-with-eye"
                                    placeholder="Confirma tu contraseña"
                                    autoComplete="new-password"
                                    maxLength={100}
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                />
                                <button
                                    type="button"
                                    className="register-eye-btn"
                                    onClick={() => setShowConfirmPassword((prev) => !prev)}
                                    aria-label={showConfirmPassword ? "Ocultar confirmación de contraseña" : "Ver confirmación de contraseña"}
                                    tabIndex={-1}
                                >
                                    {showConfirmPassword ? <FaEyeSlash aria-hidden="true" /> : <FaEye aria-hidden="true" />}
                                </button>
                            </div>
                        </div>

                        <button
                            type="submit"
                            className="register-submit-btn"
                            disabled={submitting}
                        >
                            <FaUserPlus aria-hidden="true" />
                            <span>{submitting ? "Registrando…" : "Registrarse"}</span>
                        </button>
                    </form>

                    <div className="register-divider">
                        <span>o</span>
                    </div>

                    <Link to="/login" className="register-back-link">
                        <FaArrowLeft aria-hidden="true" />
                        <span>Volver al inicio de sesión</span>
                    </Link>
                </section>
            </div>
        </main>
    );
}
