import { RegistrationFormValues, registrationSchema } from "../../validation/registrationSchema";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import FormInput from "../Form/FormInput";
import { UseFormSetError } from "react-hook-form";
import { Link } from "react-router-dom";

interface Props {
    onSubmit: (data: RegistrationFormValues, setError: UseFormSetError<RegistrationFormValues>) => void;
    title?: string;
}

const RegistrationForm = ({ onSubmit, title = "Create your account" }: Props) => {
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<RegistrationFormValues>({ resolver: zodResolver(registrationSchema) });

    const rootError = errors.root?.message;

    return (
        <div className="auth-card">
            <h2>{title}</h2>
            <p className="auth-card__subtitle">Join the marketplace in less than a minute</p>

            {rootError && <div className="auth-card__error">{rootError}</div>}

            <form onSubmit={handleSubmit((data) => onSubmit(data, setError))} noValidate>
                <FormInput
                    name="email"
                    type="email"
                    label="Email"
                    placeholder="you@example.com"
                    register={register}
                    error={errors.email}
                />
                <FormInput
                    name="username"
                    type="text"
                    label="Username"
                    placeholder="your-handle"
                    register={register}
                    error={errors.username}
                />
                <FormInput
                    name="password"
                    type="password"
                    label="Password"
                    placeholder="At least 8 characters"
                    register={register}
                    error={errors.password}
                />
                <button
                    type="submit"
                    className={`btn btn--block btn--lg${isSubmitting ? " btn--loading" : ""}`}
                    disabled={isSubmitting}
                >
                    Create account
                </button>
            </form>

            <div className="auth-card__footer">
                Already have an account? <Link to="/login">Sign in</Link>
            </div>
        </div>
    );
};

export default RegistrationForm;
