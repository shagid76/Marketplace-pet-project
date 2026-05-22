import { useState, useEffect } from "react";
import { Review } from "../../types/Review/Review";
import AdminActionButton from "./AdminAction/AdminActionButton";
import { getActiveAdminAction } from "../../services/adminService";
import { ActiveAdminAction } from "../../types/AdminAction/AdminAction";
import Pagination from "../Pagination/Pagination";
import "./AdminTable.scss";

interface Props {
    reviews: Review[];
    page: number;
    totalPages: number;
    setPage: (page: number) => void;
}

const formatDate = (date?: string) => {
    if (!date) return "—";
    return new Date(date).toLocaleString();
};

const ReviewTable = ({ reviews, page, totalPages, setPage }: Props) => {
    const [reviewActions, setReviewActions] = useState<Record<string, ActiveAdminAction>>({});

    useEffect(() => {
        const loadActions = async () => {
            const actions: Record<string, ActiveAdminAction> = {};
            await Promise.all(
                reviews.map(async (review) => {
                    try {
                        const response = await getActiveAdminAction(review.id, "REVIEW");
                        const action = response.data;
                        if (action) actions[review.id] = action;
                    } catch {
                        // no active action
                    }
                })
            );
            setReviewActions(actions);
        };
        loadActions();
    }, [reviews]);

    return (
        <div className="admin-table">
            <div className="admin-table__header">
                <h2>Reviews</h2>
                <small>{reviews.length} on this page</small>
            </div>

            <div className="table-wrap">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Author</th>
                            <th>Description</th>
                            <th>Rating</th>
                            <th>Updated</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {reviews.map((review) => {
                            const activeAction = reviewActions[review.id] || null;
                            const isBanned = activeAction?.actionType === "BAN";

                            const statusClass = isBanned ? "status-pill--bad" : "status-pill--ok";
                            const statusLabel = isBanned ? "Banned" : "Active";

                            return (
                                <tr key={review.id}>
                                    <td><span className="admin-table__id">{review.id.slice(0, 8)}…</span></td>
                                    <td>{review.authorUsername}</td>
                                    <td>
                                        <div className="admin-table__description">{review.description}</div>
                                    </td>
                                    <td>
                                        <span className="status-pill status-pill--info">★ {review.rating}</span>
                                    </td>
                                    <td>{formatDate(review.lastUpdated)}</td>
                                    <td>
                                        <span className={`status-pill ${statusClass}`}>{statusLabel}</span>
                                    </td>
                                    <td>
                                        <AdminActionButton
                                            targetType="REVIEW"
                                            targetId={review.id}
                                            activeAction={activeAction}
                                        />
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

export default ReviewTable;
