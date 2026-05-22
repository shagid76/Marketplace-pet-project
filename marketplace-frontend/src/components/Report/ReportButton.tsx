import { useReport } from "../../context/ReportContext";
import { getCurrentUserId } from "../../services/authService";

type Props = {
    targetType: "USER" | "PRODUCT" | "REVIEW";
    targetId: string;
    ownerId?: string;
};

export default function ReportButton({ targetType, targetId, ownerId }: Props) {
    const { openReport } = useReport();
    const currentUserId = getCurrentUserId();

    if (!currentUserId) return null;
    if (targetId === currentUserId) return null;
    if (ownerId && ownerId === currentUserId) return null;

    return (
        <button onClick={() => openReport(targetType, targetId)}>
            Report
        </button>
    );
}
