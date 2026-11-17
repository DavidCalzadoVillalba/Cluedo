import { fireEvent, screen, waitFor } from "@testing-library/react";
import { render } from "../../test-utils";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, test, vi } from "vitest";
import * as usersApi from "../../services/users";
import UserEditAdmin from "./UserEditAdmin";

const navigate = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigate,
    useParams: () => ({ id: "2" }),
  };
});

vi.mock("../../services/users", () => ({
  getUserById: vi.fn(),
  getUserAuthorities: vi.fn(),
  updateUser: vi.fn(),
}));

const mockUser = {
  id: 2,
  username: "player1",
  email: "player1@cluedo.test",
  accountStatus: "ACTIVE",
  avatar: "https://example.com/avatar.png",
  authority: { id: 2, authority: "PLAYER" },
};

const mockAuthorities = [
  { id: 1, authority: "ADMIN" },
  { id: 2, authority: "PLAYER" },
];

describe("UserEditAdmin Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    usersApi.getUserById.mockResolvedValue(mockUser);
    usersApi.getUserAuthorities.mockResolvedValue(mockAuthorities);
    usersApi.updateUser.mockResolvedValue({ ...mockUser, username: "player1-edited" });
  });

  test("renders form with loaded user data", async () => {
    render(<UserEditAdmin />);

    expect(await screen.findByDisplayValue("player1")).toBeInTheDocument();
    expect(screen.getByLabelText(/Nombre de usuario/i)).toHaveValue("player1");
    expect(screen.getByLabelText(/Correo electrónico/i)).toHaveValue("player1@cluedo.test");
    expect(screen.getByLabelText(/Rol \/ Autoridad/i)).toHaveValue("PLAYER");
    expect(screen.getByLabelText(/Estado de la cuenta/i)).toHaveValue("ACTIVE");
    expect(screen.getByLabelText(/Avatar/i)).toHaveValue("https://example.com/avatar.png");
  });

  test("allows modifying fields and submits updated user data", async () => {
    const user = userEvent.setup();
    render(<UserEditAdmin />);

    const usernameInput = await screen.findByDisplayValue("player1");
    const emailInput = screen.getByLabelText(/Correo electrónico/i);
    const roleSelect = screen.getByLabelText(/Rol \/ Autoridad/i);
    const statusSelect = screen.getByLabelText(/Estado de la cuenta/i);
    const saveButton = screen.getByRole("button", { name: /Guardar cambios/i });

    await user.clear(usernameInput);
    await user.type(usernameInput, "player1-mod");

    await user.clear(emailInput);
    await user.type(emailInput, "mod@cluedo.test");

    await user.selectOptions(roleSelect, "ADMIN");
    await user.selectOptions(statusSelect, "SUSPENDED");

    await user.click(saveButton);

    await waitFor(() => {
      expect(usersApi.updateUser).toHaveBeenCalledWith("2", {
        id: 2,
        username: "player1-mod",
        email: "mod@cluedo.test",
        authority: { id: 1, authority: "ADMIN" },
        accountStatus: "SUSPENDED",
        avatar: "https://example.com/avatar.png",
      });
    });

    expect(
      await screen.findByText(/Los cambios se han guardado correctamente/i)
    ).toBeInTheDocument();
  });

  test("displays error modal and preserves form input when update fails", async () => {
    const user = userEvent.setup();
    usersApi.updateUser.mockRejectedValueOnce({
      response: {
        data: {
          message: "Ese nombre de usuario ya está registrado",
        },
      },
    });

    render(<UserEditAdmin />);

    const usernameInput = await screen.findByDisplayValue("player1");
    await user.clear(usernameInput);
    await user.type(usernameInput, "duplicateName");

    const saveButton = screen.getByRole("button", { name: /Guardar cambios/i });
    await user.click(saveButton);

    expect(
      await screen.findByText("Ese nombre de usuario ya está registrado")
    ).toBeInTheDocument();

    // The user input should be preserved in the form
    expect(usernameInput).toHaveValue("duplicateName");
  });
});
