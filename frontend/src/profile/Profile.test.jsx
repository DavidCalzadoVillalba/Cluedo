import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, test, vi } from "vitest";
import { getMyProfile, updateMyProfile } from "../services/profile";
import Profile from "./Profile";

const navigate = vi.fn();

vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return { ...actual, useNavigate: () => navigate };
});

vi.mock("../services/profile", () => ({
    getMyProfile: vi.fn(),
    updateMyProfile: vi.fn(),
}));

const savedProfile = {
    id: 4,
    username: "player1",
    email: "player1@cluedo.test",
    avatar: "/images/personajes/Rubio.png",
};

describe("Profile", () => {
    beforeEach(() => {
        navigate.mockReset();
        getMyProfile.mockReset();
        updateMyProfile.mockReset();
        getMyProfile.mockResolvedValue(savedProfile);
    });

    test("shows the authenticated player's username, email and avatar", async () => {
        render(<Profile />);

        expect(await screen.findByDisplayValue("player1")).toBeInTheDocument();
        expect(screen.getByLabelText(/Nombre de usuario/)).toHaveValue("player1");
        expect(screen.getByLabelText(/Correo electrónico/)).toHaveValue("player1@cluedo.test");
        expect(screen.getByAltText("Avatar actual")).toHaveAttribute("src", "/images/personajes/Rubio.png");
    });

    test("saves the three editable fields", async () => {
        updateMyProfile.mockResolvedValue({
            ...savedProfile,
            username: "nuevo",
            email: "nuevo@cluedo.test",
            avatar: "/images/personajes/Amapola.png",
            token: "token-nuevo",
        });
        render(<Profile />);
        await screen.findByDisplayValue("player1");

        fireEvent.change(screen.getByLabelText(/Nombre de usuario/), { target: { value: "nuevo" } });
        fireEvent.change(screen.getByLabelText(/Correo electrónico/), { target: { value: "nuevo@cluedo.test" } });
        fireEvent.click(screen.getByRole("button", { name: /Cambiar avatar/ }));
        fireEvent.click(screen.getByRole("button", { name: "Seleccionar avatar de Amapola" }));
        fireEvent.click(screen.getByRole("button", { name: /Guardar cambios/ }));

        await waitFor(() => {
            expect(updateMyProfile).toHaveBeenCalledWith({
                username: "nuevo",
                email: "nuevo@cluedo.test",
                avatar: "/images/personajes/Amapola.png",
            });
        });
        expect(await screen.findByRole("status")).toHaveTextContent("Perfil actualizado correctamente.");
        expect(screen.getByLabelText(/Nombre de usuario/)).toHaveValue("nuevo");
    });

    test("shows the conflict and keeps the previous profile", async () => {
        updateMyProfile.mockRejectedValue({
            response: { data: { message: "Ese nombre de usuario ya está registrado" } },
        });
        render(<Profile />);
        await screen.findByDisplayValue("player1");

        fireEvent.change(screen.getByLabelText(/Nombre de usuario/), { target: { value: "player2" } });
        fireEvent.change(screen.getByLabelText(/Correo electrónico/), { target: { value: "otro@cluedo.test" } });
        fireEvent.click(screen.getByRole("button", { name: /Guardar cambios/ }));

        expect(await screen.findByRole("alert")).toHaveTextContent("Ese nombre de usuario ya está registrado");
        expect(screen.getByLabelText(/Nombre de usuario/)).toHaveValue("player1");
        expect(screen.getByLabelText(/Correo electrónico/)).toHaveValue("player1@cluedo.test");
        expect(screen.getByAltText("Avatar actual")).toHaveAttribute("src", "/images/personajes/Rubio.png");
    });

    test("does not call the API when the username is empty", async () => {
        render(<Profile />);
        await screen.findByDisplayValue("player1");

        fireEvent.change(screen.getByLabelText(/Nombre de usuario/), { target: { value: " " } });
        fireEvent.click(screen.getByRole("button", { name: /Guardar cambios/ }));

        expect(await screen.findByRole("alert")).toHaveTextContent("El nombre de usuario es obligatorio");
        expect(updateMyProfile).not.toHaveBeenCalled();
    });

    test("returns to the main menu without saving", async () => {
        render(<Profile />);
        await screen.findByDisplayValue("player1");

        fireEvent.click(screen.getByRole("button", { name: /Cancelar/ }));

        expect(navigate).toHaveBeenCalledWith("/");
        expect(updateMyProfile).not.toHaveBeenCalled();
    });

    test("navigates to logout when clicking the logout button in profile section", async () => {
        render(<Profile />);
        await screen.findByDisplayValue("player1");

        const logoutBtn = screen.getByRole("button", { name: /Cerrar sesión/i });
        expect(logoutBtn).toBeInTheDocument();
        fireEvent.click(logoutBtn);

        expect(navigate).toHaveBeenCalledWith("/logout");
    });
});
