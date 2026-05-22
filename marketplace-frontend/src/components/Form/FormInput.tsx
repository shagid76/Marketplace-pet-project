import { FieldError, UseFormRegister } from "react-hook-form";

interface Props {
    name: string;
    placeholder?: string;
    label?: string;
    type?: string;
    register: UseFormRegister<any>;
    error?: FieldError;
    step?: string;
}

const FormInput = ({ name, placeholder, label, type = "text", register, step, error }: Props) => {
    return (
        <div className={`form-group${error ? " form-group--error" : ""}`}>
            {label && <label htmlFor={name}>{label}</label>}
            <input
                id={name}
                type={type}
                placeholder={placeholder}
                step={step}
                {...register(name)}
            />
            {error && <p>{error.message}</p>}
        </div>
    );
};

export default FormInput;
