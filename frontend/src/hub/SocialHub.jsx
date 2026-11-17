import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { FaArrowLeft, FaListAlt, FaUsers, FaUserShield } from "react-icons/fa";
import PlayerDirectory from "../community/PlayerDirectory";
import ActiveGamesAdmin from "../admin/games/ActiveGamesAdmin";
import UserListAdmin from "../admin/users/UserListAdmin";
import useAuth from "../hooks/useAuth";
import "./hub.css";

export default function SocialHub() {
    const { roles } = useAuth();
    const isAdmin = Array.isArray(roles) && roles.includes("ADMIN");

    const [searchParams, setSearchParams] = useSearchParams();
    const rawTab = searchParams.get("tab");

    const getValidTab = (tab, admin) => {
        if (!admin) return "users";
        if (tab === "games" || tab === "admin-users") return tab;
        return "users";
    };

    const [activeTab, setActiveTab] = useState(() => getValidTab(rawTab, isAdmin));

    useEffect(() => {
        setActiveTab(getValidTab(rawTab, isAdmin));
    }, [rawTab, isAdmin]);

    function handleTabChange(tab) {
        if (!isAdmin && tab !== "users") return;
        setActiveTab(tab);
        setSearchParams({ tab });
    }

    return (
        <main className="hub-page">
            <header className="hub-nav-header">
                <Link to="/" className="hub-back-btn" aria-label="Volver al menú principal">
                    <FaArrowLeft aria-hidden="true" /> Menú principal
                </Link>

                <nav className="hub-tabs-container" aria-label="Secciones de comunidad" role="tablist">
                    <button
                        type="button"
                        role="tab"
                        id="tab-users"
                        aria-controls="panel-users"
                        aria-selected={activeTab === "users"}
                        className={`hub-tab-btn ${activeTab === "users" ? "active" : ""}`}
                        onClick={() => handleTabChange("users")}
                    >
                        <FaUsers aria-hidden="true" /> Usuarios
                    </button>
                    {isAdmin && (
                        <>
                            <button
                                type="button"
                                role="tab"
                                id="tab-games"
                                aria-controls="panel-games"
                                aria-selected={activeTab === "games"}
                                className={`hub-tab-btn ${activeTab === "games" ? "active" : ""}`}
                                onClick={() => handleTabChange("games")}
                            >
                                <FaListAlt aria-hidden="true" /> Lista de partidas
                            </button>
                            <button
                                type="button"
                                role="tab"
                                id="tab-admin-users"
                                aria-controls="panel-admin-users"
                                aria-selected={activeTab === "admin-users"}
                                className={`hub-tab-btn ${activeTab === "admin-users" ? "active" : ""}`}
                                onClick={() => handleTabChange("admin-users")}
                            >
                                <FaUserShield aria-hidden="true" /> Administración de usuarios
                            </button>
                        </>
                    )}
                </nav>
            </header>

            <div className="hub-content">
                {activeTab === "users" && (
                    <div id="panel-users" role="tabpanel" aria-labelledby="tab-users" style={{ width: "100%" }}>
                        <PlayerDirectory />
                    </div>
                )}
                {isAdmin && activeTab === "games" && (
                    <div id="panel-games" role="tabpanel" aria-labelledby="tab-games" style={{ width: "100%" }}>
                        <ActiveGamesAdmin />
                    </div>
                )}
                {isAdmin && activeTab === "admin-users" && (
                    <div id="panel-admin-users" role="tabpanel" aria-labelledby="tab-admin-users" style={{ width: "100%" }}>
                        <UserListAdmin />
                    </div>
                )}
            </div>
        </main>
    );
}
