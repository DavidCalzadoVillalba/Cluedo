import api from "./api";

export const getAllGames = async () => {
    try {
        const response = await api.get("/games/active");
        return response.data;
    } catch (error) {
        if (error.response?.status === 403) {
            error.response.data = {
                ...error.response.data,
                message: "Necesitas el Rol de Administrador para consultar la lista de partidas",
            };
        }
        throw error;
    }
};

export const getMyGameHistory = async () => {
    const response = await api.get("/games/history");
    return response.data;
};
