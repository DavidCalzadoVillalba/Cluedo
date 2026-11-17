import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Button, Input, Table } from "reactstrap";
import { FaChevronLeft, FaChevronRight, FaPlus, FaSearch, FaUsers } from "react-icons/fa";
import * as usersApi from "../../services/users";
import "../../static/css/admin/adminPage.css";
import "../../static/css/admin/usersAdmin.css";
import useErrorModal from "../../hooks/useErrorModal";
import useFetchState from "../../hooks/useFetchState";
import UserRow from "./UserRow";

export default function UserListAdmin() {
  const { errorModal, showError } = useErrorModal();
  const [users, setUsers] = useFetchState([], usersApi.getAllUsers, [], {
    onError: showError,
  });
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const pageSize = 8;
  const filteredUsers = useMemo(() => {
    const term = search.trim().toLocaleLowerCase();
    return users.filter((user) =>
      [user.username, user.email, user.authority?.authority, user.accountStatus]
        .some((value) => String(value ?? "").toLocaleLowerCase().includes(term))
    );
  }, [users, search]);
  const pageCount = Math.max(1, Math.ceil(filteredUsers.length / pageSize));
  const visibleUsers = filteredUsers.slice(page * pageSize, (page + 1) * pageSize);

  return (
    <main className="admin-page-container users-admin-page">
      {errorModal}
      <section className="users-admin-card">
        <header className="users-admin-header">
          <div className="users-admin-title">
            <FaUsers aria-hidden="true" />
            <div>
              <p>Administración</p>
              <h1>Usuarios</h1>
              <span>Gestiona las cuentas registradas en el sistema.</span>
            </div>
          </div>
          <Button className="users-create-button" tag={Link} to="/users/new">
            <FaPlus aria-hidden="true" /> Crear usuario
          </Button>
        </header>
        <div className="users-search">
          <FaSearch aria-hidden="true" />
          <Input
            type="search"
            aria-label="Buscar usuarios"
            placeholder="Buscar por nombre, correo, rol o estado"
            value={search}
            onChange={(event) => { setSearch(event.target.value); setPage(0); }}
          />
        </div>
        <div className="users-admin-table-wrap">
          <Table aria-label="users" className="users-admin-table">
            <thead>
              <tr>
                <th>Nombre de usuario</th>
                <th>Correo electrónico</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {visibleUsers.map((user) => (
                <UserRow
                  key={user.id}
                  user={user}
                />
              ))}
              {visibleUsers.length === 0 && (
                <tr><td colSpan="5" className="users-empty-state">
                  {search ? "No se encontraron usuarios con esa búsqueda." : "Todavía no hay usuarios registrados."}
                </td></tr>
              )}
            </tbody>
          </Table>
        </div>
        <footer className="users-list-footer">
          <span>{filteredUsers.length} {filteredUsers.length === 1 ? "usuario" : "usuarios"}</span>
          <div className="users-pagination" aria-label="Paginación de usuarios">
            <Button outline aria-label="Página anterior" disabled={page === 0} onClick={() => setPage((current) => current - 1)}><FaChevronLeft /></Button>
            <span>Página {page + 1} de {pageCount}</span>
            <Button outline aria-label="Página siguiente" disabled={page + 1 >= pageCount} onClick={() => setPage((current) => current + 1)}><FaChevronRight /></Button>
          </div>
        </footer>
      </section>
    </main>
  );
}
