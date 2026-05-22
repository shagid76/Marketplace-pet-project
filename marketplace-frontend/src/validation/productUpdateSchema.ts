import { z } from "zod"

export const productUpdateSchema = z.object({
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
        
    images: z.array(z.instanceof(File)).optional(), 
    existingImages: z.array(z.string()).optional(),

    category: z.string().min(1, "Category is required")
})

export type ProductUpdateFormValues = z.infer<typeof productUpdateSchema>;
export type ProductUpdateInput = z.input<typeof productUpdateSchema>