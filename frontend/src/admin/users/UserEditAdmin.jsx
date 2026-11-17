import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { Alert, Button, Form, Input, Label } from "reactstrap";
import {
  FaArrowLeft,
  FaEnvelope,
  FaImage,
  FaInfoCircle,
  FaLock,
  FaSave,
  FaShieldAlt,
  FaUser,
  FaUserCheck,
} from "react-icons/fa";
import * as usersApi from "../../services/users";
import useErrorModal from "../../hooks/useErrorModal";
import useFetchState from "../../hooks/useFetchState";
import "../../static/css/admin/adminCreateUser.css";

const STATUS_OPTIONS = [
  { value: "ACTIVE", label: "Activo" },
  { value: "SUSPENDED", label: "Suspendido" },
  { value: "BLOCKED", label: "Bloqueado" },
];

export default function UserEditAdmin() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { errorModal, showError } = useErrorModal();

  const [authorities] = useFetchState([], usersApi.getUserAuthorities, [], { onError: showError });
  const [initialUser] = useFetchState(null, () => usersApi.getUserById(id), [id], { onError: showError });

  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    authority: "PLAYER",
    accountStatus: "ACTIVE",
    avatar: "",
  });

  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState("");

  useEffect(() => {
    if (initialUser) {
      setFormData({
        username: initialUser.username || "",
        email: initialUser.email || "",
        password: "",
        authority: initialUser.authority?.authority || "PLAYER",
        accountStatus: initialUser.accountStatus || "ACTIVE",
        avatar: initialUser.avatar || "",
      });
    }
  }, [initialUser]);

  function handleChange(event) {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setSuccess("");

    try {
      const selectedAuth = authorities.find((a) => a.authority === formData.authority) || {
        authority: formData.authority,
      };

      const payload = {
        id: Number(id),
        username: formData.username.trim(),
        email: formData.email.trim(),
        authority: selectedAuth,
        accountStatus: formData.accountStatus,
        avatar: formData.avatar ? formData.avatar.trim() : null,
      };

      if (formData.password && formData.password.trim().length > 0) {
        payload.password = formData.password;
      }

      await usersApi.updateUser(id, payload);
      setSuccess("Los cambios se han guardado correctamente.");
      window.setTimeout(() => navigate("/users"), 1000);
    } catch (err) {
      const data = err.response?.data;
      showError(
        data?.message ??
          data?.detail ??
          "No se pudieron guardar los cambios. Revisa los datos e inténtalo de nuevo."
      );
    } finally {
      setSaving(false);
    }
  }

  const avatarInitials = formData.username?.trim().slice(0, 2).toUpperCase() || "?";

  return (
    <main className="user-create-page">
      {errorModal}
      <section className="user-create-card" aria-labelledby="edit-user-title">
        <header className="user-create-heading">
          <span className="user-create-heading-icon" aria-hidden="true">
            <FaShieldAlt />
          </span>
          <div>
            <p>Administración</p>
            <h1 id="edit-user-title">Editar usuario #{id}</h1>
          </div>
        </header>

        {success && (
          <Alert color="success" role="status" className="mb-4" fade={false}>
            {success}
          </Alert>
        )}

        {!initialUser ? (
          <div className="text-center py-5" role="status" style={{ color: "#e9d9b2" }}>
            Cargando datos del usuario…
          </div>
        ) : (
          <Form onSubmit={handleSubmit}>
            <div className="user-create-content">
              <aside className="user-avatar-panel">
                {formData.avatar ? (
                  <img
                    className="user-avatar-preview"
                    src={formData.avatar}
                    alt={"Avatar de " + formData.username}
                    onError={(e) => {
                      e.currentTarget.hidden = true;
                    }}
                  />
                ) : (
                  <div className="user-avatar-placeholder" aria-label="Avatar predeterminado">
                    {avatarInitials}
                  </div>
                )}
                <Label className="avatar-url-label" for="avatar">
                  Avatar (URL, opcional)
                </Label>
                <div className="avatar-url-control">
                  <FaImage aria-hidden="true" />
                  <Input
                    id="avatar"
                    name="avatar"
                    value={formData.avatar}
                    onChange={handleChange}
                    placeholder="https://…"
                    maxLength={255}
                  />
                </div>
              </aside>

              <div className="user-create-fields">
                <div className="create-field">
                  <Label for="username">
                    <FaUser aria-hidden="true" />
                    Nombre de usuario
                  </Label>
                  <Input
                    id="username"
                    name="username"
                    autoComplete="off"
                    value={formData.username}
                    onChange={handleChange}
                    required
                    maxLength={255}
                  />
                </div>

                <div className="create-field">
                  <Label for="email">
                    <FaEnvelope aria-hidden="true" />
                    Correo electrónico
                  </Label>
                  <Input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="off"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    maxLength={100}
                  />
                </div>

                <div className="create-field">
                  <Label for="authority">
                    <FaShieldAlt aria-hidden="true" />
                    Rol / Autoridad
                  </Label>
                  <Input
                    id="authority"
                    name="authority"
                    type="select"
                    value={formData.authority}
                    onChange={handleChange}
                    required
                  >
                    {authorities.length > 0 ? (
                      authorities.map((auth) => (
                        <option key={auth.id || auth.authority} value={auth.authority}>
                          {auth.authority === "PLAYER"
                            ? "Jugador"
                            : auth.authority === "ADMIN"
                            ? "Administrador"
                            : auth.authority}
                        </option>
                      ))
                    ) : (
                      <>
                        <option value="PLAYER">Jugador</option>
                        <option value="ADMIN">Administrador</option>
                      </>
                    )}
                  </Input>
                </div>

                <div className="create-field">
                  <Label for="accountStatus">
                    <FaUserCheck aria-hidden="true" />
                    Estado de la cuenta
                  </Label>
                  <Input
                    id="accountStatus"
                    name="accountStatus"
                    type="select"
                    value={formData.accountStatus}
                    onChange={handleChange}
                    required
                  >
                    {STATUS_OPTIONS.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </Input>
                </div>

                <div className="create-field" style={{ gridColumn: "1 / -1" }}>
                  <Label for="password">
                    <FaLock aria-hidden="true" />
                    Nueva contraseña (dejar en blanco para conservar la actual)
                  </Label>
                  <Input
                    id="password"
                    name="password"
                    type="password"
                    autoComplete="new-password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="••••••••"
                  />
                </div>

                <div className="user-create-note">
                  <FaInfoCircle aria-hidden="true" />
                  <span>
                    Modifica los datos del usuario y pulsa Guardar cambios. El servidor verificará
                    los permisos y la validez de los datos antes de aplicarlos.
                  </span>
                </div>
              </div>
            </div>

            <footer className="user-create-actions">
              <Button outline tag={Link} to="/users" className="create-cancel">
                <FaArrowLeft aria-hidden="true" />
                Cancelar
              </Button>
              <Button
                type="submit"
                className="create-submit"
                disabled={saving}
              >
                <FaSave aria-hidden="true" />
                {saving ? "Guardando…" : "Guardar cambios"}
              </Button>
            </footer>
          </Form>
        )}
      </section>
    </main>
  );
}
