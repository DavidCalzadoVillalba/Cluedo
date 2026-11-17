import { Link } from "react-router-dom";
import { Button } from "reactstrap";
import { FaEye, FaPencilAlt } from "react-icons/fa";
import PropTypes from "prop-types";

export default function UserRow({ user }) {
  const statusLabels = { ACTIVE: "Activo", SUSPENDED: "Suspendido", BLOCKED: "Bloqueado" };
  return (
    <tr>
      <td>{user.username}</td>
      <td>{user.email || "—"}</td>
      <td>{user.authority.authority}</td>
      <td><span className={"user-status-badge status-" + (user.accountStatus || "ACTIVE").toLowerCase()}>{statusLabels[user.accountStatus] || "Activo"}</span></td>
      <td>
        <div className="d-flex gap-2">
          <Button
            size="sm"
            color="info"
            aria-label={"view-" + user.id}
            tag={Link}
            to={"/users/" + user.id}
          >
            <FaEye aria-hidden="true" /> Ver detalle
          </Button>
          <Button
            size="sm"
            color="warning"
            aria-label={"edit-" + user.id}
            tag={Link}
            to={"/users/" + user.id + "/edit"}
          >
            <FaPencilAlt aria-hidden="true" /> Editar
          </Button>
        </div>
      </td>
    </tr>
  );
}

// Optional to facilitate static checking
UserRow.propTypes = {
  user: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired,
    username: PropTypes.string.isRequired,
    email: PropTypes.string,
    accountStatus: PropTypes.string,
    authority: PropTypes.shape({ authority: PropTypes.string }).isRequired,
  }).isRequired,
};
