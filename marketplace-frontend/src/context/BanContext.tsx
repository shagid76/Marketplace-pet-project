import React, { createContext, useContext, useMemo, useState } from "react";

type TargetType = "USER" | "PRODUCT" | "REVIEW";
type ActionType = "BAN" | "BLOCK";

type CreateModalState = {
  kind: "create";
  targetId: string;
  targetType: TargetType;
  actionType: ActionType;
};

type EditBlockModalState = {
  kind: "edit-block";
  targetId: string;
  targetType: TargetType;
  adminActionId: string;
  expiresAt?: string | null;
};

type RevokeModalState = {
  kind: "revoke";
  adminActionId: string;
  actionType: ActionType;
};

type ModalState = CreateModalState | EditBlockModalState | RevokeModalState | null;

type BanContextValue = {
  modal: ModalState;
  openCreateBan: (payload: { targetId: string; targetType: TargetType }) => void;
  openCreateBlock: (payload: { targetId: string; targetType: TargetType }) => void;
  openEditBlock: (payload: {
    targetId: string;
    targetType: TargetType;
    adminActionId: string;
    expiresAt?: string | null;
  }) => void;
  openRevoke: (payload: { adminActionId: string; actionType: ActionType }) => void;
  closeAll: () => void;
};

const BanContext = createContext<BanContextValue | null>(null);

export function BanProvider({ children }: { children: React.ReactNode }) {
  const [modal, setModal] = useState<ModalState>(null);

  const value = useMemo<BanContextValue>(
    () => ({
      modal,
      openCreateBan: ({ targetId, targetType }) =>
        setModal({
          kind: "create",
          targetId,
          targetType,
          actionType: "BAN",
        }),
      openCreateBlock: ({ targetId, targetType }) =>
        setModal({
          kind: "create",
          targetId,
          targetType,
          actionType: "BLOCK",
        }),
      openEditBlock: ({ targetId, targetType, adminActionId, expiresAt }) =>
        setModal({
          kind: "edit-block",
          targetId,
          targetType,
          adminActionId,
          expiresAt,
        }),
      openRevoke: ({ adminActionId, actionType }) =>
        setModal({
          kind: "revoke",
          adminActionId,
          actionType,
        }),
      closeAll: () => setModal(null),
    }),
    [modal]
  );

  return <BanContext.Provider value={value}>{children}</BanContext.Provider>;
}

export function useBan() {
  const ctx = useContext(BanContext);
  if (!ctx) {
    throw new Error("useBan must be used inside BanProvider");
  }
  return ctx;
}