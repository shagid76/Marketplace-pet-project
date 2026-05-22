import React, { useEffect, useState } from "react";
import AdminActionForm from "./AdminActionForm";
import { useBan } from "../../../context/BanContext";
import { AdminActionFormValues } from "../../../validation/adminActionSchema";
import { createAdminAction, editAdminAction, revokeAdminAction } from "../../../services/adminService";
import { ExtendFormValues } from "../../../types/AdminAction/AdminAction";

const AdminActionModal: React.FC = () => {
    const { modal, closeAll } = useBan();
    const [currentActionType, setCurrentActionType] = useState<"BAN" | "BLOCK">("BAN");

    useEffect(() => {
        if (modal?.kind === "create") {
            setCurrentActionType(modal.actionType);
        }
    }, [modal]);

    if (!modal) return null;

    const notifyUpdated = () => window.dispatchEvent(new Event("admin-action-updated"));

    const handleCreate = async (data: AdminActionFormValues) => {
        await createAdminAction(data);
        closeAll();
        notifyUpdated();
    };

    const handleEdit = async (data: AdminActionFormValues) => {
        if (modal.kind !== "edit-block") return;
        const payload: ExtendFormValues = {
            newExpiresAt: data.expiresAt,
            revoke: false,
            reason: data.reason,
        };
        await editAdminAction(modal.adminActionId, payload);
        closeAll();
        notifyUpdated();
    };

    const handleRevoke = async () => {
        if (modal.kind !== "revoke") return;
        await revokeAdminAction(modal.adminActionId);
        closeAll();
        notifyUpdated();
    };

    const handleUnblock = async () => {
        if (modal.kind !== "edit-block") return;
        await revokeAdminAction(modal.adminActionId);
        closeAll();
        notifyUpdated();
    };

    return (
        <div className="modal-overlay" onClick={closeAll}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <button className="modal__close" onClick={closeAll} aria-label="Close">&#215;</button>

                {modal.kind === "create" && (
                    <>
                        <h2>{currentActionType === "BAN" ? "Ban target" : "Block target"}</h2>
                        <p className="u-text-muted u-mb-4">Provide a reason for this admin action.</p>
                        <AdminActionForm
                            initialValues={{
                                targetId: modal.targetId,
                                targetType: modal.targetType,
                                actionType: modal.actionType,
                                reason: "",
                                expiresAt: "",
                            }}
                            allowActionToggle={modal.targetType === "USER"}
                            submitLabel={currentActionType === "BAN" ? "Submit ban" : "Submit block"}
                            onActionTypeChange={setCurrentActionType}
                            onSubmit={handleCreate}
                        />
                    </>
                )}

                {modal.kind === "edit-block" && (
                    <>
                        <h2>Edit block</h2>
                        <p className="u-text-muted u-mb-4">Update the expiration time or remove the block entirely.</p>
                        <AdminActionForm
                            initialValues={{
                                targetId: modal.targetId,
                                targetType: modal.targetType,
                                actionType: "BLOCK",
                                reason: "",
                                expiresAt: modal.expiresAt ?? "",
                            }}
                            allowActionToggle={false}
                            submitLabel="Update block"
                            onSubmit={handleEdit}
                        />
                        <button
                            type="button"
                            onClick={handleUnblock}
                            className="btn btn--danger btn--block u-mt-4"
                        >
                            Unblock now
                        </button>
                    </>
                )}

                {modal.kind === "revoke" && (
                    <>
                        <h2>Are you sure?</h2>
                        <p className="u-text-muted u-mb-4">
                            {modal.actionType === "BAN"
                                ? "This will unban the target."
                                : "This will unblock the target."}
                        </p>
                        <div className="u-row">
                            <button onClick={handleRevoke} className="btn btn--danger">Confirm</button>
                            <button onClick={closeAll} className="btn btn--secondary">Cancel</button>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};

export default AdminActionModal;
