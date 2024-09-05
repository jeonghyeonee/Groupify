import gplay from 'google-play-scraper';
import { createObjectCsvWriter as createCsvWriter } from 'csv-writer';

// CSV 파일을 생성하는 함수
const createCsvFile = async (collection, fileName) => {
    const csvWriter = createCsvWriter({
        path: fileName, // 생성할 CSV 파일 경로
        header: [
            { id: 'rank', title: 'Rank' },
            { id: 'appId', title: 'App ID' },
            { id: 'name', title: 'Name' },
            { id: 'category', title: 'Category' },
            { id: 'description', title: 'Description' },
            { id: 'icon', title: 'Icon URL' } // 아이콘 URL을 위한 필드 추가
        ]
    });

    try {
        const apps = await gplay.list({
            collection: collection, // 지정된 컬렉션 사용
            num: 100, // 가져올 앱의 수
            lang: 'ko', // 언어 (한국어로 설정)
            country: 'kr' // 국가 코드 (한국)
        });

        const records = [];

        for (const [index, app] of apps.entries()) {
            try {
                const appDetails = await gplay.app({ appId: app.appId });

                records.push({
                    rank: index + 1,
                    appId: appDetails.appId,
                    name: appDetails.title,
                    category: appDetails.genre,
                    description: appDetails.description,
                    icon: appDetails.icon // icon URL 추가
                });
            } catch (error) {
                if (error.status === 404) {
                    console.error(`App not found (404) for app ID: ${app.appId}. Skipping...`);
                } else {
                    console.error(`Error fetching details for app ID: ${app.appId}:`, error);
                }
                continue; // 에러 발생 시 해당 앱 건너뛰기
            }
        }

        if (records.length > 0) {
            await csvWriter.writeRecords(records); // 데이터를 CSV 파일로 기록
            console.log(`The CSV file "${fileName}" was written successfully`);
        } else {
            console.log(`No valid records to write for "${fileName}".`);
        }
    } catch (error) {
        console.error(`Error writing CSV file "${fileName}":`, error);
    }
};

// 각각의 컬렉션에 대해 CSV 파일 생성
createCsvFile(gplay.collection.TOP_FREE, 'Free_apps.csv');
createCsvFile(gplay.collection.TOP_PAID, 'Paid_apps.csv');
createCsvFile(gplay.collection.TOP_GROSSING, 'Grossing_apps.csv');
