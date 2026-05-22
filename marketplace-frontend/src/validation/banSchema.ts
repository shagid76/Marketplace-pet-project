import {z} from "zod"

export const banSchema = z.object({
     targetType: z.enum(["USER", "PRODUCT", "REVIEW"]),
     targetId: z.string().min(1, "Target ID is required"),
     reason: z.string()
        .trim()
        .min(5, "min 5")
        .max(128, "max 128")

})
export type BanFormValues = z.infer<typeof banSchema>