import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaSignOutAlt, FaTimes } from "react-icons/fa";
import * as authApi from "../../services/auth";
import tokenService from "../../services/token.service";
import "./logout.css";

export default function Logout() {
    const navigate = useNavigate();
    const [submitting, setSubmitting] = useState(false);

    function handleCancel() {
        if (window.history.length > 1) {
            navigate(-1);
        } else {
            navigate("/");
        }
    }

    async function handleConfirm() {
        setSubmitting(true);
        try {
            await authApi.signout();
        } catch {
            // Ignorar errores de red para asegurar que el cliente limpie la sesión
        } finally {
            tokenService.removeUser();
            window.dispatchEvent(new Event("auth-changed"));
            try {
                window.location.href = "/";
            } catch {
                navigate("/");
            }
        }
    }

    return (
        <main className="logout-page">
            <div className="logout-container">
                <h1 className="logout-plaque" id="logout-heading">
                    <FaSignOutAlt className="logout-plaque-icon" aria-hidden="true" />
                    <span>Cerrar sesión</span>
                </h1>

                <section className="logout-card" aria-labelledby="logout-heading">
                    <div className="logout-emblem" aria-hidden="true">
                        <div className="logout-emblem-circle">
                            <FaSignOutAlt className="logout-emblem-icon" />
                        </div>
                    </div>

                    <h2 className="logout-question">
                        ¿Estás seguro de que quieres cerrar tu sesión?
                    </h2>

                    <p className="logout-description">
                        Tras cerrar sesión, tendrás que iniciar sesión de nuevo para acceder a tu cuenta.
                    </p>

                    <div className="logout-divider" aria-hidden="true">
                        <span className="logout-divider-line"></span>
                        <span className="logout-divider-circle">o</span>
                        <span className="logout-divider-line"></span>
                    </div>

                    <div className="logout-actions">
                        <button
                            type="button"
                            className="logout-cancel-btn"
                            onClick={handleCancel}
                            aria-label="Cancelar cierre de sesión"
                        >
                            <FaTimes className="logout-btn-icon" aria-hidden="true" />
                            <span>Cancelar</span>
                        </button>
                        <button
                            type="button"
                            className="logout-confirm-btn"
                            onClick={handleConfirm}
                            disabled={submitting}
                            aria-label="Confirmar cierre de sesión"
                        >
                            <FaSignOutAlt className="logout-btn-icon" aria-hidden="true" />
                            <span>{submitting ? "Cerrando..." : "Cerrar sesión"}</span>
                        </button>
                    </div>
                </section>
            </div>
        </main>
    );
}
