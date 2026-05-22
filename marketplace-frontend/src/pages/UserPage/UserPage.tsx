import React from "react";
import { useParams, Navigate, useNavigate } from "react-router-dom";
import UserProfileView from "../../components/UserProfileView/UserProfileView";
import ReportButton from "../../components/Report/ReportButton";
import { getCurrentUserId } from "../../services/authService";
import ReviewCreateButton from "../../components/Review/ReviewCreateButton";
import { create } from "../../services/chatService";
import { useUserData } from "../../hooks/useUserData";

const UserPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const currentUserId = getCurrentUserId();
    const navigate = useNavigate();

    const { user, products, averageRating, isLoading } = useUserData(id);

    const handleOpenChat = async () => {
        if (!user || !currentUserId) return;
        const chat = await create({ user2Id: user.id });
        navigate(`/chat/${chat.id}`);
    };

    if (id && currentUserId && id === currentUserId) {
        return <Navigate to="/me" replace />;
    }

    if (isLoading) return <div className="loading-state">Loading profile…</div>;

    if (!user) {
        return (
            <div className="state-page">
                <h1>User not found</h1>
                <p>This user may have been removed.</p>
            </div>
        );
    }

    const isOwner = currentUserId === user.id;

    return (
        <>
            <UserProfileView
                user={user}
                products={products}
                isOwner={isOwner}
                averageRating={averageRating}
            />
            {!isOwner && currentUserId && (
                <div className="container u-row" style={{ paddingBottom: "3rem" }}>
                    <button className="btn" onClick={handleOpenChat}>Message</button>
                    <ReportButton targetType="USER" targetId={user.id} />
                    <ReviewCreateButton targetId={user.id} />
                </div>
            )}
        </>
    );
};

export default UserPage;
