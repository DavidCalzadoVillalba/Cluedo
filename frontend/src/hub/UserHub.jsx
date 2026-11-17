import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { FaArrowLeft, FaHistory, FaUser } from "react-icons/fa";
import Profile from "../profile/Profile";
import GameHistory from "../game/GameHistory";
import "./hub.css";

export default function UserHub() {
    const [searchParams, setSearchParams] = useSearchParams();
    const initialTab = searchParams.get("tab") === "history" ? "history" : "profile";
    const [activeTab, setActiveTab] = useState(initialTab);

    function handleTabChange(tab) {
        setActiveTab(tab);
        setSearchParams({ tab });
    }

    return (
        <main className="hub-page">
            <header className="hub-nav-header">
                <Link to="/" className="hub-back-btn" aria-label="Volver al menú principal">
                    <FaArrowLeft aria-hidden="true" /> Menú principal
                </Link>

                <nav className="hub-tabs-container" aria-label="Secciones de usuario" role="tablist">
                    <button
                        type="button"
                        role="tab"
                        id="tab-profile"
                        aria-controls="panel-profile"
                        aria-selected={activeTab === "profile"}
                        className={`hub-tab-btn ${activeTab === "profile" ? "active" : ""}`}
                        onClick={() => handleTabChange("profile")}
                    >
                        <FaUser aria-hidden="true" /> Perfil
                    </button>
                    <button
                        type="button"
                        role="tab"
                        id="tab-history"
                        aria-controls="panel-history"
                        aria-selected={activeTab === "history"}
                        className={`hub-tab-btn ${activeTab === "history" ? "active" : ""}`}
                        onClick={() => handleTabChange("history")}
                    >
                        <FaHistory aria-hidden="true" /> Historial de partidas
                    </button>
                </nav>
            </header>

            <div className="hub-content">
                {activeTab === "profile" ? (
                    <div id="panel-profile" role="tabpanel" aria-labelledby="tab-profile">
                        <Profile />
                    </div>
                ) : (
                    <div id="panel-history" role="tabpanel" aria-labelledby="tab-history">
                        <GameHistory />
                    </div>
                )}
            </div>
        </main>
    );
}
