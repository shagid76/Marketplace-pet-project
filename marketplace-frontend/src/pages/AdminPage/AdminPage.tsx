import { useState, useEffect } from "react";
import { getAllUsers } from "../../services/userService";
import { getAllProducts } from "../../services/productService";
import { getAllReviews } from "../../services/reviewService";
import { getAllActiveReports } from "../../services/reportService";
import UserTable from "../../components/AdminPanel/UserTable";
import ProductTable from "../../components/AdminPanel/ProductTable";
import ReviewTable from "../../components/AdminPanel/ReviewTable";
import ReportTable from "../../components/AdminPanel/ReportTable";
import PromoCodeForm from "../../components/AdminPanel/PromoCodeForm";
import { Product } from "../../types/Product/Product";
import { User } from "../../types/User/User";
import { Review } from "../../types/Review/Review";
import { Report } from "../../types/Report/Report";
import "./AdminPage.scss";

const AdminPage: React.FC = () => {
    const [products, setProducts] = useState<Product[]>([]);
    const [users, setUsers] = useState<User[]>([]);
    const [reviews, setReviews] = useState<Review[]>([]);
    const [reports, setReports] = useState<Report[]>([]);

    const [productPage, setProductPage] = useState(0);
    const [productTotalPages, setProductTotalPages] = useState(0);

    const [userPage, setUserPage] = useState(0);
    const [userTotalPages, setUserTotalPages] = useState(0);

    const [reviewPage, setReviewPage] = useState(0);
    const [reviewTotalPages, setReviewTotalPages] = useState(0);

    const [reportPage, setReportPage] = useState(0);
    const [reportTotalPages, setReportTotalPages] = useState(0);
    const [loadingUsers, setLoadingUsers] = useState(true);
    const [loadingProducts, setLoadingProducts] = useState(true);
    const [loadingReviews, setLoadingReviews] = useState(true);
    const [loadingReports, setLoadingReports] = useState(true);

    useEffect(() => {
        const load = async () => {
            try {
                setLoadingUsers(true);
                const data = await getAllUsers(userPage, 5);
                setUsers(data.content);
                setUserTotalPages(data.totalPages);
            } catch (err) {
                console.error("Error loading users:", err);
            } finally {
                setLoadingUsers(false);
            }
        };
        load();
    }, [userPage]);

    useEffect(() => {
        const load = async () => {
            try {
                setLoadingProducts(true);
                const data = await getAllProducts(productPage, 5);
                setProducts(data.content);
                setProductTotalPages(data.totalPages);
            } catch (err) {
                console.error("Error loading products:", err);
            } finally {
                setLoadingProducts(false);
            }
        };
        load();
    }, [productPage]);

    useEffect(() => {
        const load = async () => {
            try {
                setLoadingReviews(true);
                const data = await getAllReviews(reviewPage, 5);
                setReviews(data.content);
                setReviewTotalPages(data.totalPages);
            } catch (err) {
                console.error("Error loading reviews:", err);
            } finally {
                setLoadingReviews(false);
            }
        };
        load();
    }, [reviewPage]);

    useEffect(() => {
        const load = async () => {
            try {
                setLoadingReports(true);
                const data = await getAllActiveReports(reportPage, 5);
                setReports(data.content);
                setReportTotalPages(data.totalPages);
            } catch (err) {
                console.error("Error loading reports:", err);
            } finally {
                setLoadingReports(false);
            }
        };
        load();
    }, [reportPage]);

    useEffect(() => {
        const refetch = async () => {
            try {
                const [usersData, productsData] = await Promise.all([
                    getAllUsers(userPage, 5),
                    getAllProducts(productPage, 5),
                ]);
                setUsers(usersData.content);
                setUserTotalPages(usersData.totalPages);
                setProducts(productsData.content);
                setProductTotalPages(productsData.totalPages);
            } catch (err) {
                console.error("Error refreshing admin data:", err);
            }
        };
        window.addEventListener("admin-action-updated", refetch);
        return () => window.removeEventListener("admin-action-updated", refetch);
    }, [userPage, productPage]);

    const handleSolveReport = (id: string) =>
        setReports((prev) => prev.filter((r) => r.id !== id));

    return (
        <div className="admin">
            <div className="admin__header">
                <h1>Admin panel</h1>
                <p>Manage users, products, reviews, reports and promo codes.</p>
            </div>

            <div className="admin__panel">
                {loadingProducts
                    ? <div className="loading-state">Loading products…</div>
                    : <ProductTable products={products} page={productPage} totalPages={productTotalPages} setPage={setProductPage} />}
            </div>
            <div className="admin__panel">
                {loadingUsers
                    ? <div className="loading-state">Loading users…</div>
                    : <UserTable users={users} page={userPage} totalPages={userTotalPages} setPage={setUserPage} />}
            </div>
            <div className="admin__panel">
                {loadingReports
                    ? <div className="loading-state">Loading reports…</div>
                    : <ReportTable reports={reports} page={reportPage} totalPages={reportTotalPages} setPage={setReportPage} onSolve={handleSolveReport} />}
            </div>
            <div className="admin__panel">
                {loadingReviews
                    ? <div className="loading-state">Loading reviews…</div>
                    : <ReviewTable reviews={reviews} page={reviewPage} totalPages={reviewTotalPages} setPage={setReviewPage} />}
            </div>
            <div className="admin__panel">
                <PromoCodeForm />
            </div>
        </div>
    );
};

export default AdminPage;
