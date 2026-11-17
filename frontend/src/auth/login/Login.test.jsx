import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Login from "./Login";
import * as authApi from "../../services/auth";
import tokenService from "../../services/token.service";

vi.mock("../../services/auth", () => ({
    signin: vi.fn(),
}));

vi.mock("../../services/token.service", () => ({
    default: {
        setUser: vi.fn(),
        updateLocalAccessToken: vi.fn(),
    },
}));

describe("Login Component (HU-025)", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        delete window.location;
        window.location = { href: "" };
    });

    function renderLogin() {
        return render(
            <BrowserRouter>
                <Login />
            </BrowserRouter>
        );
    }

    it("renders the login form elements matching the HU-025 mockup", () => {
        renderLogin();

        expect(screen.getByRole("heading", { name: /inicio de sesión/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/usuario o correo electrónico/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/^contraseña/i)).toBeInTheDocument();
        expect(
            screen.getByRole("button", { name: /iniciar sesión/i })
        ).toBeInTheDocument();
        expect(
            screen.getByRole("link", { name: /¿no tienes cuenta\? regístrate/i })
        ).toHaveAttribute("href", "/register");
    });

    it("shows error when submitting without username/email", async () => {
        renderLogin();

        fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

        expect(
            await screen.findByText(/por favor, introduce tu usuario o correo electrónico/i)
        ).toBeInTheDocument();
        expect(authApi.signin).not.toHaveBeenCalled();
    });

    it("shows error when submitting with username but empty password", async () => {
        renderLogin();

        fireEvent.change(screen.getByLabelText(/usuario o correo electrónico/i), {
            target: { value: "detective1" },
        });
        fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

        expect(
            await screen.findByText(/por favor, introduce tu contraseña/i)
        ).toBeInTheDocument();
        expect(authApi.signin).not.toHaveBeenCalled();
    });

    it("toggles password visibility when clicking the eye button", () => {
        renderLogin();

        const passwordInput = screen.getByPlaceholderText(/introduce tu contraseña/i);
        expect(passwordInput).toHaveAttribute("type", "password");

        const toggleBtn = screen.getByRole("button", { name: /ver contraseña/i });
        fireEvent.click(toggleBtn);

        expect(passwordInput).toHaveAttribute("type", "text");
        expect(screen.getByRole("button", { name: /ocultar contraseña/i })).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: /ocultar contraseña/i }));
        expect(passwordInput).toHaveAttribute("type", "password");
    });

    it("authenticates successfully with username and redirects to home", async () => {
        authApi.signin.mockResolvedValueOnce({
            id: 1,
            username: "detective1",
            token: "fake-jwt-token",
            roles: ["PLAYER"],
        });

        renderLogin();

        fireEvent.change(screen.getByLabelText(/usuario o correo electrónico/i), {
            target: { value: "detective1" },
        });
        fireEvent.change(screen.getByPlaceholderText(/introduce tu contraseña/i), {
            target: { value: "secret123" },
        });

        fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

        await waitFor(() => {
            expect(authApi.signin).toHaveBeenCalledWith({
                username: "detective1",
                password: "secret123",
            });
            expect(tokenService.setUser).toHaveBeenCalledWith({
                id: 1,
                username: "detective1",
                token: "fake-jwt-token",
                roles: ["PLAYER"],
            });
            expect(tokenService.updateLocalAccessToken).toHaveBeenCalledWith("fake-jwt-token");
            expect(window.location.href).toBe("/");
        });
    });

    it("authenticates successfully with email and redirects to home", async () => {
        authApi.signin.mockResolvedValueOnce({
            id: 2,
            username: "detective2",
            token: "another-token",
            roles: ["PLAYER"],
        });

        renderLogin();

        fireEvent.change(screen.getByLabelText(/usuario o correo electrónico/i), {
            target: { value: "detective2@cluedo.test" },
        });
        fireEvent.change(screen.getByPlaceholderText(/introduce tu contraseña/i), {
            target: { value: "password456" },
        });

        fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

        await waitFor(() => {
            expect(authApi.signin).toHaveBeenCalledWith({
                username: "detective2@cluedo.test",
                password: "password456",
            });
            expect(tokenService.setUser).toHaveBeenCalled();
            expect(window.location.href).toBe("/");
        });
    });

    it("displays error banner when signin fails with bad credentials", async () => {
        authApi.signin.mockRejectedValueOnce({
            response: {
                status: 400,
                data: "Bad Credentials!",
            },
        });

        renderLogin();

        fireEvent.change(screen.getByLabelText(/usuario o correo electrónico/i), {
            target: { value: "wrongUser" },
        });
        fireEvent.change(screen.getByPlaceholderText(/introduce tu contraseña/i), {
            target: { value: "wrongPass" },
        });

        fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

        expect(
            await screen.findByText(/usuario, correo electrónico o contraseña incorrectos/i)
        ).toBeInTheDocument();
    });

    it("displays custom error message from server when available", async () => {
        authApi.signin.mockRejectedValueOnce({
            response: {
                status: 403,
                data: { message: "Tu cuenta ha sido suspendida temporalmente." },
            },
        });

        renderLogin();

        fireEvent.change(screen.getByLabelText(/usuario o correo electrónico/i), {
            target: { value: "bannedUser" },
        });
        fireEvent.change(screen.getByPlaceholderText(/introduce tu contraseña/i), {
            target: { value: "anyPass" },
        });

        fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

        expect(
            await screen.findByText(/tu cuenta ha sido suspendida temporalmente/i)
        ).toBeInTheDocument();
    });
});
