import { Report } from "../../types/Report/Report";
import { solve } from "../../services/reportService";
import Pagination from "../Pagination/Pagination";
import "./AdminTable.scss";

interface Props {
    reports: Report[];
    page: number;
    totalPages: number;
    setPage: (page: number) => void;
    onSolve: (id: string) => void;
}

const statusToPill = (status: string) => {
    const s = status.toUpperCase();
    if (s === "RESOLVED" || s === "CLOSED") return "status-pill--ok";
    if (s === "PENDING" || s === "OPEN" || s === "ACTIVE") return "status-pill--warn";
    return "status-pill--info";
};

const ReportTable = ({ reports, page, totalPages, setPage, onSolve }: Props) => {
    return (
        <div className="admin-table">
            <div className="admin-table__header">
                <h2>Reports</h2>
                <small>{reports.length} on this page</small>
            </div>

            <div className="table-wrap">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Reporter ID</th>
                            <th>Description</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {reports.map((report) => (
                            <tr key={report.id}>
                                <td><span className="admin-table__id">{report.id.slice(0, 8)}…</span></td>
                                <td>{report.authorId.slice(0, 8) + "…"}</td>
                                <td>{report.description}</td>
                                <td>
                                    <span className={`status-pill ${statusToPill(report.status)}`}>
                                        {report.status}
                                    </span>
                                </td>
                                <td>
                                    <button
                                        className="btn btn--success btn--sm"
                                        onClick={async () => {
                                            await solve(report.id);
                                            onSolve(report.id);
                                        }}
                                    >
                                        Solve
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
    );
};

export default ReportTable;
