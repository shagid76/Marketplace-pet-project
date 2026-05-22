import { Component, ReactNode } from "react";

interface Props {
    children: ReactNode;
    fallback?: ReactNode;
}

interface State {
    hasError: boolean;
}

class ErrorBoundary extends Component<Props, State> {
    state: State = { hasError: false };

    static getDerivedStateFromError(): State {
        return { hasError: true };
    }

    componentDidCatch(error: Error, info: React.ErrorInfo) {
        console.error("[ErrorBoundary] Uncaught render error:", error, info.componentStack);
    }

    render() {
        if (this.state.hasError) {
            return this.props.fallback ?? (
                <div className="state-page">
                    <h1>Something went wrong</h1>
                    <p>An unexpected error occurred. Please refresh the page.</p>
                    <button
                        className="btn"
                        onClick={() => {
                            this.setState({ hasError: false });
                            window.location.reload();
                        }}
                    >
                        Refresh
                    </button>
                </div>
            );
        }
        return this.props.children;
    }
}

export default ErrorBoundary;
