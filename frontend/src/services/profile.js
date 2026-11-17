import api from "./api";
import tokenService from "./token.service";

export const getMyProfile = async () => {
    const res = await api.get("/profile");
    return res.data;
};

export const updateMyProfile = async (profile) => {
    const res = await api.put("/profile", {
        username: profile.username,
        email: profile.email,
        avatar: profile.avatar,
    });
    if (res.data?.token) {
        tokenService.updateLocalAccessToken(res.data.token);
        window.dispatchEvent(new Event("auth-changed"));
    }
    return res.data;
};
