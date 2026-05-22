import { useEffect, useState } from "react";
import { getActiveAdminAction } from "../services/adminService";
import { ActiveAdminAction } from "../types/AdminAction/AdminAction";

export const useActiveAdminAction = (
    targetId: string,
    targetType: "USER" | "PRODUCT" | "REVIEW"
) => {
    const [data, setData] = useState<ActiveAdminAction | null>(null);

    useEffect(() => {
        getActiveAdminAction(targetId, targetType)
            .then(res => setData(res.data))
            .catch(() => setData(null));
    }, [targetId, targetType]);

    return data;
};