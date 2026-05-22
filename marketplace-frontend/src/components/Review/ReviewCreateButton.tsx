import { getCurrentUserId } from "../../services/authService";
import { useReview } from "../../context/ReviewContext";

type Props = {
    targetId: string;
};


export default function ReviewCreateButton({targetId}: Props) {
    const { openReview } = useReview();
    const currentUserId = getCurrentUserId();

    if (!currentUserId) return null;
    if (currentUserId === targetId) return null;

    return (
        <button onClick={() => openReview(targetId)}>
            Review
        </button>
    );
}