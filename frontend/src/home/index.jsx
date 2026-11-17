import '../App.css';
import '../static/css/home/home.css';
import logoCluedo from '../static/images/Cluedo_logo.png';

import { Link } from 'react-router-dom';
import { FaPlay, FaSearch, FaSignInAlt, FaUser, FaUsers } from 'react-icons/fa';
import useAuth from '../hooks/useAuth';

export default function Home() {
    const { jwt } = useAuth();
    const isAuthenticated = Boolean(jwt);

    return (
        <div className="home-page-container">
            <div className="home-menu">
                <div className="home-logo-container">
                    <img className="home-logo" src={logoCluedo} alt="Cluedo" />
                    <div className="home-logo-divider" aria-hidden="true">
                        <span className="home-divider-line" />
                        <span className="home-divider-dot" />
                        <span className="home-divider-icon">
                            <FaSearch />
                        </span>
                        <span className="home-divider-dot" />
                        <span className="home-divider-line" />
                    </div>
                </div>

                {isAuthenticated ? (
                    <div className="home-main-section">
                        <div className="home-card">
                            <div className="home-card-col home-card-create">
                                <div className="home-card-icon-wrap" aria-hidden="true">
                                    <img src="/images/dadosDashboard1.png" alt="Dados" className="home-dice-img" />
                                </div>
                                <h2>Crear partida</h2>
                                <p className="home-card-sub"> </p>
                                <Link to="/create-game" className="home-pill-link">
                                    <button className="home-pill-btn" aria-label="Crear partida">
                                        <FaPlay aria-hidden="true" className="home-btn-icon" />
                                        <span>Crear partida</span>
                                    </button>
                                </Link>
                            </div>

                            <div className="home-card-divider" aria-hidden="true" />

                            <div className="home-card-col home-card-join">
                                <div className="home-card-icon-wrap" aria-hidden="true">
                                    <FaUsers className="home-card-users-icon" />
                                </div>
                                <h2>Unirse a partida</h2>
                                <Link to="/join-game" className="home-pill-link">
                                    <button className="home-pill-btn" aria-label="Unirse a partida">
                                        <FaUsers aria-hidden="true" className="home-btn-icon" />
                                        <span>Unirse</span>
                                    </button>
                                </Link>
                            </div>
                        </div>

                        <nav className="home-capsule-nav" aria-label="Accesos rápidos">
                            <Link to="/user-hub" className="home-capsule-btn" id="home-profile-link">
                                <FaUser aria-hidden="true" className="home-capsule-icon" />
                                <span>Gestionar perfil</span>
                            </Link>
                            <span className="home-capsule-sep" aria-hidden="true" />
                            <Link to="/social-hub" className="home-capsule-btn" id="home-social-link">
                                <FaUsers aria-hidden="true" className="home-capsule-icon" />
                                <span>Menú social</span>
                            </Link>
                        </nav>
                    </div>
                ) : (
                    <div className="hero-div home-login-only-hero">
                        <Link to="/login" style={{ textDecoration: 'none', width: 'min(100%, 440px)' }}>
                            <button className="create-game-btn home-login-main-btn" aria-label="Iniciar sesión">
                                <FaSignInAlt aria-hidden="true" style={{ marginRight: '0.6rem' }} /> Iniciar sesión
                            </button>
                        </Link>
                        <p className="home-register-hint">
                            ¿No tienes cuenta? <Link to="/register">Regístrate aquí</Link>
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
}
