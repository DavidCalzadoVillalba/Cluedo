import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, test, vi } from "vitest";
import UserHub from "./UserHub";

vi.mock("../profile/Profile", () => ({
    default: () => <div data-testid="profile-section">Sección de Perfil</div>,
}));

vi.mock("../game/GameHistory", () => ({
    default: () => <div data-testid="history-section">Sección de Historial de partidas</div>,
}));

describe("UserHub", () => {
    test("renders header, back link, profile tab and switches to history tab on click", async () => {
        const user = userEvent.setup();
        render(
            <MemoryRouter initialEntries={["/user-hub"]}>
                <Routes>
                    <Route path="/user-hub" element={<UserHub />} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByRole("link", { name: /Menú principal/i })).toHaveAttribute("href", "/");

        const profileTab = screen.getByRole("tab", { name: /Perfil/i });
        const historyTab = screen.getByRole("tab", { name: /Historial de partidas/i });

        expect(profileTab).toHaveAttribute("aria-selected", "true");
        expect(historyTab).toHaveAttribute("aria-selected", "false");
        expect(screen.getByTestId("profile-section")).toBeInTheDocument();
        expect(screen.queryByTestId("history-section")).not.toBeInTheDocument();

        await user.click(historyTab);

        expect(profileTab).toHaveAttribute("aria-selected", "false");
        expect(historyTab).toHaveAttribute("aria-selected", "true");
        expect(screen.getByTestId("history-section")).toBeInTheDocument();
        expect(screen.queryByTestId("profile-section")).not.toBeInTheDocument();
    });

    test("loads directly into history tab when query param ?tab=history is passed", () => {
        render(
            <MemoryRouter initialEntries={["/user-hub?tab=history"]}>
                <Routes>
                    <Route path="/user-hub" element={<UserHub />} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByTestId("history-section")).toBeInTheDocument();
    });
});
