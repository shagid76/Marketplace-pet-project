import FormInput from "../Form/FormInput";
import { UserUpdateFormValues, userUpdateSchema } from "../../validation/userUpdateSchema";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRef, useState, useEffect } from "react";
import "./ProfileUpdateForm.scss";

interface Props {
    onSubmit: (values: UserUpdateFormValues) => void;
    initialValues: UserUpdateFormValues;
    currentAvatar: string;
    backendError?: string;
}

const DEFAULT_AVATAR = "http://localhost:9000/marketplace-images/default-avatar.png";

const ProfileUpdateForm = ({ onSubmit, initialValues, currentAvatar, backendError }: Props) => {
    const {
        register,
        handleSubmit,
        setValue,
        watch,
        formState: { errors, isSubmitting },
    } = useForm<UserUpdateFormValues>({
        resolver: zodResolver(userUpdateSchema),
        defaultValues: { ...initialValues, removeAvatar: false },
    });

    const avatarFile = watch("avatar");
    const removeAvatar = watch("removeAvatar");
    const [previewUrl, setPreviewUrl] = useState<string>(currentAvatar);
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (avatarFile instanceof File) {
            const url = URL.createObjectURL(avatarFile);
            setPreviewUrl(url);
            return () => URL.revokeObjectURL(url);
        }
        setPreviewUrl(removeAvatar ? DEFAULT_AVATAR : currentAvatar);
    }, [avatarFile, currentAvatar, removeAvatar]);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files) return;
        setValue("avatar", e.target.files[0], { shouldValidate: true, shouldDirty: true });
        setValue("removeAvatar", false);
    };

    const handleRemoveNewAvatar = () => {
        setValue("avatar", undefined, { shouldValidate: true, shouldDirty: true });
        if (fileInputRef.current) fileInputRef.current.value = "";
    };

    const handleRemoveCurrentAvatar = () => {
        setValue("avatar", undefined, { shouldValidate: true, shouldDirty: true });
        setValue("removeAvatar", true, { shouldValidate: true, shouldDirty: true });
    };

    const isDefault = currentAvatar === DEFAULT_AVATAR;
    const showRemoveCurrent = !isDefault && !avatarFile && !removeAvatar;
    const showRemoveNew = !!avatarFile;

    return (
        <div className="profile-form-card">
            <h2 className="profile-form-card__title">Edit profile</h2>

            {backendError && (
                <div className="profile-form-card__error">{backendError}</div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} noValidate>
                <div className="avatar-editor">
                    <div className="avatar-editor__wrap">
                        <img
                            src={previewUrl || DEFAULT_AVATAR}
                            alt="Avatar preview"
                            className="avatar-editor__img"
                        />
                        <label className="avatar-editor__pick" htmlFor="avatar-input" title="Change photo">
                            <span>Change photo</span>
                            <input
                                id="avatar-input"
                                ref={fileInputRef}
                                type="file"
                                accept="image/*"
                                onChange={handleFileChange}
                            />
                        </label>
                    </div>

                    <div className="avatar-editor__actions">
                        <label htmlFor="avatar-input" className="btn btn--secondary btn--sm">
                            {avatarFile ? "Change photo" : "Upload photo"}
                        </label>
                        {showRemoveNew && (
                            <button
                                type="button"
                                className="btn btn--ghost btn--sm"
                                onClick={handleRemoveNewAvatar}
                            >
                                Cancel
                            </button>
                        )}
                        {showRemoveCurrent && (
                            <button
                                type="button"
                                className="btn btn--ghost btn--sm u-text-danger"
                                onClick={handleRemoveCurrentAvatar}
                            >
                                Remove photo
                            </button>
                        )}
                    </div>

                    <p className="avatar-editor__hint">JPG, PNG or WEBP</p>

                    {errors.avatar && (
                        <p className="avatar-editor__hint u-text-danger">
                            {errors.avatar.message as string}
                        </p>
                    )}
                </div>

                <FormInput
                    name="username"
                    type="text"
                    label="Username"
                    placeholder="your-handle"
                    register={register}
                    error={errors.username}
                />

                <button
                    type="submit"
                    className={"btn btn--block btn--lg" + (isSubmitting ? " btn--loading" : "")}
                    disabled={isSubmitting}
                >
                    Save changes
                </button>
            </form>
        </div>
    );
};

export default ProfileUpdateForm;
