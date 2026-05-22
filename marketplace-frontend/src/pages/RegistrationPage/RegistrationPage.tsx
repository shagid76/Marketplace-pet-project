import React from "react";
import RegistrationForm from "../../components/RegistrationForm/RegistrationForm";
import { useNavigate } from "react-router-dom";
import { registration } from "../../services/authService";
import { RegistrationFormValues } from "../../validation/registrationSchema";
import { UseFormSetError } from "react-hook-form";

const RegistrationPage: React.FC = () => {
    const navigate = useNavigate();

    const handleRegistration = async (
        data: RegistrationFormValues,
        setError: UseFormSetError<RegistrationFormValues>
    ) => {
        try {
            await registration(data);
            navigate("/login");
        } catch (err: any) {
            const backendMessage = err.response?.data?.message || "";
            const status = err.response?.status;

            if (status === 409) {
                const field = backendMessage.toLowerCase().includes("username")
                    ? "username"
                    : "email";

                setError(field, {
                    type: "manual",
                    message: backendMessage || "This value is already in use",
                });
            } else if (status === 400) {
                console.error("Server validation error:", backendMessage);
                setError("root", {
                    message: "Server-side error while creating your profile. Please try signing in.",
                });
            } else {
                console.error("Registration error:", backendMessage || err);
                setError("root", {
                    message: "Something went wrong. Please try again.",
                });
            }
        }
    };

    return (
        <div className="auth-shell">
            <RegistrationForm onSubmit={handleRegistration} />
        </div>
    );
};

export default RegistrationPage;
