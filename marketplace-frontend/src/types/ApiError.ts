export type ApiErrorType = "validation" | "unknown";

export interface ApiError {
  type: ApiErrorType;
  message?: string;
  field?: string;
}