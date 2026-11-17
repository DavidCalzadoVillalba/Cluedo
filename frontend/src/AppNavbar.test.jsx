import AppNavbar from "./AppNavbar";
import { act, render, screen } from "./test-utils";
import useAuth from "./hooks/useAuth";

vi.mock("./hooks/useAuth", () => ({
    default: vi.fn(),
}));

describe('AppNavbar', () => {

    beforeEach(() => {
        useAuth.mockReturnValue({ jwt: null, roles: [], username: null });
    });

    test('renders public links correctly', () => {
        render(<AppNavbar />);
        const linkDocsElement = screen.getByRole('link', { name: 'Docs' });
        expect(linkDocsElement).toBeInTheDocument();

        const linkPlansElement = screen.getByRole('link', { name: 'Pricing Plans' });
        expect(linkPlansElement).toBeInTheDocument();

        const linkHomeElement = screen.getByRole('link', { name: /Your Game/ });
        expect(linkHomeElement).toBeInTheDocument();
        expect(screen.queryByRole('link', { name: 'Perfil' })).not.toBeInTheDocument();
    });

    test('renders not user links correctly', () => {
        render(<AppNavbar />);
        const linkDocsElement = screen.getByRole('link', { name: 'Register' });
        expect(linkDocsElement).toBeInTheDocument();

        const linkPlansElement = screen.getByRole('link', { name: 'Login' });
        expect(linkPlansElement).toBeInTheDocument();
    });

    test('renders authenticated and administrator links correctly', () => {
        useAuth.mockReturnValue({
            jwt: 'token',
            roles: ['ADMIN'],
            username: 'admin',
        });

        render(<AppNavbar />);

        expect(screen.getByRole('link', { name: 'Dashboard' })).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Lista de Partidas' })).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Usuarios' })).toHaveAttribute('href', '/community');
        expect(screen.getByRole('link', { name: 'Administración de usuarios' })).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Logout' })).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Perfil' })).toHaveAttribute('href', '/profile');
        expect(screen.getByText('admin')).toBeInTheDocument();
        expect(screen.queryByRole('link', { name: 'Login' })).not.toBeInTheDocument();
    });

    test('refreshes the signed-in username when the profile session changes', async () => {
        useAuth.mockReturnValue({
            jwt: 'token',
            roles: ['PLAYER'],
            username: 'player1',
        });
        render(<AppNavbar />);
        expect(screen.getByText('player1')).toBeInTheDocument();

        useAuth.mockReturnValue({
            jwt: 'token',
            roles: ['PLAYER'],
            username: 'nuevo',
        });
        await act(async () => {
            window.dispatchEvent(new Event('auth-changed'));
        });

        expect(screen.getByText('nuevo')).toBeInTheDocument();
    });

});
