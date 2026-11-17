import { render, screen } from "../../test-utils";
import userEvent from "@testing-library/user-event";
import UserListAdmin from "./UserListAdmin";

describe("UserListAdmin", () => {
    test("renders correctly", async () => {
        render(<UserListAdmin />);
        expect(screen.getByRole("heading", { name: /Usuarios/i })).toBeInTheDocument();
        expect(screen.getByRole("table", { name: "users" })).toBeInTheDocument();
    });

    test("renders users correctly", async () => {
        render(<UserListAdmin />);

        const owner1 = await screen.findByRole("cell", { name: "owner1" });
        expect(owner1).toBeInTheDocument();

        const admin1 = await screen.findByRole("cell", { name: "admin1" });
        expect(admin1).toBeInTheDocument();

        const detailButtons = await screen.findAllByRole("link", { name: /^view-/i });
        expect(detailButtons.length).toBeGreaterThanOrEqual(2);

        const editButtons = await screen.findAllByRole("link", { name: /^edit-/i });
        expect(editButtons.length).toBeGreaterThanOrEqual(2);

        const rows = await screen.findAllByRole("row");
        expect(rows.length).toBeGreaterThanOrEqual(3);
    });

    test("filters users by search input", async () => {
        const user = userEvent.setup();
        render(<UserListAdmin />);

        await screen.findByRole("cell", { name: "owner1" });

        const searchInput = screen.getByRole("searchbox", { name: /Buscar usuarios/i });
        await user.type(searchInput, "admin1");

        expect(screen.getByRole("cell", { name: "admin1" })).toBeInTheDocument();
        expect(screen.queryByRole("cell", { name: "owner1" })).not.toBeInTheDocument();
    });
});
