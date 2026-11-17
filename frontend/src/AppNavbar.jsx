import { useEffect, useState } from 'react';
import { Navbar, NavbarBrand, NavLink, NavItem, Nav, NavbarText, NavbarToggler, Collapse } from 'reactstrap';
import { Link, useLocation } from 'react-router-dom';
import { FaCog, FaChevronDown, FaUserCircle } from 'react-icons/fa';
import useAuth from './hooks/useAuth';
import logoCluedo from './static/images/Cluedo_logo.png';

function AppNavbar() {
    const location = useLocation();
    const { jwt, roles, username } = useAuth();
    const [collapsed, setCollapsed] = useState(true);
    const [sessionStamp, setSessionStamp] = useState(0);

    useEffect(() => {
        const refreshSession = () => setSessionStamp((stamp) => stamp + 1);
        window.addEventListener('auth-changed', refreshSession);
        return () => window.removeEventListener('auth-changed', refreshSession);
    }, []);

    if (location.pathname.startsWith('/game/play') || location.pathname === '/board') {
        return null;
    }

    const toggleNavbar = () => setCollapsed(!collapsed);

    let adminLinks = <></>;
    let ownerLinks = <></>;
    let userLinks = <></>;
    let userLogout = <></>;
    let publicLinks = <></>;

    roles.forEach((role) => {
        if (role === "ADMIN") {
            adminLinks = (
                <>
                    <NavItem>
                        <NavLink className="nav-link-white" tag={Link} to="/users">Administración de usuarios</NavLink>
                    </NavItem>
                </>
            )
        }
    })

    if (!jwt) {
        publicLinks = (
            <>
                <NavItem>
                    <NavLink className="nav-link-white" id="docs" tag={Link} to="/docs">Docs</NavLink>
                </NavItem>
                <NavItem>
                    <NavLink className="nav-link-white" id="plans" tag={Link} to="/plans">Pricing Plans</NavLink>
                </NavItem>
                <NavItem>
                    <NavLink className="nav-link-white" id="register" tag={Link} to="/register">Register</NavLink>
                </NavItem>
                <NavItem>
                    <NavLink className="nav-link-white" id="login" tag={Link} to="/login">Login</NavLink>
                </NavItem>
            </>
        )
    } else {
        userLinks = (
            <>
                <NavItem>
                    <NavLink className="nav-link-white" tag={Link} to="/dashboard">Dashboard</NavLink>
                </NavItem>
                        <NavItem>
                            <NavLink className="nav-link-white" tag={Link} to="/games/active">Lista de Partidas</NavLink>
                        </NavItem>
                <NavItem>
                    <NavLink className="nav-link-white" id="game-history" tag={Link} to="/games/history">Historial de partidas</NavLink>
                </NavItem>
                <NavItem>
                    <NavLink className="nav-link-white" id="community-users" tag={Link} to="/community">Usuarios</NavLink>
                </NavItem>
            </>
        )
        userLogout = (
            <>
                <NavItem>
                    <NavLink className="nav-link-white" id="docs" tag={Link} to="/docs">Docs</NavLink>
                </NavItem>
                <NavItem>
                    <NavLink className="nav-link-white" id="plans" tag={Link} to="/plans">Pricing Plans</NavLink>
                </NavItem>
                <NavItem>
                    <NavLink className="nav-link-white" id="profile" tag={Link} to="/profile">Perfil</NavLink>
                </NavItem>
                <NavbarText key={sessionStamp} className="nav-link-white justify-content-end app-navbar-user-area">
                    <FaUserCircle className="app-navbar-avatar-icon" aria-hidden="true" />
                    <span className="app-navbar-user-text">{username}</span>
                    <FaChevronDown className="app-navbar-caret-icon" aria-hidden="true" />
                </NavbarText>
                <span className="app-navbar-user-divider d-none d-md-inline" aria-hidden="true">|</span>
                <NavItem className="d-flex align-items-center">
                    <NavLink className="nav-link-white app-navbar-gear-btn" tag={Link} to="/user-hub" title="Ajustes de cuenta" aria-label="Ajustes">
                        <FaCog aria-hidden="true" />
                    </NavLink>
                </NavItem>
                <NavItem className="d-flex">
                    <NavLink className="nav-link-white" id="logout" tag={Link} to="/logout">Logout</NavLink>
                </NavItem>
            </>
        )

    }

    return (
        <div>
            <Navbar expand="md" dark color="dark" className="app-navbar">
                <NavbarBrand href="/" aria-label="Your Game">
                    <img className="app-navbar-logo" alt="Cluedo" src={logoCluedo} />
                </NavbarBrand>
                <NavbarToggler onClick={toggleNavbar} className="ms-2" />
                <Collapse isOpen={!collapsed} navbar>
                    <Nav className="me-auto mb-2 mb-lg-0" navbar>
                        {userLinks}
                        {adminLinks}
                        {ownerLinks}
                    </Nav>
                    <Nav className="ms-auto mb-2 mb-lg-0" navbar>
                        {publicLinks}
                        {userLogout}
                    </Nav>
                </Collapse>
            </Navbar>
        </div>
    );
}

export default AppNavbar;
