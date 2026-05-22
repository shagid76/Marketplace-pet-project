import { useState } from "react";

interface Props {
    page: number;
    totalPages: number;
    onChange: (page: number) => void;
}

const Pagination = ({ page, totalPages, onChange }: Props) => {
    const [inputVal, setInputVal] = useState("");

    if (totalPages <= 1) return null;

    const handleJump = () => {
        const num = parseInt(inputVal, 10);
        if (!isNaN(num) && num >= 1 && num <= totalPages) {
            onChange(num - 1);
        }
        setInputVal("");
    };

    return (
        <div className="pagination">
            <button
                className="btn btn--secondary btn--sm"
                onClick={() => onChange(0)}
                disabled={page === 0}
                title="First page"
            >
                &laquo;
            </button>

            <button
                className="btn btn--secondary btn--sm"
                onClick={() => onChange(page - 1)}
                disabled={page === 0}
            >
                &larr; Prev
            </button>

            <span className="pagination__info">
                Page {page + 1} of {totalPages}
            </span>

            <button
                className="btn btn--secondary btn--sm"
                onClick={() => onChange(page + 1)}
                disabled={page + 1 >= totalPages}
            >
                Next &rarr;
            </button>

            <button
                className="btn btn--secondary btn--sm"
                onClick={() => onChange(totalPages - 1)}
                disabled={page + 1 >= totalPages}
                title="Last page"
            >
                &raquo;
            </button>

            <div className="pagination__jump">
                <input
                    type="number"
                    className="pagination__jump-input"
                    min={1}
                    max={totalPages}
                    placeholder="Page #"
                    value={inputVal}
                    onChange={e => setInputVal(e.target.value)}
                    onKeyDown={e => e.key === "Enter" && handleJump()}
                />
                <button
                    className="btn btn--secondary btn--sm"
                    onClick={handleJump}
                    disabled={!inputVal.trim()}
                >
                    Go
                </button>
            </div>
        </div>
    );
};

export default Pagination;
