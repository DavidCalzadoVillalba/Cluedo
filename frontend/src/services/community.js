import api from "./api";

export const getCommunityUsers = async ({ page = 0, size = 6, search = "" } = {}) => {
    const params = { page, size };
    if (search && search.trim()) {
        params.search = search.trim();
    }
    const res = await api.get("/community/users", { params });
    return res.data;
};
