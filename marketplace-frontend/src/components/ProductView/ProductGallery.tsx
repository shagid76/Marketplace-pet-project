import { useState, useCallback, useEffect } from "react";

interface Props {
    images: string[];
    title: string;
}

const ProductGallery: React.FC<Props> = ({ images, title }) => {
    const [lightboxIndex, setLightboxIndex] = useState<number | null>(null);
    const isOpen = lightboxIndex !== null;

    const close = () => setLightboxIndex(null);

    const prev = useCallback(() => {
        setLightboxIndex((i) => (i === null ? 0 : (i - 1 + images.length) % images.length));
    }, [images.length]);

    const next = useCallback(() => {
        setLightboxIndex((i) => (i === null ? 0 : (i + 1) % images.length));
    }, [images.length]);

    useEffect(() => {
        if (!isOpen) return;
        const onKey = (e: KeyboardEvent) => {
            if (e.key === "ArrowLeft") prev();
            else if (e.key === "ArrowRight") next();
            else if (e.key === "Escape") close();
        };
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [isOpen, prev, next]);

    return (
        <>
            <div className="product-detail__gallery">
                {images.map((img, index) => (
                    <img
                        key={img}
                        src={img}
                        alt={`${title} ${index + 1}`}
                        loading="lazy"
                        className="product-detail__gallery-img"
                        onClick={() => setLightboxIndex(index)}
                        title="Click to enlarge"
                    />
                ))}
            </div>

            {isOpen && lightboxIndex !== null && (
                <div className="lightbox" onClick={close}>
                    <button className="lightbox__close" onClick={close} aria-label="Close">&#215;</button>

                    {images.length > 1 && (
                        <button
                            className="lightbox__arrow lightbox__arrow--prev"
                            onClick={(e) => { e.stopPropagation(); prev(); }}
                            aria-label="Previous image"
                        >
                            &#8249;
                        </button>
                    )}

                    <div className="lightbox__content" onClick={(e) => e.stopPropagation()}>
                        <img src={images[lightboxIndex]} alt={`${title} ${lightboxIndex + 1}`} />
                        {images.length > 1 && (
                            <p className="lightbox__counter">{lightboxIndex + 1} / {images.length}</p>
                        )}
                    </div>

                    {images.length > 1 && (
                        <button
                            className="lightbox__arrow lightbox__arrow--next"
                            onClick={(e) => { e.stopPropagation(); next(); }}
                            aria-label="Next image"
                        >
                            &#8250;
                        </button>
                    )}
                </div>
            )}
        </>
    );
};

export default ProductGallery;
