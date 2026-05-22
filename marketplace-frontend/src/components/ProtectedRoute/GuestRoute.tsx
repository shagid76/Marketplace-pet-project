import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

const GuestRoute = () => {
    const { isAuthenticated } = useAuth();

    if (isAuthenticated === null) {
        return <div>Loading...</div>;
    }

    if (isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};

export default GuestRoute;
