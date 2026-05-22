import { useEffect, useState } from "react";
import ProfileUpdateForm from "../../components/ProfileUpdateForm/ProfileUpdateForm";
import { getMe, update } from "../../services/userService";
import { UserUpdateFormValues } from "../../validation/userUpdateSchema";
import { useNavigate } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";

const ProfileUpdatePage = () => {
    const [initialValues, setInitialValues] = useState<UserUpdateFormValues | null>(null);
    const [currentAvatar, setCurrentAvatar] = useState<string>("");
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    useEffect(() => {
        const loadUser = async () => {
            try {
                const user = await getMe();
                setInitialValues({
                    username: user.username,
                    removeAvatar: false,
                    avatar: undefined,
                });
                setCurrentAvatar(user.avatar);
            } catch (err) {
                console.error("Error loading user:", err);
            } finally {
                setLoading(false);
            }
        };
        loadUser();
    }, []);

    const [backendError, setBackendError] = useState<string | undefined>();

    const handleSubmit = async (data: UserUpdateFormValues) => {
        setBackendError(undefined);
        try {
            await update(data);
            await queryClient.invalidateQueries({ queryKey: ["profile"] });
            navigate("/me");
        } catch (err: any) {
            const status = err?.response?.status;
            if (status === 413) {
                setBackendError("Avatar image is too large. Please choose an image under 5 MB.");
            } else {
                setBackendError(err?.response?.data?.message || "Failed to update profile.");
            }
        }
    };

    if (loading || !initialValues) {
        return <div className="loading-state">Loading...</div>;
    }

    return (
        <div className="auth-shell">
            <ProfileUpdateForm
                initialValues={initialValues}
                onSubmit={handleSubmit}
                currentAvatar={currentAvatar}
                backendError={backendError}
            />
        </div>
    );
};

export default ProfileUpdatePage;
