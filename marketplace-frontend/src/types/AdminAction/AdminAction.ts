export type AdminActionType = "BAN" | "BLOCK";

export type AdminActionTargetType = "USER" | "PRODUCT" | "REVIEW";

export type AdminActionTarget = {
    targetId: string;
    targetType: AdminActionTargetType;
    actionType: AdminActionType;
};

export interface ExtendFormValues  {
  newExpiresAt?: string;
  revoke?: boolean;
  reason: string; 
};

export interface ActiveAdminAction {
    id: string;
    actionType: AdminActionType;
    expiresAt?: string | null;
}