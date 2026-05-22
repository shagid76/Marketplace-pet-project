import React from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Pagination from "../../components/Pagination/Pagination";

// Visibility
describe("Pagination - visibility", () => {
    it("renders nothing when totalPages is 1", () => {
        const { container } = render(
            <Pagination page={0} totalPages={1} onChange={jest.fn()} />
        );
        expect(container.firstChild).toBeNull();
    });

    it("renders nothing when totalPages is 0", () => {
        const { container } = render(
            <Pagination page={0} totalPages={0} onChange={jest.fn()} />
        );
        expect(container.firstChild).toBeNull();
    });

    it("renders navigation when totalPages is greater than 1", () => {
        render(<Pagination page={0} totalPages={3} onChange={jest.fn()} />);
        expect(screen.getByText(/Prev/i)).toBeInTheDocument();
        expect(screen.getByText(/Next/i)).toBeInTheDocument();
    });
});

// Page info label
describe("Pagination - page label", () => {
    it("shows the correct current page and total", () => {
        render(<Pagination page={1} totalPages={5} onChange={jest.fn()} />);
        expect(screen.getByText("Page 2 of 5")).toBeInTheDocument();
    });

    it("shows Page 1 on the first page", () => {
        render(<Pagination page={0} totalPages={4} onChange={jest.fn()} />);
        expect(screen.getByText("Page 1 of 4")).toBeInTheDocument();
    });
});

// Disabled states
describe("Pagination - disabled states", () => {
    it("disables Prev and First on the first page", () => {
        render(<Pagination page={0} totalPages={3} onChange={jest.fn()} />);
        expect(screen.getByTitle("First page")).toBeDisabled();
        expect(screen.getByText(/Prev/i)).toBeDisabled();
    });

    it("disables Next and Last on the last page", () => {
        render(<Pagination page={2} totalPages={3} onChange={jest.fn()} />);
        expect(screen.getByText(/Next/i)).toBeDisabled();
        expect(screen.getByTitle("Last page")).toBeDisabled();
    });

    it("enables all buttons on a middle page", () => {
        render(<Pagination page={1} totalPages={3} onChange={jest.fn()} />);
        expect(screen.getByTitle("First page")).not.toBeDisabled();
        expect(screen.getByText(/Prev/i)).not.toBeDisabled();
        expect(screen.getByText(/Next/i)).not.toBeDisabled();
        expect(screen.getByTitle("Last page")).not.toBeDisabled();
    });
});

// onClick callbacks
describe("Pagination - navigation clicks", () => {
    it("calls onChange with page + 1 when Next is clicked", async () => {
        const onChange = jest.fn();
        render(<Pagination page={1} totalPages={5} onChange={onChange} />);
        await userEvent.click(screen.getByText(/Next/i));
        expect(onChange).toHaveBeenCalledWith(2);
    });

    it("calls onChange with page - 1 when Prev is clicked", async () => {
        const onChange = jest.fn();
        render(<Pagination page={2} totalPages={5} onChange={onChange} />);
        await userEvent.click(screen.getByText(/Prev/i));
        expect(onChange).toHaveBeenCalledWith(1);
    });

    it("calls onChange with 0 when First is clicked", async () => {
        const onChange = jest.fn();
        render(<Pagination page={3} totalPages={5} onChange={onChange} />);
        await userEvent.click(screen.getByTitle("First page"));
        expect(onChange).toHaveBeenCalledWith(0);
    });

    it("calls onChange with totalPages - 1 when Last is clicked", async () => {
        const onChange = jest.fn();
        render(<Pagination page={0} totalPages={5} onChange={onChange} />);
        await userEvent.click(screen.getByTitle("Last page"));
        expect(onChange).toHaveBeenCalledWith(4);
    });
});

// Jump to page
describe("Pagination - jump to page", () => {
    it("calls onChange with correct 0-based page when Go is clicked", async () => {
        const onChange = jest.fn();
        render(<Pagination page={0} totalPages={10} onChange={onChange} />);
        await userEvent.type(screen.getByPlaceholderText("Page #"), "7");
        await userEvent.click(screen.getByText("Go"));
        expect(onChange).toHaveBeenCalledWith(6);
    });

    it("does not call onChange when Go is clicked with empty input", async () => {
        const onChange = jest.fn();
        render(<Pagination page={0} totalPages={10} onChange={onChange} />);
        await userEvent.click(screen.getByText("Go"));
        expect(onChange).not.toHaveBeenCalled();
    });

    it("calls onChange when Enter is pressed in the jump input", async () => {
        const onChange = jest.fn();
        render(<Pagination page={0} totalPages={10} onChange={onChange} />);
        await userEvent.type(screen.getByPlaceholderText("Page #"), "3{Enter}");
        expect(onChange).toHaveBeenCalledWith(2);
    });
});
