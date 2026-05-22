import React from "react";
import "./BannedItemsAlert.scss";

interface BannedItem {
    id: string;
    title: string;
}

interface Props {
    items: BannedItem[];
    context: "cart" | "wishlist";
    onDismiss: () => void;
}

const BannedItemsAlert: React.FC<Props> = ({ items, context, onDismiss }) => {
    if (items.length === 0) return null;

    return (
        <div className="banned-alert-overlay" onClick={onDismiss}>
            <div className="banned-alert" onClick={(e) => e.stopPropagation()}>
                <div className="banned-alert__icon">🚫</div>

                <h2 className="banned-alert__title">
                    {items.length === 1
                        ? "An item in your " + context + " has been banned"
                        : `${items.length} items in your ${context} have been banned`}
                </h2>

                <p className="banned-alert__desc">
                    Banned items cannot be {context === "cart" ? "purchased" : "moved to cart"}.
                    You can only remove them.
                </p>

                <ul className="banned-alert__list">
                    {items.map((item) => (
                        <li key={item.id} className="banned-alert__list-item">
                            <span className="banned-alert__bullet">🚫</span>
                            {item.title}
                        </li>
                    ))}
                </ul>

                <button className="btn btn--danger btn--block" onClick={onDismiss}>
                    Got it
                </button>
            </div>
        </div>
    );
};

export default BannedItemsAlert;
