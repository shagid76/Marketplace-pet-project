import api from "../api/axiosInstance";
import { AdminActionFormValues } from "../validation/adminActionSchema";
import { ExtendFormValues } from "../types/AdminAction/AdminAction";
import { ActiveAdminAction } from "../types/AdminAction/AdminAction";

type AdminTargetType = "USER" | "PRODUCT" | "REVIEW";

export const createAdminAction = async (data: AdminActionFormValues): Promise<ActiveAdminAction> => {
    const res = await api.post<ActiveAdminAction>("/admin-actions", data);
    return res.data;
};

export const getActiveAdminAction = (targetId: string, targetType: AdminTargetType) => {
    return api.get<ActiveAdminAction | null>("/admin-actions/active", {
        params: { targetId, targetType }
    });
};

export const editAdminAction = async (adminActionId: string, data: ExtendFormValues): Promise<ActiveAdminAction> => {
    const response = await api.patch<ActiveAdminAction>(`/admin-actions/extend/${adminActionId}`, data);
    return response.data;
};

export const revokeAdminAction = async (adminActionId: string): Promise<void> => {
    await api.delete(`/admin-actions/revoke/${adminActionId}`);
};
