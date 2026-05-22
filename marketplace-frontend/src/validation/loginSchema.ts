import { z } from "zod"

export const loginSchema = z.object({
    email: z.string()
        .trim()
        .min(5, "Min 5 characters")
        .email("Invalid email")
        .max(128, "Max 128 characters"),
    password: z.string()
        .min(8, "Min 8 characters")
        .max(128, "Max 128 characters")
})

export type LoginFormValues = z.infer<typeof loginSchema>