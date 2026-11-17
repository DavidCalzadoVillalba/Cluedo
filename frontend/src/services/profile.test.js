import { beforeEach, describe, expect, test, vi } from "vitest";
import api from "./api";
import tokenService from "./token.service";
import { getMyProfile, updateMyProfile } from "./profile";

vi.mock("./api", () => ({
    default: { get: vi.fn(), put: vi.fn() },
}));

vi.mock("./token.service", () => ({
    default: { updateLocalAccessToken: vi.fn() },
}));

describe("profile service", () => {
    beforeEach(() => {
        api.get.mockReset();
        api.put.mockReset();
        tokenService.updateLocalAccessToken.mockReset();
    });

    test("loads the authenticated profile", async () => {
        api.get.mockResolvedValue({ data: { username: "player1" } });

        await expect(getMyProfile()).resolves.toEqual({ username: "player1" });
        expect(api.get).toHaveBeenCalledWith("/profile");
    });

    test("renews the session token after a successful update", async () => {
        api.put.mockResolvedValue({ data: { username: "nuevo", token: "jwt-nuevo" } });
        const dispatch = vi.spyOn(window, "dispatchEvent");

        await updateMyProfile({ username: "nuevo", email: "nuevo@cluedo.test", avatar: null });

        expect(api.put).toHaveBeenCalledWith("/profile", {
            username: "nuevo",
            email: "nuevo@cluedo.test",
            avatar: null,
        });
        expect(tokenService.updateLocalAccessToken).toHaveBeenCalledWith("jwt-nuevo");
        expect(dispatch).toHaveBeenCalledWith(expect.any(Event));
        dispatch.mockRestore();
    });
});
