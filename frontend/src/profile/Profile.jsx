import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaArrowLeft, FaEnvelope, FaImage, FaInfoCircle, FaSave, FaSignOutAlt, FaUser } from "react-icons/fa";
import { CHARACTERS, CHARACTER_IMAGES } from "../game/gameConstants";
import { getMyProfile, updateMyProfile } from "../services/profile";
import "./profile.css";

const getErrorMessage = (error, fallback) =>
    typeof error.response?.data?.message === "string"
        ? error.response.data.message
        : fallback;

function validateProfile(username, email) {
    if (!username.trim()) return "El nombre de usuario es obligatorio";
    if (username.trim().length > 255) return "El nombre de usuario no puede superar los 255 caracteres";
    if (!email.trim()) return "El correo electrónico es obligatorio";
    if (email.trim().length > 100) return "El correo electrónico no puede superar los 100 caracteres";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) return "El correo electrónico no es válido";
    return "";
}

export default function Profile() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [avatar, setAvatar] = useState("");
    const [saved, setSaved] = useState(null);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [choosingAvatar, setChoosingAvatar] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        let mounted = true;
        getMyProfile()
            .then((profile) => { if (mounted) applyProfile(profile); })
            .catch(() => { if (mounted) setLoadError(true); })
            .finally(() => { if (mounted) setLoading(false); });
        return () => { mounted = false; };
    }, []);

    function applyProfile(profile) {
        setSaved(profile);
        setUsername(profile.username ?? "");
        setEmail(profile.email ?? "");
        setAvatar(profile.avatar ?? "");
    }

    function restoreSaved() {
        if (!saved) return;
        setUsername(saved.username ?? "");
        setEmail(saved.email ?? "");
        setAvatar(saved.avatar ?? "");
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setSuccess("");
        const validationMessage = validateProfile(username, email);
        if (validationMessage) {
            setError(validationMessage);
            return;
        }

        setError("");
        setSubmitting(true);
        try {
            const updated = await updateMyProfile({
                username: username.trim(),
                email: email.trim(),
                avatar: avatar.trim() ? avatar.trim() : null,
            });
            applyProfile(updated);
            setChoosingAvatar(false);
            setSuccess("Perfil actualizado correctamente.");
        } catch (updateError) {
            restoreSaved();
            setError(getErrorMessage(updateError, "No se han podido guardar los cambios."));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="profile-page">
            <section className="profile-panel" aria-labelledby="profile-title">
                <h1 id="profile-title"><FaUser aria-hidden="true" /> Mi perfil</h1>
                {loading ? <p className="profile-message" role="status">Cargando perfil…</p> : null}
                {loadError ? (
                    <p className="profile-message profile-error" role="alert">
                        No se ha podido cargar el perfil. Inténtalo de nuevo más tarde.
                    </p>
                ) : null}
                {!loading && !loadError ? (
                    <form onSubmit={handleSubmit} noValidate>
                        <div className="profile-layout">
                            <div className="profile-avatar-column">
                                <div className="profile-avatar-preview">
                                    {avatar ? <img className="profile-avatar-photo" src={avatar} alt="Avatar actual" /> : <FaUser aria-hidden="true" />}
                                </div>
                                <p className="profile-avatar-label" id="avatar-label">
                                    <FaImage aria-hidden="true" /> Avatar
                                </p>
                                <button
                                    type="button"
                                    className="profile-secondary"
                                    aria-expanded={choosingAvatar}
                                    aria-controls="avatar-choices"
                                    onClick={() => setChoosingAvatar((open) => !open)}
                                >
                                    <FaImage aria-hidden="true" /> Cambiar avatar
                                </button>
                                {choosingAvatar ? (
                                    <div className="profile-avatar-choices" id="avatar-choices" role="group" aria-labelledby="avatar-label">
                                        {CHARACTERS.map((character) => {
                                            const src = CHARACTER_IMAGES[character.apiValue];
                                            return (
                                                <button
                                                    type="button"
                                                    key={character.id}
                                                    className={avatar === src ? "is-selected" : ""}
                                                    aria-pressed={avatar === src}
                                                    aria-label={`Seleccionar avatar de ${character.name}`}
                                                    onClick={() => {
                                                        setAvatar(src);
                                                        setSuccess("");
                                                    }}
                                                >
                                                    <img className="profile-avatar-photo" src={src} alt="" />
                                                </button>
                                            );
                                        })}
                                    </div>
                                ) : null}
                            </div>
                            <div className="profile-fields">
                                <div className="profile-field">
                                    <label htmlFor="profile-username"><FaUser aria-hidden="true" /> Nombre de usuario</label>
                                    <input
                                        id="profile-username"
                                        name="username"
                                        autoComplete="username"
                                        maxLength={255}
                                        value={username}
                                        onChange={(event) => {
                                            setUsername(event.target.value);
                                            setSuccess("");
                                        }}
                                    />
                                </div>
                                <div className="profile-field">
                                    <label htmlFor="profile-email"><FaEnvelope aria-hidden="true" /> Correo electrónico</label>
                                    <input
                                        id="profile-email"
                                        name="email"
                                        type="email"
                                        autoComplete="email"
                                        maxLength={100}
                                        value={email}
                                        onChange={(event) => {
                                            setEmail(event.target.value);
                                            setSuccess("");
                                        }}
                                    />
                                </div>
                                <p className="profile-note">
                                    <FaInfoCircle aria-hidden="true" />
                                    <span>Aquí puedes actualizar tu información personal y tu avatar.</span>
                                </p>
                            </div>
                        </div>
                        {error ? <p className="profile-message profile-error" role="alert">{error}</p> : null}
                        {success ? <p className="profile-message profile-success" role="status">{success}</p> : null}
                        <div className="profile-actions">
                            <button type="button" className="profile-secondary" onClick={() => navigate("/")}>
                                <FaArrowLeft aria-hidden="true" /> Cancelar
                            </button>
                            <button type="submit" className="profile-primary" disabled={submitting}>
                                <FaSave aria-hidden="true" /> Guardar cambios
                            </button>
                        </div>
                    </form>
                ) : null}

                {!loading && !loadError ? (
                    <div className="profile-logout-section">
                        <div className="profile-logout-info">
                            <span className="profile-logout-title">
                                <FaSignOutAlt aria-hidden="true" /> Sesión de usuario
                            </span>
                            <span className="profile-logout-desc">
                                Finaliza tu sesión actual en la plataforma.
                            </span>
                        </div>
                        <button
                            type="button"
                            className="profile-logout-btn"
                            onClick={() => navigate("/logout")}
                        >
                            <FaSignOutAlt aria-hidden="true" /> Cerrar sesión
                        </button>
                    </div>
                ) : null}
            </section>
        </main>
    );
}
