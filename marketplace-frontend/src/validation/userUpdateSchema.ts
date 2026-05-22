import { z } from "zod"

export const userUpdateSchema = z.object({
    username: z.string()
        .trim()
        .min(5, "Min 5 characters")
        .max(128, "Max 128 characters"),
    avatar: z.instanceof(File, {
        message: "Image is required"
    }).optional(),
    removeAvatar: z.boolean().optional().default(false),
})

export type UserUpdateFormValues = z.infer<typeof userUpdateSchema>