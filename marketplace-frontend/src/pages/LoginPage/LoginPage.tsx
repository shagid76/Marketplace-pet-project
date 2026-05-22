import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../../services/authService";
import LoginForm from "../../components/LoginForm/LoginForm";
import { LoginFormValues } from "../../validation/loginSchema";

const LoginPage: React.FC = () => {
    const navigate = useNavigate();
    const [backendError, setBackendError] = useState<string | undefined>();

    const handleLogin = async (data: LoginFormValues) => {
        setBackendError(undefined);
        try {
            await login(data);
            navigate("/me");
        } catch (err: any) {
            const msg = err?.response?.data?.message;
            setBackendError(msg || "Invalid email or password.");
        }
    };

    return (
        <div className="auth-shell">
            <LoginForm onSubmit={handleLogin} backendError={backendError} />
        </div>
    );
};

export default LoginPage;
