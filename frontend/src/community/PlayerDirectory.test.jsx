import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, test, vi } from "vitest";
import PlayerDirectory from "./PlayerDirectory";
import * as communityApi from "../services/community";

vi.mock("../services/community", () => ({
    getCommunityUsers: vi.fn(),
}));

describe("PlayerDirectory", () => {
    const mockUsersPage0 = {
        content: [
            { id: 1, username: "DavidCalzado", avatar: "/images/personajes/Amapola.png" },
            { id: 2, username: "LuciaGomez", avatar: null },
            { id: 3, username: "AlvaroRamos", avatar: null },
            { id: 4, username: "MarioPerez", avatar: null },
            { id: 5, username: "SaraFernandez", avatar: null },
            { id: 6, username: "JavierLopez", avatar: null },
        ],
        totalPages: 2,
        totalElements: 8,
        number: 0,
        size: 6,
    };

    const mockUsersPage1 = {
        content: [
            { id: 7, username: "ElenaCastro", avatar: null },
            { id: 8, username: "CarlosRuiz", avatar: null },
        ],
        totalPages: 2,
        totalElements: 8,
        number: 1,
        size: 6,
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    test("renders header, search bar, and displays list of users with avatars", async () => {
        communityApi.getCommunityUsers.mockResolvedValueOnce(mockUsersPage0);

        render(<PlayerDirectory />);

        expect(screen.getByText("Cargando usuarios…")).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText("DavidCalzado")).toBeInTheDocument();
        });

        expect(screen.getByRole("heading", { name: /Usuarios registrados/i })).toBeInTheDocument();
        expect(screen.getByPlaceholderText("Buscar por nombre de usuario...")).toBeInTheDocument();
        expect(screen.getByText("LuciaGomez")).toBeInTheDocument();
        expect(screen.getByText("JavierLopez")).toBeInTheDocument();

        const avatarImg = screen.getByAltText("Avatar de DavidCalzado");
        expect(avatarImg).toBeInTheDocument();
        expect(avatarImg).toHaveAttribute("src", "/images/personajes/Amapola.png");

        expect(screen.getByText("1 / 2")).toBeInTheDocument();
    });

    test("handles search submission and queries backend with search term", async () => {
        communityApi.getCommunityUsers
            .mockResolvedValueOnce(mockUsersPage0)
            .mockResolvedValueOnce({
                content: [{ id: 1, username: "DavidCalzado", avatar: null }],
                totalPages: 1,
                totalElements: 1,
                number: 0,
                size: 6,
            });

        const user = userEvent.setup();
        render(<PlayerDirectory />);

        await waitFor(() => {
            expect(screen.getByText("DavidCalzado")).toBeInTheDocument();
        });

        const searchInput = screen.getByPlaceholderText("Buscar por nombre de usuario...");
        await user.type(searchInput, "David");

        const searchBtn = screen.getByRole("button", { name: "Buscar" });
        await user.click(searchBtn);

        await waitFor(() => {
            expect(communityApi.getCommunityUsers).toHaveBeenLastCalledWith({
                page: 0,
                size: 6,
                search: "David",
            });
        });

        expect(screen.getByText("DavidCalzado")).toBeInTheDocument();
        expect(screen.queryByText("LuciaGomez")).not.toBeInTheDocument();
    });

    test("allows clearing search input", async () => {
        communityApi.getCommunityUsers.mockResolvedValue(mockUsersPage0);

        const user = userEvent.setup();
        render(<PlayerDirectory />);

        await waitFor(() => {
            expect(screen.getByText("DavidCalzado")).toBeInTheDocument();
        });

        const searchInput = screen.getByPlaceholderText("Buscar por nombre de usuario...");
        await user.type(searchInput, "David");

        const clearBtn = screen.getByRole("button", { name: "Limpiar búsqueda" });
        await user.click(clearBtn);

        expect(searchInput).toHaveValue("");
    });

    test("handles pagination next and previous controls", async () => {
        communityApi.getCommunityUsers
            .mockResolvedValueOnce(mockUsersPage0)
            .mockResolvedValueOnce(mockUsersPage1);

        const user = userEvent.setup();
        render(<PlayerDirectory />);

        await waitFor(() => {
            expect(screen.getByText("DavidCalzado")).toBeInTheDocument();
        });

        const nextBtn = screen.getByRole("button", { name: "Página siguiente" });
        expect(nextBtn).toBeEnabled();

        const prevBtn = screen.getByRole("button", { name: "Página anterior" });
        expect(prevBtn).toBeDisabled();

        await user.click(nextBtn);

        await waitFor(() => {
            expect(communityApi.getCommunityUsers).toHaveBeenLastCalledWith({
                page: 1,
                size: 6,
                search: "",
            });
        });

        await waitFor(() => {
            expect(screen.getByText("ElenaCastro")).toBeInTheDocument();
        });
        expect(screen.getByText("2 / 2")).toBeInTheDocument();
        expect(nextBtn).toBeDisabled();
        expect(prevBtn).toBeEnabled();
    });

    test("shows empty message when no users match search", async () => {
        communityApi.getCommunityUsers.mockResolvedValueOnce({
            content: [],
            totalPages: 1,
            totalElements: 0,
            number: 0,
            size: 6,
        });

        render(<PlayerDirectory />);

        await waitFor(() => {
            expect(screen.getByText("No hay usuarios registrados disponibles.")).toBeInTheDocument();
        });
    });

    test("shows error message when request fails", async () => {
        communityApi.getCommunityUsers.mockRejectedValueOnce(new Error("Network Error"));

        render(<PlayerDirectory />);

        await waitFor(() => {
            expect(
                screen.getByText("No se ha podido cargar el listado de usuarios. Inténtalo de nuevo más tarde.")
            ).toBeInTheDocument();
        });
    });
});
