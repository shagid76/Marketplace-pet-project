export const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080";
export const API_ROOT = `${API_BASE.replace(/\/$/, "")}/api`;
