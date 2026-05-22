import { useReview } from "../../context/ReviewContext";
import { createOrUpdate, deleteReview, getMyReviewWithId } from "../../services/reviewService";
import { ReviewFormValues } from "../../validation/reviewCreateSchema";
import ReviewForm from "./ReviewForm";
import ConfirmModal from "../ConfirmModal/ConfirmModal";
import { useState, useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";

type ReviewInitialData = {
    id: string;
    description: string;
    rating: number;
} | null;

const ReviewModal: React.FC = () => {
    const { reviewTarget, closeReviewModal } = useReview();
    const queryClient = useQueryClient();
    const [loading, setLoading] = useState(true);
    const [initialData, setInitialData] = useState<ReviewInitialData>(null);
    const [deleting, setDeleting] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);

    useEffect(() => {
        if (!reviewTarget) return;
        const load = async () => {
            try {
                setLoading(true);
                const data = await getMyReviewWithId(reviewTarget.targetId);
                setInitialData(data ?? null);
            } catch {
                setInitialData(null);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [reviewTarget]);

    if (!reviewTarget) return null;

    const invalidateRating = () => {
        if (!reviewTarget) return;
        queryClient.invalidateQueries({ queryKey: ["user", reviewTarget.targetId] });
        queryClient.invalidateQueries({ queryKey: ["profile"] });
    };

    const handleSubmit = async (data: ReviewFormValues) => {
        try {
            await createOrUpdate(data);
            closeReviewModal();
            invalidateRating();
        } catch (err) {
            console.error("Error submitting review:", err);
        }
    };

    const handleDeleteConfirmed = async () => {
        if (!initialData?.id) return;
        setShowConfirm(false);
        try {
            setDeleting(true);
            await deleteReview(initialData.id);
            closeReviewModal();
            invalidateRating();
        } catch (err) {
            console.error("Error deleting review:", err);
        } finally {
            setDeleting(false);
        }
    };

    return (
        <>
            <div className="modal-overlay" onClick={closeReviewModal}>
                <div className="modal" onClick={(e) => e.stopPropagation()}>
                    <button className="modal__close" onClick={closeReviewModal} aria-label="Close">×</button>
                    <h2>{initialData ? "Edit your review" : "Leave a review"}</h2>
                    <p className="u-text-muted u-mb-4">Share your experience to help other buyers.</p>

                    {loading ? (
                        <div className="loading-state">Loading…</div>
                    ) : (
                        <>
                            <ReviewForm
                                targetId={reviewTarget.targetId}
                                initialData={initialData}
                                onSubmit={handleSubmit}
                            />
                            {initialData && (
                                <button
                                    type="button"
                                    className={`btn btn--danger btn--sm u-mt-3${deleting ? " btn--loading" : ""}`}
                                    onClick={() => setShowConfirm(true)}
                                    disabled={deleting}
                                    style={{ marginTop: "0.75rem", width: "100%" }}
                                >
                                    Delete my review
                                </button>
                            )}
                        </>
                    )}
                </div>
            </div>

            {showConfirm && (
                <ConfirmModal
                    message="Are you sure you want to delete your review? This cannot be undone."
                    confirmLabel="Delete"
                    danger
                    onConfirm={handleDeleteConfirmed}
                    onCancel={() => setShowConfirm(false)}
                />
            )}
        </>
    );
};

export default ReviewModal;
