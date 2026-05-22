import { z } from "zod"

export const blockSchema = z.object({
    targetId: z.string().min(1, "Target ID is required"),
    reason: z.string()
        .trim()
        .min(5, "min 5")
        .max(128, "max 128"),

    bannedUntil: z.coerce.date() 
        .refine((date) => date > new Date(), {
            message: "Дата должна быть в будущем",
        }),

})
export type BlockFormValues = z.infer<typeof blockSchema>