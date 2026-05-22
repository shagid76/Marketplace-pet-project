import { LoginFormValues, loginSchema } from "../../validation/loginSchema";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import FormInput from "../Form/FormInput";
import { Link } from "react-router-dom";

interface Props {
    onSubmit: (data: LoginFormValues) => void;
    title?: string;
    backendError?: string;
}

const LoginForm = ({ onSubmit, title = "Welcome back", backendError }: Props) => {
    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) });

    return (
        <div className="auth-card">
            <h2>{title}</h2>
            <p className="auth-card__subtitle">Sign in to continue to your marketplace</p>

            {backendError && <div className="auth-card__error">{backendError}</div>}

            <form onSubmit={handleSubmit(onSubmit)} noValidate>
                <FormInput
                    name="email"
                    type="email"
                    label="Email"
                    placeholder="you@example.com"
                    register={register}
                    error={errors.email}
                />
                <FormInput
                    name="password"
                    type="password"
                    label="Password"
                    placeholder="••••••••"
                    register={register}
                    error={errors.password}
                />
                <button
                    type="submit"
                    className={`btn btn--block btn--lg${isSubmitting ? " btn--loading" : ""}`}
                    disabled={isSubmitting}
                >
                    Sign in
                </button>
            </form>

            <div className="auth-card__footer">
                Don&apos;t have an account? <Link to="/registration">Create one</Link>
            </div>
        </div>
    );
};

export default LoginForm;
