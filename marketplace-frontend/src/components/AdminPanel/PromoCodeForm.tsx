import React, { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { create, getAll, deactivate, PromoCodeDto, PromoCodeType } from "../../services/promoCodeService";
import { getAllCategories } from "../../services/productService";
import { formatCategory } from "../../utils/formatCategory";
import Pagination from "../Pagination/Pagination";
import "./AdminTable.scss";

const schema = z.object({
    code: z
        .string()
        .min(3, "Min 3 characters")
        .max(32, "Max 32 characters")
        .regex(/^[A-Z0-9_-]+$/, "Only uppercase letters, numbers, _ and -"),
    promoCodeType: z.enum(["PERCENTAGE", "FIXED_AMOUNT"]),
    startAt: z.string().min(1, "Start date required"),
    endAt: z.string().min(1, "End date required"),
    discountValue: z.coerce.number().min(0.01, "Must be greater than 0"),
    maxUsagePerUser: z.coerce.number().int().min(1, "At least 1"),
    requiredProducts: z.coerce.number().int().min(0, "0 or more"),
    requiredPrice: z.coerce.number().min(0, "0 or more"),
    applicableCategories: z.array(z.string()).min(1, "Select at least one category"),
});

type FormValues = z.infer<typeof schema>;

function fmtDate(iso: string): string {
    try { return new Date(iso).toLocaleDateString(); } catch { return iso; }
}

function promoStatus(p: PromoCodeDto): { label: string; cls: string } {
    if (new Date(p.endAt) < new Date()) return { label: "Expired", cls: "status-pill--warn" };
    if (p.active) return { label: "Active", cls: "status-pill--ok" };
    return { label: "Inactive", cls: "status-pill--bad" };
}

const PROMO_PAGE_SIZE = 5;

const PromoCodeForm: React.FC = () => {
    const [promos, setPromos] = useState<PromoCodeDto[]>([]);
    const [promoTotalPages, setPromoTotalPages] = useState(0);
    const [promoPage, setPromoPage] = useState(0);
    const [categories, setCategories] = useState<string[]>([]);
    const [success, setSuccess] = useState(false);
    const [backendError, setBackendError] = useState<string | null>(null);
    const [deactivating, setDeactivating] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        reset,
        watch,
        setValue,
        formState: { errors, isSubmitting },
    } = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: { promoCodeType: "PERCENTAGE", requiredProducts: 0, requiredPrice: 0 },
    });

    const promoType = watch("promoCodeType") as PromoCodeType;
    const selectedCats = watch("applicableCategories") || [];
    const allCatsSelected = categories.length > 0 && selectedCats.length === categories.length;

    const handleToggleAllCategories = () => {
        setValue(
            "applicableCategories",
            allCatsSelected ? [] : [...categories],
            { shouldValidate: true, shouldDirty: true }
        );
    };

    const loadPromos = async (page: number) => {
        try {
            const data = await getAll(page, PROMO_PAGE_SIZE);
            setPromos(data.content);
            setPromoTotalPages(data.totalPages);
        } catch { /* no-op */ }
    };

    useEffect(() => {
        getAllCategories().then(setCategories).catch(() => {});
    }, []);

    useEffect(() => {
        loadPromos(promoPage);
    }, [promoPage]);

    const onSubmit = async (data: FormValues) => {
        setBackendError(null);
        setSuccess(false);
        try {
            await create({
                ...data,
                promoCodeType: data.promoCodeType as PromoCodeType,
                startAt: new Date(data.startAt).toISOString(),
                endAt: new Date(data.endAt).toISOString(),
            });
            setSuccess(true);
            reset({ promoCodeType: "PERCENTAGE", requiredProducts: 0, requiredPrice: 0 });
            setPromoPage(0);
            loadPromos(0);
        } catch (err: any) {
            const data = err?.response?.data;
            let msg = "Failed to create promo code.";
            if (typeof data === "string") {
                msg = data;
            } else if (data?.message) {
                msg = data.message;
            } else if (data?.error) {
                msg = data.error;
            } else if (data?.errors && typeof data.errors === "object" && !Array.isArray(data.errors)) {
                // Spring map: { fieldName: "message", ... }
                msg = Object.values(data.errors).join(", ");
            } else if (Array.isArray(data?.errors)) {
                // Spring array: [{ defaultMessage: "..." }]
                msg = data.errors.map((e: any) => e.defaultMessage || e.message).join(", ");
            }
            setBackendError(msg);
        }
    };

    const handleDeactivate = async (id: string) => {
        setDeactivating(id);
        try {
            await deactivate(id);
            loadPromos(promoPage);
        } catch { /* no-op */ } finally {
            setDeactivating(null);
        }
    };

    return (
        <div className="admin-table">
            <div className="admin-table__header">
                <h2>Promo codes</h2>
                <small>Create &amp; manage discount codes</small>
            </div>

            {success && (
                <div className="promo-admin-banner promo-admin-banner--success">
                    Promo code created successfully!
                </div>
            )}
            {backendError && (
                <div className="promo-admin-banner promo-admin-banner--error">{backendError}</div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} noValidate className="promo-admin-form">

                <div className={"form-group" + (errors.code ? " form-group--error" : "")}>
                    <label htmlFor="pa-code">Code</label>
                    <input
                        id="pa-code"
                        {...register("code")}
                        placeholder="SUMMER20"
                        onChange={e => { e.target.value = e.target.value.toUpperCase(); register("code").onChange(e); }}
                    />
                    {errors.code && <p>{errors.code.message}</p>}
                </div>

                <div className="promo-admin-row">
                    <div className={"form-group" + (errors.promoCodeType ? " form-group--error" : "")}>
                        <label htmlFor="pa-type">Type</label>
                        <select id="pa-type" {...register("promoCodeType")}>
                            <option value="PERCENTAGE">Percentage (%)</option>
                            <option value="FIXED_AMOUNT">Fixed amount ($)</option>
                        </select>
                        {errors.promoCodeType && <p>{errors.promoCodeType.message}</p>}
                    </div>

                    <div className={"form-group" + (errors.discountValue ? " form-group--error" : "")}>
                        <label htmlFor="pa-discount">
                            {promoType === "PERCENTAGE" ? "Discount %" : "Discount $"}
                        </label>
                        <input
                            id="pa-discount"
                            type="number"
                            step="0.01"
                            min="0.01"
                            {...register("discountValue")}
                            placeholder={promoType === "PERCENTAGE" ? "20" : "10.00"}
                        />
                        {errors.discountValue && <p>{errors.discountValue.message}</p>}
                    </div>
                </div>

                <div className="promo-admin-row promo-admin-row--dates">
                    <div className={"form-group" + (errors.startAt ? " form-group--error" : "")}>
                        <label htmlFor="pa-start">Starts at</label>
                        <input id="pa-start" type="datetime-local" {...register("startAt")} />
                        {errors.startAt && <p>{errors.startAt.message}</p>}
                    </div>

                    <div className={"form-group" + (errors.endAt ? " form-group--error" : "")}>
                        <label htmlFor="pa-end">Expires at</label>
                        <input id="pa-end" type="datetime-local" {...register("endAt")} />
                        {errors.endAt && <p>{errors.endAt.message}</p>}
                    </div>

                    <div className="form-group form-group--infinity">
                        <label>&nbsp;</label>
                        <button
                            type="button"
                            className="btn btn--ghost btn--infinity"
                            title="Set start to now and end to year 2099"
                            onClick={() => {
                                const now = new Date();
                                const pad = (n: number) => String(n).padStart(2, "0");
                                const toLocal = (d: Date) =>
                                    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
                                setValue("startAt", toLocal(now), { shouldValidate: true });
                                setValue("endAt", "2099-12-31T23:59", { shouldValidate: true });
                            }}
                        >
                            Never expire
                        </button>
                    </div>
                </div>

                <div className="promo-admin-row">
                    <div className={"form-group" + (errors.maxUsagePerUser ? " form-group--error" : "")}>
                        <label htmlFor="pa-maxuse">Max uses / user</label>
                        <input id="pa-maxuse" type="number" min="1" {...register("maxUsagePerUser")} placeholder="1" />
                        {errors.maxUsagePerUser && <p>{errors.maxUsagePerUser.message}</p>}
                    </div>

                    <div className={"form-group" + (errors.requiredProducts ? " form-group--error" : "")}>
                        <label htmlFor="pa-reqprod">Min products</label>
                        <input id="pa-reqprod" type="number" min="0" {...register("requiredProducts")} placeholder="0" />
                        {errors.requiredProducts && <p>{errors.requiredProducts.message}</p>}
                    </div>

                    <div className={"form-group" + (errors.requiredPrice ? " form-group--error" : "")}>
                        <label htmlFor="pa-reqprice">Min order ($)</label>
                        <input id="pa-reqprice" type="number" step="0.01" min="0" {...register("requiredPrice")} placeholder="0" />
                        {errors.requiredPrice && <p>{errors.requiredPrice.message}</p>}
                    </div>
                </div>

                <div className={"form-group" + (errors.applicableCategories ? " form-group--error" : "")}>
                    <div className="promo-admin-cats-header">
                        <label>Applicable categories</label>
                        {categories.length > 0 && (
                            <button
                                type="button"
                                className="btn btn--ghost btn--sm"
                                onClick={handleToggleAllCategories}
                            >
                                {allCatsSelected ? "Deselect all" : "Select all"}
                            </button>
                        )}
                    </div>
                    <div className="promo-admin-checkboxes">
                        {categories.map(cat => (
                            <label key={cat} className="promo-admin-check">
                                <input type="checkbox" value={cat} {...register("applicableCategories")} />
                                {formatCategory(cat)}
                            </label>
                        ))}
                        {categories.length === 0 && (
                            <span className="u-text-muted" style={{ fontSize: "0.875rem" }}>Loading categories...</span>
                        )}
                    </div>
                    {errors.applicableCategories && <p>{errors.applicableCategories.message}</p>}
                </div>

                <button
                    type="submit"
                    className={"btn" + (isSubmitting ? " btn--loading" : "")}
                    disabled={isSubmitting}
                >
                    Create promo code
                </button>
            </form>

            <div className="promo-admin-list">
                <div className="admin-table__header">
                    <h3 className="promo-admin-list__title">Existing codes</h3>
                    <small>Page {promoPage + 1} of {promoTotalPages || 1}</small>
                </div>

                {promos.length === 0 ? (
                    <p style={{ fontSize: "0.875rem", color: "var(--text-muted, #94a3b8)", padding: "1rem 0" }}>
                        No promo codes yet.
                    </p>
                ) : (
                    <React.Fragment>
                        <div className="table-wrap">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Code</th>
                                        <th>Type</th>
                                        <th>Discount</th>
                                        <th>Starts</th>
                                        <th>Expires</th>
                                        <th>Status</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {promos.map(p => {
                                        const { label, cls } = promoStatus(p);
                                        return (
                                            <tr key={p.id}>
                                                <td><code>{p.code}</code></td>
                                                <td>{p.promoCodeType === "PERCENTAGE" ? "%" : "$"}</td>
                                                <td>
                                                    <span className="admin-table__price">
                                                        {p.promoCodeType === "PERCENTAGE"
                                                            ? p.discountValue + "%"
                                                            : "$" + p.discountValue}
                                                    </span>
                                                </td>
                                                <td>{fmtDate(p.startAt)}</td>
                                                <td>{fmtDate(p.endAt)}</td>
                                                <td>
                                                    <span className={"status-pill " + cls}>{label}</span>
                                                </td>
                                                <td>
                                                    {p.active && (
                                                        <button
                                                            className={"btn btn--sm btn--danger" + (deactivating === p.id ? " btn--loading" : "")}
                                                            onClick={() => handleDeactivate(p.id)}
                                                            disabled={deactivating === p.id}
                                                        >
                                                            Deactivate
                                                        </button>
                                                    )}
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                        <Pagination
                            page={promoPage}
                            totalPages={promoTotalPages}
                            onChange={(p) => setPromoPage(p)}
                        />
                    </React.Fragment>
                )}
            </div>
        </div>
    );
};

export default PromoCodeForm;
