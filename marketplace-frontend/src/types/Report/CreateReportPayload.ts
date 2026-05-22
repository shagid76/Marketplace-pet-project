export type CreateReportPayload = {
   targetType: "USER" | "PRODUCT" | "REVIEW";
   targetId: string;
   description: string;
};