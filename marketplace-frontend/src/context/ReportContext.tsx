import { createContext, useContext, useState, ReactNode } from "react";

type ReportTarget = {
   targetType: "USER" | "PRODUCT" | "REVIEW";
   targetId: string;
};

type ReportContextType = {
   reportTarget: ReportTarget | null;
   openReport: (targetType: ReportTarget["targetType"], targetId: string) => void;
   closeReport: () => void;
};

const ReportContext = createContext<ReportContextType | null>(null);

type Props = {
   children: ReactNode;
};

export const ReportProvider = ({ children }: Props) => {

   const [reportTarget, setReportTarget] =
      useState<ReportTarget | null>(null);

   const openReport = (
      targetType: ReportTarget["targetType"],
      targetId: string
   ) => {
      setReportTarget({ targetType, targetId });
   };

   const closeReport = () => {
      setReportTarget(null);
   };

   return (
      <ReportContext.Provider value={{
         reportTarget,
         openReport,
         closeReport
      }}>
         {children}
      </ReportContext.Provider>
   );
};

export const useReport = () => {

   const context = useContext(ReportContext);

   if (!context) {
      throw new Error("useReport must be used inside ReportProvider");
   }

   return context;
};