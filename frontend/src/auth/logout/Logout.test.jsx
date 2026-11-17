import { beforeEach, describe, expect, test, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Logout from "./Logout";
import * as authApi from "../../services/auth";
import tokenService from "../../services/token.service";

const mockedNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useNavigate: () => mockedNavigate,
    };
});

vi.mock("../../services/auth", () => ({
    signout: vi.fn(),
}));

vi.mock("../../services/token.service", () => ({
    default: {
        removeUser: vi.fn(),
    },
}));

function renderLogout() {
    return render(
        <BrowserRouter>
            <Logout />
        </BrowserRouter>
    );
}

describe("Logout Component (HU-026)", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        authApi.signout.mockResolvedValue({ message: "Log out successful!" });

        delete window.location;
        window.location = { href: "" };
    });

    test("renders the modal elements matching the HU-026 style", () => {
        renderLogout();

        // Plaque and Titles
        expect(screen.getByRole("heading", { name: /^cerrar sesión$/i })).toBeInTheDocument();
        expect(screen.getByRole("heading", { name: /¿estás seguro de que quieres cerrar tu sesión\?/i })).toBeInTheDocument();
        expect(
            screen.getByText(/tras cerrar sesión, tendrás que iniciar sesión de nuevo para acceder a tu cuenta/i)
        ).toBeInTheDocument();

        // Actions
        expect(screen.getByRole("button", { name: /cancelar cierre de sesión/i })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /confirmar cierre de sesión/i })).toBeInTheDocument();
    });

    test("navigates back or to home when clicking Cancelar", () => {
        renderLogout();

        const cancelBtn = screen.getByRole("button", { name: /cancelar cierre de sesión/i });
        fireEvent.click(cancelBtn);

        expect(mockedNavigate).toHaveBeenCalled();
    });

    test("signs out user, clears token, fires auth-changed, and redirects to home on confirm", async () => {
        const dispatchSpy = vi.spyOn(window, "dispatchEvent");
        renderLogout();

        const confirmBtn = screen.getByRole("button", { name: /confirmar cierre de sesión/i });
        fireEvent.click(confirmBtn);

        await waitFor(() => {
            expect(authApi.signout).toHaveBeenCalled();
            expect(tokenService.removeUser).toHaveBeenCalled();
            expect(dispatchSpy).toHaveBeenCalledWith(expect.objectContaining({ type: "auth-changed" }));
            expect(window.location.href).toBe("/");
        });
    });

    test("cleans up session even if backend signout encounters an error", async () => {
        authApi.signout.mockRejectedValue(new Error("Network Error"));
        renderLogout();

        const confirmBtn = screen.getByRole("button", { name: /confirmar cierre de sesión/i });
        fireEvent.click(confirmBtn);

        await waitFor(() => {
            expect(tokenService.removeUser).toHaveBeenCalled();
            expect(window.location.href).toBe("/");
        });
    });
});
