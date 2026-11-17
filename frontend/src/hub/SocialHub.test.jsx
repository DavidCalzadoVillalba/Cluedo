import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, test, vi } from "vitest";
import SocialHub from "./SocialHub";
import useAuth from "../hooks/useAuth";

vi.mock("../hooks/useAuth", () => ({
    default: vi.fn(),
}));

vi.mock("../community/PlayerDirectory", () => ({
    default: () => <div data-testid="community-section">Sección de Usuarios</div>,
}));

vi.mock("../admin/games/ActiveGamesAdmin", () => ({
    default: () => <div data-testid="active-games-section">Sección de Lista de partidas</div>,
}));

vi.mock("../admin/users/UserListAdmin", () => ({
    default: () => <div data-testid="admin-users-section">Sección de Administración de usuarios</div>,
}));

describe("SocialHub", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        useAuth.mockReturnValue({
            jwt: "token",
            roles: ["ADMIN"],
            username: "admin",
        });
    });

    test("renders header, back link, all 3 tabs for admin, and switches between tabs", async () => {
        const user = userEvent.setup();
        render(
            <MemoryRouter initialEntries={["/social-hub"]}>
                <Routes>
                    <Route path="/social-hub" element={<SocialHub />} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByRole("link", { name: /Menú principal/i })).toHaveAttribute("href", "/");

        const usersTab = screen.getByRole("tab", { name: /^Usuarios$/i });
        const gamesTab = screen.getByRole("tab", { name: /^Lista de partidas$/i });
        const adminUsersTab = screen.getByRole("tab", { name: /^Administración de usuarios$/i });

        expect(usersTab).toHaveAttribute("aria-selected", "true");
        expect(gamesTab).toHaveAttribute("aria-selected", "false");
        expect(adminUsersTab).toHaveAttribute("aria-selected", "false");
        expect(screen.getByTestId("community-section")).toBeInTheDocument();
        expect(screen.queryByTestId("active-games-section")).not.toBeInTheDocument();
        expect(screen.queryByTestId("admin-users-section")).not.toBeInTheDocument();

        // Switch to games tab
        await user.click(gamesTab);
        expect(gamesTab).toHaveAttribute("aria-selected", "true");
        expect(screen.getByTestId("active-games-section")).toBeInTheDocument();
        expect(screen.queryByTestId("community-section")).not.toBeInTheDocument();

        // Switch to admin users tab
        await user.click(adminUsersTab);
        expect(adminUsersTab).toHaveAttribute("aria-selected", "true");
        expect(screen.getByTestId("admin-users-section")).toBeInTheDocument();
        expect(screen.queryByTestId("active-games-section")).not.toBeInTheDocument();
    });

    test("loads directly into admin-users tab when query param ?tab=admin-users is passed for admin", () => {
        render(
            <MemoryRouter initialEntries={["/social-hub?tab=admin-users"]}>
                <Routes>
                    <Route path="/social-hub" element={<SocialHub />} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByTestId("admin-users-section")).toBeInTheDocument();
    });

    test("for regular player: only renders users tab, hiding games and admin users tabs", () => {
        useAuth.mockReturnValue({
            jwt: "token",
            roles: ["PLAYER"],
            username: "player1",
        });

        render(
            <MemoryRouter initialEntries={["/social-hub?tab=admin-users"]}>
                <Routes>
                    <Route path="/social-hub" element={<SocialHub />} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByRole("tab", { name: /^Usuarios$/i })).toBeInTheDocument();
        expect(screen.queryByRole("tab", { name: /^Lista de partidas$/i })).not.toBeInTheDocument();
        expect(screen.queryByRole("tab", { name: /^Administración de usuarios$/i })).not.toBeInTheDocument();

        // Falls back to users section even if ?tab=admin-users was passed
        expect(screen.getByTestId("community-section")).toBeInTheDocument();
        expect(screen.queryByTestId("admin-users-section")).not.toBeInTheDocument();
        expect(screen.queryByTestId("active-games-section")).not.toBeInTheDocument();
    });
});
