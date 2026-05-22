import { ProductUpdateFormValues, ProductUpdateInput } from "../../validation/productUpdateSchema";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import FormInput from "../Form/FormInput";
import { productUpdateSchema } from "../../validation/productUpdateSchema";
import { useRef, useState, useEffect } from "react";
import { formatCategory } from "../../utils/formatCategory";
import "../ProductCreateForm/ProductCreateForm.scss";

export type ProductUpdatePayload = ProductUpdateFormValues & {
    existingImages: string[];
};

interface Props {
    categories: string[];
    loading: boolean;
    onSubmit: (values: ProductUpdatePayload) => void;
    backendError?: string;
    initialValues: ProductUpdateFormValues;
    initialExistingImages: string[];
}

const MAX_IMAGES = 6;

const ProductUpdateForm = ({
    categories,
    loading,
    onSubmit,
    backendError,
    initialValues,
    initialExistingImages,
}: Props) => {
    const {
        register,
        handleSubmit,
        setValue,
        formState: { errors, isSubmitting },
    } = useForm<ProductUpdateInput, any, ProductUpdateFormValues>({
        resolver: zodResolver(productUpdateSchema),
        defaultValues: initialValues,
    });

    const [existingImages, setExistingImages] = useState<string[]>(initialExistingImages);
    const [newImages, setNewImages] = useState<File[]>([]);
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        setExistingImages(initialExistingImages);
    }, [initialExistingImages]);

    const totalImages = existingImages.length + newImages.length;
    const remaining = MAX_IMAGES - totalImages;

    const handleImagesChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files) return;
        const files = Array.from(e.target.files);
        if (totalImages + files.length > MAX_IMAGES) {
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

    const removeExistingImage = (img: string) => {
        setExistingImages(prev => prev.filter(i => i !== img));
    };

    const removeNewImage = (indexToRemove: number) => {
        setNewImages(prev => {
            const updated = prev.filter((_, i) => i !== indexToRemove);
            setValue("images", updated, { shouldValidate: true });
            return updated;
        });
    };

    const submitHandler = (data: ProductUpdateFormValues) => {
        const total = existingImages.length + (data.images?.length || 0);
        if (total < 2 || total > 6) {
            alert("Listing must have between 2 and 6 photos");
            return;
        }
        onSubmit({ ...data, existingImages });
    };

    return (
        <div className="product-form-card">
            <h2 className="product-form-card__title">Edit listing</h2>

            {backendError && (
                <div className="product-form-card__error">{backendError}</div>
            )}

            <form onSubmit={handleSubmit(submitHandler)} noValidate>
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
                    error={errors.price as any}
                />

                <div className={"form-group" + (errors.category ? " form-group--error" : "")}>
                    <label htmlFor="upd-category">Category</label>
                    <select id="upd-category" {...register("category")} disabled={loading}>
                        <option value="">Select a category</option>
                        {categories.map(cat => (
                            <option key={cat} value={cat}>{formatCategory(cat)}</option>
                        ))}
                    </select>
                    {errors.category && <p>{errors.category.message}</p>}
                </div>

                <div className="form-group">
                    <label>Photos</label>

                    {totalImages > 0 && (
                        <p className="image-counter">
                            <strong>{totalImages}</strong> / {MAX_IMAGES} photos
                        </p>
                    )}

                    {(existingImages.length > 0 || newImages.length > 0) && (
                        <div className="image-preview-grid">
                            {existingImages.map((img, index) => (
                                <div key={img} className="image-preview-item">
                                    <img src={img} alt={"Existing " + (index + 1)} />
                                    {index === 0 && newImages.length === 0 && (
                                        <span className="image-preview-item__badge">Cover</span>
                                    )}
                                    <button
                                        type="button"
                                        className="image-preview-item__remove"
                                        onClick={() => removeExistingImage(img)}
                                        aria-label="Remove image"
                                    >
                                        x
                                    </button>
                                </div>
                            ))}
                            {newImages.map((file, index) => (
                                <div key={"new-" + index} className="image-preview-item">
                                    <img src={URL.createObjectURL(file)} alt={"New " + (index + 1)} />
                                    <span className="image-preview-item__badge">New</span>
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
                        <label className="upload-zone" htmlFor="upd-images-input">
                            <span className="upload-zone__icon">📷</span>
                            <span className="upload-zone__label">
                                {totalImages === 0 ? "Click to add photos" : "Add more photos"}
                            </span>
                            <span className="upload-zone__hint">
                                {"Up to " + remaining + " more · JPG, PNG, WEBP"}
                            </span>
                            <input
                                id="upd-images-input"
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
                    Save changes
                </button>
            </form>
        </div>
    );
};

export default ProductUpdateForm;
