import { useState, useCallback } from "react";
import { Route, Routes } from "react-router-dom";
import ReportModal from "../components/Report/ReportModal";
import ReviewModal from "../components/Review/ReviewModal";
import Header from "../components/Header/Header";
import Footer from "../components/Footer/Footer";
import AdminPage from "../pages/AdminPage/AdminPage";
import CartPage from "../pages/CartPage/CartPage";
import HomePage from "../pages/HomePage/HomePage";
import LoginPage from "../pages/LoginPage/LoginPage";
import NotFoundPage from "../pages/NotFoundPage/NotFoundPage";
import ProductPage from "../pages/ProductPage/ProductPage";
import ProfilePage from "../pages/ProfilePage/ProfilePage";
import RegistrationPage from "../pages/RegistrationPage/RegistrationPage";
import SearchResultPage from "../pages/SearchResultPage/SearchResultPage";
import ProductCreatePage from "../pages/ProductCreatePage/ProductCreatePage";
import ProfileUpdatePage from "../pages/ProfileUpdatePage/ProfileUpdatePage";
import ProductUpdatePage from "../pages/ProductUpdatePage/ProductUpdatePage";
import UserPage from "../pages/UserPage/UserPage";
import AdminActionModal from "../components/AdminPanel/AdminAction/AdminActionModal";
import ProtectedRoute from "../components/ProtectedRoute/ProtectedRoute";
import GuestRoute from "../components/ProtectedRoute/GuestRoute";
import ChatPage from "../pages/ChatPage/ChatPage";
import BannedAccountModal from "../components/BannedAccountModal/BannedAccountModal";
import ErrorBoundary from "../components/ErrorBoundary/ErrorBoundary";
import { useSseEvents } from "../hooks/useSseEvents";
import { useAccountStatusPoll } from "../hooks/useAccountStatusPoll";

const App = () => {
    const [accountAction, setAccountAction] = useState<{ type: "banned" | "blocked"; message: string } | null>(null);

    const handleAcknowledge = useCallback(() => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
    }, []);

    // Functional update: whichever fires first wins; the other is ignored
    const handleBanned = useCallback((message: string) => {
        setAccountAction((prev) => prev ?? { type: "banned", message });
    }, []);
    const handleBlocked = useCallback((message: string) => {
        setAccountAction((prev) => prev ?? { type: "blocked", message });
    }, []);

    // Primary: SSE — instant detection the moment admin bans/blocks
    useSseEvents({ onBanned: handleBanned, onBlocked: handleBlocked });

    // Fallback: polling — catches cases where SSE connection silently dropped
    useAccountStatusPoll({ onBanned: handleBanned, onBlocked: handleBlocked });

    return (
        <>
            {accountAction && (
                <BannedAccountModal
                    type={accountAction.type}
                    message={accountAction.message}
                    onAcknowledge={handleAcknowledge}
                />
            )}

            <Header />
            <main>
                <ErrorBoundary>
                    <Routes>
                        <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MODERATOR']} />}>
                            <Route path="/admin-page" element={<AdminPage />} />
                        </Route>
                        <Route element={<ProtectedRoute />}>
                            <Route path="/update" element={<ProfileUpdatePage />} />
                            <Route path="/create-product" element={<ProductCreatePage />} />
                            <Route path="/update-product" element={<ProductUpdatePage />} />
                            <Route path="/cart" element={<CartPage />} />
                            <Route path="/chat" element={<ChatPage />} />
                            <Route path="/chat/:chatId" element={<ChatPage />} />
                            <Route path="/me" element={<ProfilePage />} />
                        </Route>
                        <Route
                            path="/forbidden"
                            element={
                                <div className="state-page">
                                    <h1>403</h1>
                                    <p>You don't have permission to view this page.</p>
                                </div>
                            }
                        />
                        <Route path="/user/:id" element={<UserPage />} />
                        <Route path="/product/:id" element={<ProductPage />} />
                        <Route path="/" element={<HomePage />} />
                        <Route element={<GuestRoute />}>
                            <Route path="/login" element={<LoginPage />} />
                            <Route path="/registration" element={<RegistrationPage />} />
                        </Route>
                        <Route path="/search" element={<SearchResultPage />} />
                        <Route path="*" element={<NotFoundPage />} />
                    </Routes>
                </ErrorBoundary>
            </main>
            <ReportModal />
            <ReviewModal />
            <AdminActionModal />
            <Footer />
        </>
    );
};

export default App;
