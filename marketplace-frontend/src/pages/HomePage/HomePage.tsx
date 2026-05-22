import React, { useState } from 'react';
import HomeView from '../../components/HomeView/HomeView';
import { useHomeData } from '../../hooks/useHomeData';

const DEFAULT_CATEGORY = 'ELECTRONICS';

const HomePage: React.FC = () => {
    const [selectedCategory, setSelectedCategory] = useState<string>(DEFAULT_CATEGORY);
    const { newProducts, productsByCategory, categories, isLoading, categoryLoading } = useHomeData(selectedCategory);

    if (isLoading) return <div>Loading...</div>;

    return (
        <div>
            <HomeView
                newProducts={newProducts}
                productsByCategory={productsByCategory}
                categories={categories}
                selectedCategory={selectedCategory}
                onCategoryChange={setSelectedCategory}
                categoryLoading={categoryLoading}
            />
        </div>
    );
};

export default HomePage;
