import { useState, useEffect } from "react";
import { useNavigate, useLocation, Navigate } from "react-router-dom";
import { getAllCategories, getProductById, updateProduct } from "../../services/productService";
import ProductUpdateForm from "../../components/ProductUpdateForm/ProductUpdateForm";
import { ProductUpdateFormValues } from "../../validation/productUpdateSchema";
import { useQueryClient } from "@tanstack/react-query";

const ProductUpdatePage = () => {
    const [initialValues, setInitialValues] = useState<ProductUpdateFormValues | null>(null);
    const [categories, setCategories] = useState<string[]>([]);
    const [existingImages, setExistingImages] = useState<string[]>([]);
    const [loading, setLoading] = useState(true);
    const [backendError, setBackendError] = useState<string | undefined>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const location = useLocation();
    const productId: string = location.state?.productId;

    useEffect(() => {
        const loadData = async () => {
            if (!productId) {
                setLoading(false);
                return;
            }
            try {
                const [cats, product] = await Promise.all([
                    getAllCategories(),
                    getProductById(productId),
                ]);
                setCategories(cats);
                setInitialValues({
                    title: product.title,
                    description: product.description,
                    price: product.price,
                    images: [],
                    category: product.category,
                });
                setExistingImages(product.images);
            } finally {
                setLoading(false);
            }
        };
        loadData();
    }, [productId]);

    if (!productId) return <Navigate to="/" replace />;
    if (loading || !initialValues) return <div className="loading-state">Loading...</div>;

    const handleUpdate = async (data: ProductUpdateFormValues & { existingImages: string[] }) => {
        setBackendError(undefined);
        try {
            await updateProduct(data, data.existingImages, productId);
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["product", productId] }),
                queryClient.invalidateQueries({ queryKey: ["profile"] }),
            ]);
            navigate(-1);
        } catch (err: any) {
            const status = err?.response?.status;
            if (status === 413) {
                setBackendError("One or more images are too large. Please use images under 5 MB.");
            } else {
                setBackendError(err?.response?.data?.message || "Failed to update product.");
            }
        }
    };

    return (
        <div className="auth-shell">
            <ProductUpdateForm
                categories={categories}
                loading={loading}
                onSubmit={handleUpdate}
                initialValues={initialValues}
                initialExistingImages={existingImages}
                backendError={backendError}
            />
        </div>
    );
};

export default ProductUpdatePage;
