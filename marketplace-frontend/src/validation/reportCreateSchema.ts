import { z } from "zod";

export const reportSchema = z.object({
    targetType: z.enum(["USER", "PRODUCT", "REVIEW"]),
    targetId: z.string().min(1, "Target ID is required"),
    description: z.string()
        .trim()
        .min(10, "min 10")
        .max(256, "Description must be at most 256 characters")
});

export type ReportFormValues = z.infer<typeof reportSchema>;