import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, test, vi } from "vitest";
import { render } from "../../test-utils";
import * as usersApi from "../../services/users";
import UserDetailAdmin from "./UserDetailAdmin";

const navigate = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigate,
    useParams: () => ({ id: "1" }),
  };
});

vi.mock("../../services/users", () => ({
  getUserDetails: vi.fn(),
  deleteUser: vi.fn(),
}));

const mockUser = {
  id: 1,
  username: "DavidCalzado",
  email: "david.calzado@ejemplo.com",
  accountStatus: "ACTIVE",
  authority: "PLAYER",
  finishedGames: 5,
  wins: 2,
};

describe("UserDetailAdmin Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    usersApi.getUserDetails.mockResolvedValue(mockUser);
    usersApi.deleteUser.mockResolvedValue({});
  });

  test("renders user profile, fields and action buttons", async () => {
    render(<UserDetailAdmin />);

    expect(await screen.findByRole("heading", { name: "DavidCalzado" })).toBeInTheDocument();
    expect(screen.getByText("david.calzado@ejemplo.com")).toBeInTheDocument();
    expect(screen.getByText("Jugador")).toBeInTheDocument();
    expect(screen.getAllByText("Activo").length).toBeGreaterThanOrEqual(1);
    expect(screen.getByRole("link", { name: /Volver al listado/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Editar usuario/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Eliminar cuenta/i })).toBeInTheDocument();
  });

  test("opens custom delete modal on clicking 'Eliminar cuenta' and cancels properly", async () => {
    const user = userEvent.setup();
    render(<UserDetailAdmin />);

    const deleteBtn = await screen.findByRole("button", { name: /Eliminar cuenta/i });
    await user.click(deleteBtn);

    // Modal is opened
    expect(screen.getByRole("heading", { name: "¿Eliminar usuario?" })).toBeInTheDocument();
    expect(screen.getByText(/¿Estás seguro de que quieres eliminar la cuenta de/i)).toBeInTheDocument();

    // Click cancel
    const cancelBtn = screen.getByRole("button", { name: "Cancelar" });
    await user.click(cancelBtn);

    expect(screen.queryByRole("heading", { name: "¿Eliminar usuario?" })).not.toBeInTheDocument();
    expect(usersApi.deleteUser).not.toHaveBeenCalled();
  });

  test("confirms deletion in modal, calls deleteUser, and shows success toast", async () => {
    const user = userEvent.setup();
    render(<UserDetailAdmin />);

    const deleteBtn = await screen.findByRole("button", { name: /Eliminar cuenta/i });
    await user.click(deleteBtn);

    const confirmBtn = screen.getByRole("button", { name: "Confirmar eliminación" });
    await user.click(confirmBtn);

    await waitFor(() => {
      expect(usersApi.deleteUser).toHaveBeenCalledWith(1);
    });

    expect(await screen.findByText("Usuario eliminado correctamente.")).toBeInTheDocument();
  });

  test("shows error modal if deletion fails", async () => {
    const user = userEvent.setup();
    usersApi.deleteUser.mockRejectedValueOnce({
      response: { data: { message: "No puedes eliminar este usuario" } },
    });

    render(<UserDetailAdmin />);

    const deleteBtn = await screen.findByRole("button", { name: /Eliminar cuenta/i });
    await user.click(deleteBtn);

    const confirmBtn = screen.getByRole("button", { name: "Confirmar eliminación" });
    await user.click(confirmBtn);

    expect(await screen.findByText("No puedes eliminar este usuario")).toBeInTheDocument();
  });
});
