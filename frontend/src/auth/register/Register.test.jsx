import { describe, test, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Register from "./Register";
import * as authApi from "../../services/auth";
import tokenService from "../../services/token.service";

vi.mock("../../services/auth", () => ({
    signup: vi.fn(),
    signin: vi.fn(),
}));

vi.mock("../../services/token.service", () => ({
    default: {
        setUser: vi.fn(),
        updateLocalAccessToken: vi.fn(),
    },
}));

describe("Register Component (HU-024)", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    test("renders the registration form elements and default avatar", () => {
        render(
            <BrowserRouter>
                <Register />
            </BrowserRouter>
        );

        expect(screen.getByText("Registro de usuario")).toBeInTheDocument();
        expect(screen.getByLabelText(/Nombre de usuario/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Correo electrónico/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/^Contraseña \*/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Confirmar contraseña/i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Registrarse/i })).toBeInTheDocument();
        expect(screen.getByRole("link", { name: /Volver al inicio de sesión/i })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Cambiar avatar/i })).toBeInTheDocument();
    });

    test("shows validation error if required fields are missing", async () => {
        render(
            <BrowserRouter>
                <Register />
            </BrowserRouter>
        );

        fireEvent.click(screen.getByRole("button", { name: /Registrarse/i }));

        expect(await screen.findByText(/El nombre de usuario es obligatorio/i)).toBeInTheDocument();
        expect(authApi.signup).not.toHaveBeenCalled();
    });

    test("shows validation error if email format is invalid", async () => {
        render(
            <BrowserRouter>
                <Register />
            </BrowserRouter>
        );

        fireEvent.change(screen.getByLabelText(/Nombre de usuario/i), {
            target: { value: "detective1" },
        });
        fireEvent.change(screen.getByLabelText(/Correo electrónico/i), {
            target: { value: "correo-invalido" },
        });
        fireEvent.change(screen.getByLabelText(/^Contraseña \*/i), {
            target: { value: "secret123" },
        });
        fireEvent.change(screen.getByLabelText(/Confirmar contraseña/i), {
            target: { value: "secret123" },
        });

        fireEvent.click(screen.getByRole("button", { name: /Registrarse/i }));

        expect(await screen.findByText(/introduce un correo electrónico válido/i)).toBeInTheDocument();
        expect(authApi.signup).not.toHaveBeenCalled();
    });

    test("shows validation error if passwords do not match", async () => {
        render(
            <BrowserRouter>
                <Register />
            </BrowserRouter>
        );

        fireEvent.change(screen.getByLabelText(/Nombre de usuario/i), {
            target: { value: "detective1" },
        });
        fireEvent.change(screen.getByLabelText(/Correo electrónico/i), {
            target: { value: "detective@cluedo.test" },
        });
        fireEvent.change(screen.getByLabelText(/^Contraseña \*/i), {
            target: { value: "secret123" },
        });
        fireEvent.change(screen.getByLabelText(/Confirmar contraseña/i), {
            target: { value: "distinto123" },
        });

        fireEvent.click(screen.getByRole("button", { name: /Registrarse/i }));

        expect(await screen.findByText(/Las contraseñas no coinciden/i)).toBeInTheDocument();
        expect(authApi.signup).not.toHaveBeenCalled();
    });

    test("allows selecting a character avatar", () => {
        render(
            <BrowserRouter>
                <Register />
            </BrowserRouter>
        );

        fireEvent.click(screen.getByRole("button", { name: /Cambiar avatar/i }));

        const amapolaBtn = screen.getByRole("button", { name: /Seleccionar avatar de Amapola/i });
        expect(amapolaBtn).toBeInTheDocument();

        fireEvent.click(amapolaBtn);

        // After selection, the choices grid closes and the preview has updated
        expect(screen.queryByRole("group", { name: /Selección de avatar/i })).not.toBeInTheDocument();
    });

    test("toggles password visibility with eye buttons", () => {
        render(
            <BrowserRouter>
                <Register />
            </BrowserRouter>
        );

        const passwordInput = screen.getByLabelText(/^Contraseña \*/i);
        expect(passwordInput).toHaveAttribute("type", "password");

        const eyeToggle = screen.getByLabelText(/Ver contraseña/i);
        fireEvent.click(eyeToggle);
        expect(passwordInput).toHaveAttribute("type", "text");

        fireEvent.click(screen.getByLabelText(/Ocultar contraseña/i));
        expect(passwordInput).toHaveAttribute("type", "password");
    });

    test("submits valid registration and auto-logs in with tokenService", async () => {
        authApi.signup.mockResolvedValueOnce({ message: "User registered successfully!" });
        authApi.signin.mockResolvedValueOnce({
            token: "fake-jwt-token",
            username: "detective1",
            roles: ["PLAYER"],
        });

        const originalLocation = window.location;
        delete window.location;
        window.location = { href: "" };

        render(
            <BrowserRouter>
                <Register />
            </BrowserRouter>
        );

        fireEvent.change(screen.getByLabelText(/Nombre de usuario/i), {
            target: { value: "detective1" },
        });
        fireEvent.change(screen.getByLabelText(/Correo electrónico/i), {
            target: { value: "detective1@cluedo.test" },
        });
        fireEvent.change(screen.getByLabelText(/^Contraseña \*/i), {
            target: { value: "password123" },
        });
        fireEvent.change(screen.getByLabelText(/Confirmar contraseña/i), {
            target: { value: "password123" },
        });

        fireEvent.click(screen.getByRole("button", { name: /Registrarse/i }));

        await waitFor(() => {
            expect(authApi.signup).toHaveBeenCalledWith(
                expect.objectContaining({
                    username: "detective1",
                    email: "detective1@cluedo.test",
                    password: "password123",
                    authority: "PLAYER",
                })
            );
            expect(authApi.signin).toHaveBeenCalledWith({
                username: "detective1",
                password: "password123",
            });
            expect(tokenService.setUser).toHaveBeenCalled();
            expect(tokenService.updateLocalAccessToken).toHaveBeenCalledWith("fake-jwt-token");
            expect(window.location.href).toBe("/");
        });

        window.location = originalLocation;
    });

    test("displays server error message when registration fails", async () => {
        authApi.signup.mockRejectedValueOnce({
            response: {
                data: {
                    message: "Error: Username is already taken!",
                },
            },
        });

        render(
            <BrowserRouter>
                <Register />
            </BrowserRouter>
        );

        fireEvent.change(screen.getByLabelText(/Nombre de usuario/i), {
            target: { value: "usuarioExistente" },
        });
        fireEvent.change(screen.getByLabelText(/Correo electrónico/i), {
            target: { value: "nuevo@cluedo.test" },
        });
        fireEvent.change(screen.getByLabelText(/^Contraseña \*/i), {
            target: { value: "password123" },
        });
        fireEvent.change(screen.getByLabelText(/Confirmar contraseña/i), {
            target: { value: "password123" },
        });

        fireEvent.click(screen.getByRole("button", { name: /Registrarse/i }));

        expect(
            await screen.findByText(/El nombre de usuario ya está registrado por otro jugador/i)
        ).toBeInTheDocument();
    });
});
