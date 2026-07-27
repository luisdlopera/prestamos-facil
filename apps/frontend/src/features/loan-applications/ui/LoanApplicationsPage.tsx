"use client";

import { useState, useRef } from "react";
import { LoanApplicationsTable } from "./LoanApplicationsTable";
import { ApproveLoanModal } from "./ApproveLoanModal";
import { RejectLoanModal } from "./RejectLoanModal";
import { LoanApplicationResultModal } from "./LoanApplicationResultModal";
import {
  approveLoanApplication,
  rejectLoanApplication,
  validateLoanApplicationAutomatically,
} from "../infrastructure/loan-applications-api";
import type {
  LoanApplicationDto,
  AutomaticEvaluationResponse,
} from "../infrastructure/loan-applications-api";
import { useAsyncAction } from "@/lib/errors";

export function LoanApplicationsPage({ defaultSearch }: { defaultSearch?: string }) {
  const [selected, setSelected] = useState<LoanApplicationDto | null>(null);
  const [approveOpen, setApproveOpen] = useState(false);
  const [rejectOpen, setRejectOpen] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [resultData, setResultData] = useState<LoanApplicationDto | null>(null);
  const [showResultModal, setShowResultModal] = useState(false);
  const autoValidateAppRef = useRef<LoanApplicationDto | null>(null);

  const { run: doApprove, isLoading: isApproving } = useAsyncAction<LoanApplicationDto>({
    successMsg: "Solicitud aprobada correctamente",
    onSuccess: (result) => {
      setApproveOpen(false);
      setSelected(null);
      setResultData(result);
      setShowResultModal(true);
      setRefreshKey((k) => k + 1);
    },
  });

  const { run: doReject, isLoading: isRejecting } = useAsyncAction<LoanApplicationDto>({
    successMsg: "Solicitud rechazada",
    onSuccess: (result) => {
      setRejectOpen(false);
      setSelected(null);
      setResultData(result);
      setShowResultModal(true);
      setRefreshKey((k) => k + 1);
    },
  });

  const { run: doAutoValidate } = useAsyncAction<AutomaticEvaluationResponse>({
    successMsg: "Validación automática completada",
    onSuccess: (result) => {
      const app = autoValidateAppRef.current;
      if (
        app &&
        (result.decision === "APPROVED" ||
          result.decision === "MANUAL_REVIEW" ||
          result.decision === "REJECTED")
      ) {
        setResultData({
          ...app,
          status: result.decision,
          decisionReason: result.reason ?? app.decisionReason,
        });
        setShowResultModal(true);
      }
      setRefreshKey((k) => k + 1);
    },
  });

  const handleApprove = (app: LoanApplicationDto) => {
    setSelected(app);
    setApproveOpen(true);
  };

  const handleReject = (app: LoanApplicationDto) => {
    setSelected(app);
    setRejectOpen(true);
  };

  const handleAutoValidate = (app: LoanApplicationDto) => {
    autoValidateAppRef.current = app;
    doAutoValidate(() => validateLoanApplicationAutomatically(app.id));
  };

  const handleConfirmApprove = (app: LoanApplicationDto) => {
    doApprove(() => approveLoanApplication(app.id));
  };

  const handleConfirmReject = (app: LoanApplicationDto, reason: string) => {
    doReject(() => rejectLoanApplication(app.id, reason));
  };

  return (
    <>
      <LoanApplicationsTable
        key={refreshKey}
        defaultSearch={defaultSearch}
        onApprove={handleApprove}
        onReject={handleReject}
        onAutoValidate={handleAutoValidate}
      />
      <ApproveLoanModal
        isOpen={approveOpen}
        onOpenChange={setApproveOpen}
        application={selected}
        onConfirm={handleConfirmApprove}
        isProcessing={isApproving}
      />
      <RejectLoanModal
        isOpen={rejectOpen}
        onOpenChange={setRejectOpen}
        application={selected}
        onConfirm={handleConfirmReject}
        isProcessing={isRejecting}
      />
      <LoanApplicationResultModal
        isOpen={showResultModal}
        onOpenChange={setShowResultModal}
        result={resultData}
        onClose={() => {
          setShowResultModal(false);
          setResultData(null);
        }}
      />
    </>
  );
}
