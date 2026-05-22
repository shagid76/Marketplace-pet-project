import { useState, useEffect } from "react";
import { Product } from "../../types/Product/Product";
import AdminActionButton from "./AdminAction/AdminActionButton";
import { getActiveAdminAction } from "../../services/adminService";
import Pagination from "../Pagination/Pagination";
import { formatCategory } from "../../utils/formatCategory";
import "./AdminTable.scss";

interface Props {
    products: Product[];
    page: number;
    totalPages: number;
    setPage: (page: number) => void;
}

interface ActiveAction {
    id: string;
    actionType: "BAN" | "BLOCK";
    expiresAt?: string | null;
}

const ProductTable = ({ products, page, totalPages, setPage }: Props) => {
    const [productActions, setProductActions] = useState<Record<string, ActiveAction>>({});

    useEffect(() => {
        const loadActions = async () => {
            const actions: Record<string, ActiveAction> = {};
            await Promise.all(
                products.map(async (product) => {
                    try {
                        const response = await getActiveAdminAction(product.id, "PRODUCT");
                        const action = response.data;
                        if (action) actions[product.id] = action;
                    } catch {
                        // no active action
                    }
                })
            );
            setProductActions(actions);
        };
        loadActions();
    }, [products]);

    return (
        <div className="admin-table">
            <div className="admin-table__header">
                <h2>Products</h2>
                <small>{products.length} on this page</small>
            </div>

            <div className="table-wrap">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Title</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {products.map((product) => {
                            const action = productActions[product.id];
                            const isBanned = action?.actionType === "BAN";

                            let statusClass = product.inStock ? "status-pill--ok" : "status-pill--info";
                            let statusLabel = product.inStock ? "In stock" : "Sold";
                            if (isBanned) { statusClass = "status-pill--bad"; statusLabel = "Banned"; }

                            return (
                                <tr key={product.id}>
                                    <td><span className="admin-table__id">{product.id.slice(0, 8)}…</span></td>
                                    <td>{product.title}</td>
                                    <td>{formatCategory(product.category)}</td>
                                    <td><span className="admin-table__price">${product.price}</span></td>
                                    <td>
                                        <span className={`status-pill ${statusClass}`}>{statusLabel}</span>
                                    </td>
                                    <td>
                                        {product.inStock ? (
                                            <AdminActionButton
                                                targetId={product.id}
                                                targetType="PRODUCT"
                                                activeAction={action}
                                            />
                                        ) : (
                                            <span className="u-text-muted" style={{ fontSize: "0.75rem" }}>—</span>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
    );
};

export default ProductTable;
