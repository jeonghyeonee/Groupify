import express from 'express';
import gplay from 'google-play-scraper';

const app = express();
const port = 3000;

app.use(express.json());

const getAppCategory = async (packageName) => {
    try {
        const app = await gplay.app({ appId: packageName });
        return app.genre;
    } catch (error) {
        console.error(`Error fetching category for package ${packageName}:`, error);
        return 'Unknown';
    }
};

app.post('/getAppCategories', async (req, res) => {
    const { packageNames } = req.body;

    if (!packageNames || !Array.isArray(packageNames)) {
        return res.status(400).json({ error: 'Invalid input' });
    }

    const results = {};
    for (const packageName of packageNames) {
        const category = await getAppCategory(packageName);
        results[packageName] = category;
    }

    res.json(results);
});

app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
