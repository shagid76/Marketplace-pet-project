import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage: React.FC = () => {
    return (
        <div className="state-page">
            <h1>404</h1>
            <p>The page you're looking for doesn't exist or has been moved.</p>
            <Link to="/">
                <button className="btn btn--lg">Back to home</button>
            </Link>
        </div>
    );
};

export default NotFoundPage;
