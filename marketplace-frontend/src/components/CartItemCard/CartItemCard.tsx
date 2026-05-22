import React from "react";

interface CartItemProps {
    id: string;
    title: string;
    price: number;
    isBanned?: boolean;
    onRemove: (id: string) => void;
}

const CartItemCard: React.FC<CartItemProps> = ({ id, title, price, isBanned = false, onRemove }) => {
    return (
        <li className={`cart-item${isBanned ? " cart-item--banned" : ""}`}>
            <div>
                <div className="cart-item__title">
                    {title}
                    {isBanned && <span className="cart-item__banned-badge">Banned</span>}
                </div>
                <div className={`cart-item__price${isBanned ? " cart-item__price--banned" : ""}`}>
                    ${price}
                </div>
            </div>
            <button className="btn btn--ghost btn--sm" onClick={() => onRemove(id)}>
                Remove
            </button>
        </li>
    );
};

export default CartItemCard;
