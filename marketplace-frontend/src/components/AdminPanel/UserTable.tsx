import { useState, useEffect } from "react";
import { User } from "../../types/User/User";
import AdminActionButton from "./AdminAction/AdminActionButton";
import { getActiveAdminAction } from "../../services/adminService";
import Pagination from "../Pagination/Pagination";
import "./AdminTable.scss";

interface Props {
    users: User[];
    page: number;
    totalPages: number;
    setPage: (page: number) => void;
}

const formatDate = (date?: string) => {
    if (!date) return "—";
    return new Date(date).toLocaleString();
};

const isActive = (date?: string) => {
    if (!date) return false;
    return new Date(date) > new Date();
};

interface ActiveAction {
    id: string;
    actionType: "BAN" | "BLOCK";
    expiresAt?: string | null;
}

const UserTable = ({ users, page, totalPages, setPage }: Props) => {
    const [userActions, setUserActions] = useState<Record<string, ActiveAction>>({});

    useEffect(() => {
        const loadActions = async () => {
            const actions: Record<string, ActiveAction> = {};
            await Promise.all(
                users.map(async (user) => {
                    try {
                        const response = await getActiveAdminAction(user.id, "USER");
                        const action = response.data;
                        if (action) actions[user.id] = action;
                    } catch (err) {
                        console.error("Error fetching active action:", err);
                    }
                })
            );
            setUserActions(actions);
        };
        loadActions();
    }, [users]);

    return (
        <div className="admin-table">
            <div className="admin-table__header">
                <h2>Users</h2>
                <small>{users.length} on this page</small>
            </div>

            <div className="table-wrap">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Username</th>
                            <th>Roles</th>
                            <th>Joined</th>
                            <th>Block</th>
                            <th>Ban</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => {
                            const isBlocked = isActive(user.blockedUntil);
                            const isBanned = user.banned;
                            const activeAction = userActions[user.id] || null;

                            return (
                                <tr key={user.id}>
                                    <td><span className="admin-table__id">{user.id.slice(0, 8)}&#8230;</span></td>
                                    <td>{user.username}</td>
                                    <td>{user.role?.join(", ") || "—"}</td>
                                    <td>{formatDate(user.createdAt)}</td>
                                    <td>
                                        <span className={`status-pill ${isBlocked ? "status-pill--bad" : "status-pill--ok"}`}>
                                            {isBlocked ? "Blocked" : "Active"}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`status-pill ${isBanned ? "status-pill--bad" : "status-pill--ok"}`}>
                                            {isBanned ? "Banned" : "Active"}
                                        </span>
                                    </td>
                                    <td>
                                        <AdminActionButton
                                            targetType="USER"
                                            targetId={user.id}
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

export default UserTable;
