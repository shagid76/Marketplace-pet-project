import { createContext, useContext, useState, ReactNode } from "react";

export type ReviewTarget = {
  targetId: string;
};

type ReviewContextType = {

  reviewTarget: ReviewTarget | null;

  openReview: (targetId: string) => void;
  closeReviewModal: () => void;
};

const ReviewContext = createContext<ReviewContextType | null>(null);

type Props = {
  children: ReactNode;
};

export const ReviewProvider = ({ children }: Props) => {

  const [reviewTarget, setReviewTarget] =
    useState<ReviewTarget | null>(null);

  const openReview = (targetId: string) => {
    setReviewTarget({ targetId });
  };

  const closeReviewModal = () => {
    setReviewTarget(null);
  };

  return (
    <ReviewContext.Provider
      value={{
        reviewTarget,
        openReview,
        closeReviewModal
      }}
    >
      {children}
    </ReviewContext.Provider>
  );
};

export const useReview = () => {

  const context = useContext(ReviewContext);

  if (!context) {
    throw new Error("useReview must be used inside ReviewProvider");
  }

  return context;
};