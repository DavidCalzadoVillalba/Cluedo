import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { beforeEach, describe, expect, test, vi } from "vitest";
import Home from "./index";
import useAuth from "../hooks/useAuth";

vi.mock("../hooks/useAuth", () => ({
    default: vi.fn(),
}));

describe("Home", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    test("when user is not authenticated, only displays login button and register link", () => {
        useAuth.mockReturnValue({ jwt: null, roles: [], username: null });

        render(
            <BrowserRouter>
                <Home />
            </BrowserRouter>
        );

        expect(screen.getByRole("button", { name: /Iniciar sesión/i })).toBeInTheDocument();
        expect(screen.getByRole("link", { name: /Regístrate aquí/i })).toHaveAttribute("href", "/register");

        expect(screen.queryByRole("button", { name: /^Crear partida$/i })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: /Unirse/i })).not.toBeInTheDocument();
        expect(screen.queryByRole("link", { name: /Gestionar perfil/i })).not.toBeInTheDocument();
        expect(screen.queryByRole("link", { name: /Menú social/i })).not.toBeInTheDocument();
    });

    test("when user is authenticated, displays two-column card and bottom capsule nav", () => {
        useAuth.mockReturnValue({ jwt: "test-token", roles: ["PLAYER"], username: "player1" });

        render(
            <BrowserRouter>
                <Home />
            </BrowserRouter>
        );

        expect(screen.getByRole("heading", { name: /Crear partida/i })).toBeInTheDocument();
        expect(screen.getByRole("heading", { name: /Unirse a partida/i })).toBeInTheDocument();

        const createGameBtn = screen.getByRole("button", { name: /^Crear partida$/i });
        expect(createGameBtn).toBeInTheDocument();
        expect(createGameBtn.closest("a")).toHaveAttribute("href", "/create-game");

        const joinGameBtn = screen.getByRole("button", { name: /^Unirse/i });
        expect(joinGameBtn).toBeInTheDocument();
        expect(joinGameBtn.closest("a")).toHaveAttribute("href", "/join-game");

        const profileCapsule = screen.getByRole("link", { name: /Gestionar perfil/i });
        expect(profileCapsule).toBeInTheDocument();
        expect(profileCapsule).toHaveAttribute("href", "/user-hub");

        const socialCapsule = screen.getByRole("link", { name: /Menú social/i });
        expect(socialCapsule).toBeInTheDocument();
        expect(socialCapsule).toHaveAttribute("href", "/social-hub");

        expect(screen.queryByRole("button", { name: /Iniciar sesión/i })).not.toBeInTheDocument();
    });
});
