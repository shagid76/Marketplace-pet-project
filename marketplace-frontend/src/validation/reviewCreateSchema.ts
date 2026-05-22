import { z } from "zod"

export const reviewCreateSchema = z.object({
    description: z.string()
        .trim()
        .min(10, "Min 10 characters")
        .max(256, "Max 256 characters"),

    rating: z.coerce.number()
        .min(0, "Min 0.0")
        .max(5, "Max 5.0"),

    targetId: z.string().min(1, "Target ID is required"),
});

export type ReviewFormValues = z.infer<typeof reviewCreateSchema>