import { useReport } from "../../context/ReportContext";
import ReportForm from "./ReportForm";
import { create } from "../../services/reportService";
import { ReportFormValues } from "../../validation/reportCreateSchema";

const ReportModal: React.FC = () => {
    const { reportTarget, closeReport } = useReport();

    if (!reportTarget) return null;

    const handleSubmit = async (data: ReportFormValues) => {
        try {
            await create(data);
            closeReport();
        } catch (err) {
            console.error("Error submitting report:", err);
        }
    };

    return (
        <div className="modal-overlay" onClick={closeReport}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <button className="modal__close" onClick={closeReport} aria-label="Close">×</button>
                <h2>Report</h2>
                <p className="u-text-muted u-mb-4">Tell us what's wrong. We'll review it shortly.</p>
                <ReportForm
                    targetType={reportTarget.targetType}
                    targetId={reportTarget.targetId}
                    onSubmit={handleSubmit}
                />
            </div>
        </div>
    );
};

export default ReportModal;
