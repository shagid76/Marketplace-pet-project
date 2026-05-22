import React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { ReviewFormValues, reviewCreateSchema } from "../../validation/reviewCreateSchema";
import FormInput from "../Form/FormInput";
import { useEffect } from "react";

type ReviewInitialData = {
    description: string;
    rating: number;
} | null;

type Props = {
    targetId: string;
    initialData?: ReviewInitialData;
    onSubmit: (data: ReviewFormValues) => void;
};

const ReviewForm: React.FC<Props> = ({ targetId, initialData, onSubmit }) => {
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isSubmitting },
    } = useForm<ReviewFormValues>({
        resolver: zodResolver(reviewCreateSchema),
        defaultValues: { targetId, description: "", rating: 0 },
    });

    useEffect(() => {
        if (initialData) {
            reset({
                targetId,
                description: initialData.description ?? "",
                rating: initialData.rating ?? 0,
            });
        }
    }, [initialData, targetId, reset]);

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <input type="hidden" {...register("targetId")} />

            <FormInput
                name="description"
                label="Your review"
                placeholder="What did you like or dislike?"
                register={register}
                error={errors.description}
            />

            <FormInput
                name="rating"
                type="number"
                step="0.1"
                label="Rating (0.0 – 5.0)"
                placeholder="4.5"
                register={register}
                error={errors.rating}
            />

            <button
                type="submit"
                className={`btn btn--block btn--lg${isSubmitting ? " btn--loading" : ""}`}
                disabled={isSubmitting}
            >
                Submit review
            </button>
        </form>
    );
};

export default ReviewForm;
