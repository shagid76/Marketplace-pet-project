import { ProductCreateFormValues, ProductCreateInput, productCreateSchema } from "../../validation/productCreateSchema";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import FormInput from "../Form/FormInput";
import { useRef, useState } from "react";
import { formatCategory } from "../../utils/formatCategory";
import "./ProductCreateForm.scss";

interface Props {
    categories: string[];
    loading: boolean;
    onSubmit: (data: ProductCreateFormValues) => void;
    backendError?: string;
}

const MAX_IMAGES = 6;

const ProductCreateForm = ({ categories, loading, onSubmit, backendError }: Props) => {
    const {
        register,
        handleSubmit,
        setValue,
        formState: { errors, isSubmitting },
    } = useForm<ProductCreateInput, any, ProductCreateFormValues>({
        resolver: zodResolver(productCreateSchema),
    });

    const [newImages, setNewImages] = useState<File[]>([]);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleImagesChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files) return;
        const files = Array.from(e.target.files);
        if (newImages.length + files.length > MAX_IMAGES) {
            alert("Maximum " + MAX_IMAGES + " images allowed");
            return;
        }
        setNewImages(prev => {
            const updated = [...prev, ...files];
            setValue("images", updated, { shouldValidate: true, shouldDirty: true });
            return updated;
        });
        if (fileInputRef.current) fileInputRef.current.value = "";
    };

    const removeNewImage = (indexToRemove: number) => {
        setNewImages(prev => {
            const updated = prev.filter((_, i) => i !== indexToRemove);
            setValue("images", updated, { shouldValidate: true });
            return updated;
        });
    };

    const remaining = MAX_IMAGES - newImages.length;

    return (
        <div className="product-form-card">
            <h2 className="product-form-card__title">Create listing</h2>

            {backendError && (
                <div className="product-form-card__error">{backendError}</div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} noValidate>
                <FormInput
                    name="title"
                    type="text"
                    label="Title"
                    placeholder="e.g. Vintage leather jacket"
                    register={register}
                    error={errors.title}
                />
                <FormInput
                    name="description"
                    type="text"
                    label="Description"
                    placeholder="Describe the item, its condition, size, etc."
                    register={register}
                    error={errors.description}
                />
                <FormInput
                    name="price"
                    type="number"
                    step="0.01"
                    label="Price ($)"
                    placeholder="0.00"
                    register={register}
                    error={errors.price}
                />

                <div className={"form-group" + (errors.category ? " form-group--error" : "")}>
                    <label htmlFor="category">Category</label>
                    <select id="category" {...register("category")} disabled={loading}>
                        <option value="">Select a category</option>
                        {categories.map(cat => (
                            <option key={cat} value={cat}>{formatCategory(cat)}</option>
                        ))}
                    </select>
                    {errors.category && <p>{errors.category.message}</p>}
                </div>

                <div className="form-group">
                    <label>Photos</label>

                    {newImages.length > 0 && (
                        <p className="image-counter">
                            <strong>{newImages.length}</strong> / {MAX_IMAGES} photos
                        </p>
                    )}

                    {newImages.length > 0 && (
                        <div className="image-preview-grid">
                            {newImages.map((file, index) => (
                                <div key={index} className="image-preview-item">
                                    <img src={URL.createObjectURL(file)} alt={"Preview " + (index + 1)} />
                                    {index === 0 && <span className="image-preview-item__badge">Cover</span>}
                                    <button
                                        type="button"
                                        className="image-preview-item__remove"
                                        onClick={() => removeNewImage(index)}
                                        aria-label="Remove image"
                                    >
                                        x
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}

                    {remaining > 0 && (
                        <label className="upload-zone" htmlFor="images-input">
                            <span className="upload-zone__icon">📷</span>
                            <span className="upload-zone__label">
                                {newImages.length === 0 ? "Click to add photos" : "Add more photos"}
                            </span>
                            <span className="upload-zone__hint">
                                {"Up to " + remaining + " more · JPG, PNG, WEBP"}
                            </span>
                            <input
                                id="images-input"
                                ref={fileInputRef}
                                type="file"
                                multiple
                                accept="image/*"
                                onChange={handleImagesChange}
                            />
                        </label>
                    )}

                    {errors.images && (
                        <p className="image-field-error">{errors.images.message as string}</p>
                    )}
                </div>

                <button
                    type="submit"
                    className={"btn btn--block btn--lg" + (isSubmitting || loading ? " btn--loading" : "")}
                    disabled={isSubmitting || loading}
                >
                    Publish listing
                </button>
            </form>
        </div>
    );
};

export default ProductCreateForm;
