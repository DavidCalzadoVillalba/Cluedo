import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Alert, Button, Form, Input, Label } from "reactstrap";
import { FaArrowLeft, FaEnvelope, FaImage, FaInfoCircle, FaLock, FaSave, FaShieldAlt, FaUser } from "react-icons/fa";
import * as usersApi from "../../services/users";
import useErrorModal from "../../hooks/useErrorModal";
import useFetchState from "../../hooks/useFetchState";
import "../../static/css/admin/adminCreateUser.css";

export default function UserCreateAdmin() {
  const navigate = useNavigate();
  const { errorModal, showError } = useErrorModal();
  const [authorities] = useFetchState([], usersApi.getUserAuthorities, [], { onError: showError });
  const [user, setUser] = useState({ username: "", email: "", password: "", authority: "", avatar: "" });
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState("");

  function handleChange(event) {
    setUser((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setSuccess("");
    try {
      const authority = authorities.find((candidate) => candidate.authority === user.authority);
      await usersApi.createUser({ ...user, authority, username: user.username.trim(), email: user.email.trim() });
      setSuccess("El usuario se ha creado correctamente.");
      window.setTimeout(() => navigate("/users"), 900);
    } catch (err) {
      const data = err.response?.data;
      showError(data?.message ?? data?.detail ?? "No se pudo crear el usuario. Revisa los datos e inténtalo de nuevo.");
    } finally {
      setSaving(false);
    }
  }

  const avatarInitials = user.username.trim().slice(0, 2).toUpperCase() || "?";
  return (
    <main className="user-create-page">
      <section className="user-create-card" aria-labelledby="create-user-title">
        <header className="user-create-heading">
          <span className="user-create-heading-icon" aria-hidden="true"><FaShieldAlt /></span>
          <div><p>Administración</p><h1 id="create-user-title">Crear nuevo usuario</h1></div>
        </header>
        {errorModal}
        {success && <Alert color="success" role="status">{success}</Alert>}
        <Form onSubmit={handleSubmit}>
          <div className="user-create-content">
            <aside className="user-avatar-panel">
              {user.avatar ? <img className="user-avatar-preview" src={user.avatar} alt="Vista previa del avatar" onError={(event) => { event.currentTarget.hidden = true; }} /> : <div className="user-avatar-placeholder" aria-label="Avatar predeterminado">{avatarInitials}</div>}
              <Label className="avatar-url-label" for="avatar">Avatar (URL, opcional)</Label>
              <div className="avatar-url-control"><FaImage aria-hidden="true" /><Input id="avatar" name="avatar" value={user.avatar} onChange={handleChange} placeholder="https://…" maxLength={255} /></div>
            </aside>
            <div className="user-create-fields">
              <div className="create-field"><Label for="username"><FaUser aria-hidden="true" />Nombre de usuario</Label><Input id="username" name="username" autoComplete="off" value={user.username} onChange={handleChange} required maxLength={255} /></div>
              <div className="create-field"><Label for="email"><FaEnvelope aria-hidden="true" />Correo electrónico</Label><Input id="email" name="email" type="email" autoComplete="off" value={user.email} onChange={handleChange} required maxLength={100} /></div>
              <div className="create-field"><Label for="password"><FaLock aria-hidden="true" />Contraseña inicial</Label><Input id="password" name="password" type="password" autoComplete="new-password" value={user.password} onChange={handleChange} required /></div>
              <div className="create-field"><Label for="authority"><FaShieldAlt aria-hidden="true" />Rol / Autoridad</Label><Input id="authority" name="authority" type="select" value={user.authority} onChange={handleChange} required><option value="">Selecciona un rol</option>{authorities.map((authority) => <option key={authority.id} value={authority.authority}>{authority.authority === "PLAYER" ? "Jugador" : authority.authority === "ADMIN" ? "Administrador" : authority.authority}</option>)}</Input></div>
              <div className="user-create-note"><FaInfoCircle aria-hidden="true" /><span>La contraseña se almacenará de forma segura y el usuario podrá acceder con estas credenciales.</span></div>
            </div>
          </div>
          <footer className="user-create-actions">
            <Button outline tag={Link} to="/users" className="create-cancel"><FaArrowLeft aria-hidden="true" />Cancelar</Button>
            <Button type="submit" className="create-submit" disabled={saving || authorities.length === 0}><FaSave aria-hidden="true" />{saving ? "Creando…" : "Crear usuario"}</Button>
          </footer>
        </Form>
      </section>
    </main>
  );
}
