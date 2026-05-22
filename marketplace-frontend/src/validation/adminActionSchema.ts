import { z } from "zod";

export const adminActionSchema = z.object({
    targetId: z.string().min(1),

    targetType: z.enum(["USER", "PRODUCT", "REVIEW"]),

    actionType: z.enum(["BAN", "BLOCK"]),

    reason: z.string()
        .trim()
        .min(5, "Reason must be at least 5 characters")
        .max(256, "Reason must be at most 256 characters"),

  expiresAt: z
    .string()
    .optional()
    .transform((val) => {
        if (!val) return undefined;
        return new Date(val).toISOString();
    }),
})
.superRefine((data, ctx) => {
    if (data.actionType === "BLOCK") {
        if (!data.expiresAt) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                message: "Unblock time is required for BLOCK",
                path: ["expiresAt"],
            });
        }
    }

    if (data.actionType === "BAN") {
        if (data.expiresAt) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                message: "Ban cannot have expiration date",
                path: ["expiresAt"],
            });
        }
    }
});

export type AdminActionFormValues = z.infer<typeof adminActionSchema>;