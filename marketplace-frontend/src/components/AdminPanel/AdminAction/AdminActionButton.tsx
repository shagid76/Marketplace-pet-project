import { useBan } from "../../../context/BanContext";
import { ActiveAdminAction } from "../../../types/AdminAction/AdminAction";
import { getCurrentUserId } from "../../../services/authService";

type Props = {
    targetId: string;
    targetType: "USER" | "PRODUCT" | "REVIEW";
    activeAction?: ActiveAdminAction | null;
};

export default function AdminActionButton({ targetId, targetType, activeAction }: Props) {
    const { openCreateBan, openCreateBlock, openEditBlock, openRevoke } = useBan();

    const isBanned = activeAction?.actionType === "BAN";
    const isBlocked = activeAction?.actionType === "BLOCK";
    const isSelf = targetType === "USER" && getCurrentUserId() === targetId;

    if (isSelf) {
        return <span className="u-text-muted" style={{ fontSize: "0.75rem" }}>You</span>;
    }

    return (
        <div className="admin-action">
            <div className="admin-action__row">
                <button
                    type="button"
                    className={`btn btn--sm ${isBanned ? "btn--secondary" : "btn--danger"}`}
                    onClick={() =>
                        isBanned && activeAction
                            ? openRevoke({ adminActionId: activeAction.id, actionType: "BAN" })
                            : openCreateBan({ targetId, targetType })
                    }
                >
                    {isBanned ? "Unban" : "Ban"}
                </button>

                {targetType === "USER" && (
                    <button
                        type="button"
                        className={`btn btn--sm ${isBlocked ? "btn--secondary" : "btn--ghost"}`}
                        onClick={() =>
                            isBlocked && activeAction
                                ? openEditBlock({
                                    targetId,
                                    targetType,
                                    adminActionId: activeAction.id,
                                    expiresAt: activeAction.expiresAt,
                                })
                                : openCreateBlock({ targetId, targetType })
                        }
                    >
                        {isBlocked ? "Edit block" : "Block"}
                    </button>
                )}
            </div>

            {activeAction && (
                <p className="admin-action__note">
                    {isBanned
                        ? "Currently banned"
                        : isBlocked
                            ? `Blocked until ${activeAction.expiresAt ? new Date(activeAction.expiresAt).toLocaleString() : "unknown"}`
                            : ""}
                </p>
            )}
        </div>
    );
}
