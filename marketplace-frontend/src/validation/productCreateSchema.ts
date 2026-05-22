import { z } from "zod"

export const productCreateSchema = z.object({
    title: z.string()
        .trim()
        .min(5, "Title must be at least 5 characters")
        .max(128, "Title must be at most 128 characters"),

    description: z.string()
        .trim()
        .min(10, "Description must be at least 10 characters")
        .max(256, "Description must be at most 256 characters"),

    price: z.coerce.number()
        .positive("Price must be positive"),

    images: z
        .array(z.instanceof(File))
        .min(2, "At least 2 images required")
        .max(6, "Maximum 6 images allowed"),

    category: z.string().min(1, "Category is required")
})
export type ProductCreateInput = z.input<typeof productCreateSchema>;
export type ProductCreateFormValues = z.infer<typeof productCreateSchema>;