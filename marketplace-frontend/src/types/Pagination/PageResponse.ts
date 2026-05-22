export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
}