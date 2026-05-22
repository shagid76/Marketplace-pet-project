import React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { reportSchema, ReportFormValues } from "../../validation/reportCreateSchema";
import FormInput from "../Form/FormInput";
import { useEffect } from "react";

type Props = {
    targetType: "USER" | "PRODUCT" | "REVIEW";
    targetId: string;
    onSubmit: (data: ReportFormValues) => void;
};

const ReportForm: React.FC<Props> = ({ targetType, targetId, onSubmit }) => {
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isSubmitting },
    } = useForm<ReportFormValues>({
        resolver: zodResolver(reportSchema),
        defaultValues: { targetType, targetId, description: "" },
    });

    useEffect(() => {
        reset({ targetType, targetId, description: "" });
    }, [targetType, targetId, reset]);

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <input type="hidden" {...register("targetType")} />
            <input type="hidden" {...register("targetId")} />

            <FormInput
                name="description"
                label="Details"
                placeholder="Tell us what happened…"
                register={register}
                error={errors.description}
            />

            <button
                type="submit"
                className={`btn btn--block btn--lg${isSubmitting ? " btn--loading" : ""}`}
                disabled={isSubmitting}
            >
                Submit report
            </button>
        </form>
    );
};

export default ReportForm;
