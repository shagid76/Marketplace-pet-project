import { createProduct } from "../../services/productService";
import ProductCreateForm from "../../components/ProductCreateForm/ProductCreateForm";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { getAllCategories } from "../../services/productService";
import { ProductCreateFormValues } from "../../validation/productCreateSchema";
import { useQueryClient } from "@tanstack/react-query";

const ProductCreatePage = () => {
    const [categories, setCategories] = useState<string[]>([]);
    const [loading, setLoading] = useState(true);
    const [backendError, setBackendError] = useState<string | undefined>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    useEffect(() => {
        getAllCategories()
            .then(data => setCategories(data))
            .finally(() => setLoading(false));
    }, []);

    const handleCreate = async (data: ProductCreateFormValues) => {
        setBackendError(undefined);
        try {
            await createProduct(data);
            await queryClient.invalidateQueries({ queryKey: ["profile"] });
            navigate("/me");
        } catch (err: any) {
            const status = err?.response?.status;
            if (status === 413) {
                setBackendError("One or more images are too large. Please use images under 5 MB.");
            } else {
                setBackendError(err?.response?.data?.message || "Failed to create product.");
            }
        }
    };

    return (
        <div className="auth-shell">
            <ProductCreateForm
                categories={categories}
                loading={loading}
                onSubmit={handleCreate}
                backendError={backendError}
            />
        </div>
    );
};

export default ProductCreatePage;
