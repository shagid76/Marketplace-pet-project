import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
    adminActionSchema,
    AdminActionFormValues,
} from "../../../validation/adminActionSchema";

type Props = {
    initialValues: AdminActionFormValues;
    onSubmit: (data: AdminActionFormValues) => Promise<void> | void;
    submitLabel: string;
    allowActionToggle?: boolean;
    onActionTypeChange?: (type: "BAN" | "BLOCK") => void;
};

function toDateTimeLocalValue(value?: string | null) {
    if (!value) return "";
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return "";
    const pad = (n: number) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

const AdminActionForm: React.FC<Props> = ({
    initialValues, onSubmit, submitLabel, allowActionToggle = true, onActionTypeChange,
}) => {
    const {
        register,
        handleSubmit,
        watch,
        setValue,
        reset,
        formState: { errors, isSubmitting },
    } = useForm<AdminActionFormValues>({
        resolver: zodResolver(adminActionSchema),
        defaultValues: initialValues,
    });

    const actionType = watch("actionType");

    useEffect(() => {
        reset({
            ...initialValues,
            expiresAt: initialValues.expiresAt ? toDateTimeLocalValue(initialValues.expiresAt) : "",
        });
    }, [initialValues, reset]);

    const setBan = () => {
        setValue("actionType", "BAN", { shouldValidate: true, shouldDirty: true });
        setValue("expiresAt", "", { shouldValidate: true, shouldDirty: true });
        onActionTypeChange?.("BAN");
    };

    const setBlock = () => {
        setValue("actionType", "BLOCK", { shouldValidate: true, shouldDirty: true });
        onActionTypeChange?.("BLOCK");
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <input type="hidden" {...register("targetId")} />
            <input type="hidden" {...register("targetType")} />

            {allowActionToggle ? (
                <div className="u-row u-mb-4">
                    <button
                        type="button"
                        onClick={setBan}
                        className={`btn btn--sm ${actionType === "BAN" ? "" : "btn--secondary"}`}
                    >
                        Ban
                    </button>
                    <button
                        type="button"
                        onClick={setBlock}
                        className={`btn btn--sm ${actionType === "BLOCK" ? "" : "btn--secondary"}`}
                    >
                        Block
                    </button>
                </div>
            ) : (
                <input type="hidden" {...register("actionType")} />
            )}

            <div className={`form-group${errors.reason ? " form-group--error" : ""}`}>
                <label>Reason</label>
                <input {...register("reason")} placeholder="Why this action?" />
                {errors.reason && <p>{errors.reason.message}</p>}
            </div>

            {actionType === "BLOCK" && (
                <div className={`form-group${errors.expiresAt ? " form-group--error" : ""}`}>
                    <label>Expires at</label>
                    <input type="datetime-local" {...register("expiresAt")} />
                    {errors.expiresAt && <p>{errors.expiresAt.message}</p>}
                </div>
            )}

            <button
                type="submit"
                className={`btn btn--block btn--lg${isSubmitting ? " btn--loading" : ""}`}
                disabled={isSubmitting}
            >
                {submitLabel}
            </button>
        </form>
    );
};

export default AdminActionForm;
