import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { Button } from "reactstrap";
import {
  FaArrowLeft,
  FaCheckCircle,
  FaChevronDown,
  FaCog,
  FaExclamationTriangle,
  FaPencilAlt,
  FaShieldAlt,
  FaTimes,
  FaTrash,
} from "react-icons/fa";
import * as usersApi from "../../services/users";
import useErrorModal from "../../hooks/useErrorModal";
import useFetchState from "../../hooks/useFetchState";
import "../../static/css/admin/userDetailAdmin.css";

const statusLabels = { ACTIVE: "Activo", SUSPENDED: "Suspendido", BLOCKED: "Bloqueado" };
const roleLabels = { PLAYER: "Jugador", ADMIN: "Administrador" };

export default function UserDetailAdmin() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { errorModal, showError } = useErrorModal();
  const [user] = useFetchState(null, () => usersApi.getUserDetails(id), [id], { onError: showError });
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showSuccessToast, setShowSuccessToast] = useState(false);
  const [deleting, setDeleting] = useState(false);

    async function handleConfirmDelete() {
      setDeleting(true);
      try {
        await usersApi.deleteUser(user.id);
        setShowDeleteModal(false);
        setShowSuccessToast(true);
        window.setTimeout(() => {
          navigate("/social-hub?tab=admin-users");
        }, 1400);
      } catch (error) {
        showError(error.response?.data?.message ?? "No se pudo eliminar el usuario.");
        setDeleting(false);
        setShowDeleteModal(false);
      }
    }

    const initials = user?.username?.trim().slice(0, 2).toUpperCase() || "?";
    const status = user?.accountStatus || "ACTIVE";

    return (
      <main className="user-detail-page">
        {errorModal}

        {!user ? (
        <section className="user-detail-card user-detail-loading" role="status">
          Cargando perfil…
        </section>
        ) : (
          <section className="user-detail-card" aria-labelledby="user-detail-title">
              <div className="user-detail-top-badge" aria-hidden="true">
                <FaCog /> Administración
              </div>

              <header className="user-detail-header-bar">
                <Button
                  outline
                  tag={Link}
                  to="/social-hub?tab=admin-users"
                  className="user-detail-back-btn"
                >
                  <FaArrowLeft aria-hidden="true" /> Volver al listado
                </Button>
                <div className="user-detail-title-line">
                  <h1 id="user-detail-title">Gestión de Usuarios</h1>
                </div>
                <div className="user-detail-header-spacer" aria-hidden="true" />
              </header>

              <div className="user-detail-content">
                <aside className="user-detail-profile">
                  <div className="user-detail-avatar-wrapper">
                    {user.avatar ? (
                      <img
                        className="user-detail-avatar"
                        src={user.avatar}
                        alt={"Avatar de " + user.username}
                        onError={(event) => {
                          event.currentTarget.hidden = true;
                        }}
                      />
                    ) : (
                      <div
                        className="user-detail-avatar user-detail-avatar-placeholder"
                        aria-label="Sin avatar"
                      >
                        {initials}
                      </div>
                    )}
                  </div>
                  <h2 className="user-detail-username">{user.username}</h2>
                  <span className={"user-detail-status-pill status-" + status.toLowerCase()}>
                    <span className="status-dot" aria-hidden="true" />
                    {statusLabels[status] || status}
                  </span>
                </aside>

                <div className="user-detail-fields">
                  <div className="user-detail-input-box">
                    <span className="user-detail-input-value">{user.username}</span>
                  </div>

                  <div className="user-detail-input-box">
                    <span className="user-detail-input-value">
                      {user.email || "No disponible"}
                    </span>
                  </div>

                  <div className="user-detail-input-box">
                    <span className="user-detail-input-with-icon">
                      <FaShieldAlt className="user-detail-box-icon" aria-hidden="true" />
                      {roleLabels[user.authority] || user.authority || "Jugador"}
                    </span>
                    <FaChevronDown className="user-detail-chevron" aria-hidden="true" />
                  </div>

                  <div className="user-detail-input-box">
                    <span className="user-detail-input-with-icon">
                      <span
                        className={"status-dot status-dot-" + status.toLowerCase()}
                        aria-hidden="true"
                      />
                      {statusLabels[status] || status}
                    </span>
                    <FaChevronDown className="user-detail-chevron" aria-hidden="true" />
                  </div>

                  {(user.finishedGames !== undefined || user.wins !== undefined) && (
                    <div className="user-detail-stats-bar">
                      <span>
                        Partidas finalizadas: <strong>{user.finishedGames ?? 0}</strong>
                      </span>
                      <span>
                        Victorias: <strong>{user.wins ?? 0}</strong>
                      </span>
                    </div>
                  )}
                </div>
              </div>

              <footer className="user-detail-actions">
                <Button
                  tag={Link}
                  to={"/users/" + user.id + "/edit"}
                  className="user-detail-edit-btn"
                >
                  <FaPencilAlt aria-hidden="true" /> Editar usuario
                </Button>
                <Button
                  color="danger"
                  onClick={() => setShowDeleteModal(true)}
                  className="user-detail-delete-btn"
                  aria-label="Eliminar cuenta"
                >
                  <FaTrash aria-hidden="true" /> Eliminar cuenta
                </Button>
              </footer>
          </section>
        )}

        {/* Custom Delete Confirmation Modal */}
        {showDeleteModal && (
          <div
            className="custom-delete-modal-overlay"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-modal-title"
          >
            <div className="custom-delete-modal-card">
              <button
                type="button"
                className="custom-delete-modal-close"
                onClick={() => !deleting && setShowDeleteModal(false)}
                aria-label="Cerrar modal"
                disabled={deleting}
              >
                <FaTimes aria-hidden="true" />
              </button>

              <div className="custom-delete-modal-icon-wrap" aria-hidden="true">
                <FaExclamationTriangle className="custom-delete-modal-icon" />
              </div>

              <h2 id="delete-modal-title" className="custom-delete-modal-title">
                ¿Eliminar usuario?
              </h2>

              <p className="custom-delete-modal-desc">
                ¿Estás seguro de que quieres eliminar la cuenta de{" "}
                <strong>{user?.username}</strong>?
              </p>

              <div className="custom-delete-modal-actions">
                <button
                  type="button"
                  className="custom-delete-modal-btn-cancel"
                  onClick={() => setShowDeleteModal(false)}
                  disabled={deleting}
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  className="custom-delete-modal-btn-confirm"
                  onClick={handleConfirmDelete}
                  disabled={deleting}
                  aria-label="Confirmar eliminación"
                >
                  <FaTrash aria-hidden="true" /> {deleting ? "Eliminando…" : "Eliminar"}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Success Notification Toast */}
        {showSuccessToast && (
          <div className="custom-delete-toast" role="status" aria-live="polite">
            <FaCheckCircle className="custom-delete-toast-icon" aria-hidden="true" />
            <span className="custom-delete-toast-text">Usuario eliminado correctamente.</span>
            <button
              type="button"
              className="custom-delete-toast-close"
              onClick={() => setShowSuccessToast(false)}
              aria-label="Cerrar confirmación"
            >
              <FaTimes aria-hidden="true" />
            </button>
          </div>
        )}
      </main>
    );
  }
