import { useEffect, useState } from "react";
import {
    FaAngleDoubleLeft,
    FaAngleDoubleRight,
    FaAngleLeft,
    FaAngleRight,
    FaSearch,
    FaTimes,
    FaUser,
    FaUsers,
} from "react-icons/fa";
import { CHARACTER_IMAGES } from "../game/gameConstants";
import { getCommunityUsers } from "../services/community";
import "./playerDirectory.css";

const PAGE_SIZE = 6;

function resolveAvatar(avatar) {
    if (!avatar) return null;
    if (CHARACTER_IMAGES[avatar]) return CHARACTER_IMAGES[avatar];
    return avatar;
}

export default function PlayerDirectory() {
    const [users, setUsers] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [totalElements, setTotalElements] = useState(0);
    const [searchInput, setSearchInput] = useState("");
    const [activeSearch, setActiveSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);

    useEffect(() => {
        let mounted = true;
        setLoading(true);
        setError(false);

        getCommunityUsers({ page, size: PAGE_SIZE, search: activeSearch })
            .then((data) => {
                if (!mounted) return;
                const items = data.content ?? (Array.isArray(data) ? data : []);
                setUsers(items);
                setTotalPages(Math.max(data.totalPages ?? 1, 1));
                setTotalElements(data.totalElements ?? items.length);
            })
            .catch(() => {
                if (!mounted) return;
                setError(true);
            })
            .finally(() => {
                if (!mounted) return;
                setLoading(false);
            });

        return () => {
            mounted = false;
        };
    }, [page, activeSearch]);

    function handleSearchSubmit(event) {
        event.preventDefault();
        setPage(0);
        setActiveSearch(searchInput.trim());
    }

    function handleClearSearch() {
        setSearchInput("");
        setActiveSearch("");
        setPage(0);
    }

    const currentPageDisplay = totalElements === 0 ? 1 : page + 1;
    const canGoPrev = page > 0;
    const canGoNext = page < totalPages - 1;

    return (
        <main className="community-page">
            <div className="community-header-badge">
                <h1 id="community-title">
                    <FaUsers aria-hidden="true" /> Usuarios registrados
                </h1>
            </div>

            <section className="community-panel" aria-labelledby="community-title">
                <form className="community-search-bar" role="search" onSubmit={handleSearchSubmit}>
                    <div className="community-search-input-wrapper">
                        <FaSearch className="community-search-icon" aria-hidden="true" />
                        <input
                            type="text"
                            className="community-search-input"
                            placeholder="Buscar por nombre de usuario..."
                            aria-label="Buscar por nombre de usuario"
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                        />
                        {searchInput ? (
                            <button
                                type="button"
                                className="community-clear-button"
                                aria-label="Limpiar búsqueda"
                                onClick={handleClearSearch}
                            >
                                <FaTimes aria-hidden="true" />
                            </button>
                        ) : null}
                    </div>
                    <button type="submit" className="community-search-button" aria-label="Buscar">
                        <FaSearch aria-hidden="true" />
                    </button>
                </form>

                {loading ? (
                    <p className="community-message" role="status">Cargando usuarios…</p>
                ) : null}

                {error ? (
                    <p className="community-message community-error" role="alert">
                        No se ha podido cargar el listado de usuarios. Inténtalo de nuevo más tarde.
                    </p>
                ) : null}

                {!loading && !error && users.length === 0 ? (
                    <p className="community-message">
                        {activeSearch
                            ? `No se han encontrado usuarios que coincidan con "${activeSearch}".`
                            : "No hay usuarios registrados disponibles."}
                    </p>
                ) : null}

                {!loading && !error && users.length > 0 ? (
                    <div className="community-grid" role="list">
                        {users.map((user) => {
                            const avatarSrc = resolveAvatar(user.avatar);
                            return (
                                <article key={user.id ?? user.username} className="community-user-card" role="listitem">
                                    <div className="community-avatar-frame">
                                        {avatarSrc ? (
                                            <img
                                                src={avatarSrc}
                                                alt={`Avatar de ${user.username}`}
                                                className="community-avatar-img"
                                            />
                                        ) : (
                                            <div className="community-avatar-placeholder">
                                                <FaUser aria-hidden="true" />
                                            </div>
                                        )}
                                    </div>
                                    <span className="community-username" title={user.username}>
                                        {user.username}
                                    </span>
                                </article>
                            );
                        })}
                    </div>
                ) : null}

                {!loading && !error && (
                    <nav className="community-pagination" aria-label="Paginación de usuarios">
                        <button
                            type="button"
                            className="community-page-btn"
                            aria-label="Primera página"
                            onClick={() => setPage(0)}
                            disabled={!canGoPrev}
                        >
                            <FaAngleDoubleLeft aria-hidden="true" />
                        </button>
                        <button
                            type="button"
                            className="community-page-btn"
                            aria-label="Página anterior"
                            onClick={() => setPage((p) => Math.max(p - 1, 0))}
                            disabled={!canGoPrev}
                        >
                            <FaAngleLeft aria-hidden="true" />
                        </button>
                        <span className="community-page-indicator" aria-current="page">
                            {currentPageDisplay} / {totalPages}
                        </span>
                        <button
                            type="button"
                            className="community-page-btn"
                            aria-label="Página siguiente"
                            onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
                            disabled={!canGoNext}
                        >
                            <FaAngleRight aria-hidden="true" />
                        </button>
                        <button
                            type="button"
                            className="community-page-btn"
                            aria-label="Última página"
                            onClick={() => setPage(totalPages - 1)}
                            disabled={!canGoNext}
                        >
                            <FaAngleDoubleRight aria-hidden="true" />
                        </button>
                    </nav>
                )}
            </section>
        </main>
    );
}
