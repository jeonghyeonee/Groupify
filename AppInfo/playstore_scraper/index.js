import gplay from "google-play-scraper";
import fs from "fs";
import { Parser } from "json2csv";

// 플레이스토어의 모든 카테고리
const categories = [
  "APPLICATION",
  "ART_AND_DESIGN",
  "AUTO_AND_VEHICLES",
  "BEAUTY",
  "BOOKS_AND_REFERENCE",
  "BUSINESS",
  "COMICS",
  "COMMUNICATION",
  "DATING",
  "EDUCATION",
  "ENTERTAINMENT",
  "EVENTS",
  "FINANCE",
  "FOOD_AND_DRINK",
  "HEALTH_AND_FITNESS",
  "HOUSE_AND_HOME",
  "LIBRARIES_AND_DEMO",
  "LIFESTYLE",
  "MAPS_AND_NAVIGATION",
  "MEDICAL",
  "MUSIC_AND_AUDIO",
  "NEWS_AND_MAGAZINES",
  "PARENTING",
  "PERSONALIZATION",
  "PHOTOGRAPHY",
  "PRODUCTIVITY",
  "SHOPPING",
  "SOCIAL",
  "SPORTS",
  "TOOLS",
  "TRAVEL_AND_LOCAL",
  "VIDEO_PLAYERS",
  "WEATHER",
  "GAME_ACTION",
  "GAME_ADVENTURE",
  "GAME_ARCADE",
  "GAME_BOARD",
  "GAME_CARD",
  "GAME_CASINO",
  "GAME_CASUAL",
  "GAME_EDUCATIONAL",
  "GAME_MUSIC",
  "GAME_PUZZLE",
  "GAME_RACING",
  "GAME_ROLE_PLAYING",
  "GAME_SIMULATION",
  "GAME_SPORTS",
  "GAME_STRATEGY",
  "GAME_TRIVIA",
  "GAME_WORD",
];

// 컬렉션 리스트
const collections = [
  gplay.collection.TOP_FREE,
  gplay.collection.TOP_PAID,
  gplay.collection.NEW_FREE,
  gplay.collection.NEW_PAID,
  gplay.collection.GROSSING,
  gplay.collection.TRENDING,
];

// 이미 저장된 앱 데이터를 담기 위한 배열
let totalApps = [];

// 앱 상세 정보를 가져와 description과 icon을 포함한 데이터를 수집하는 함수
async function fetchAppDetails(appId) {
  try {
    const appDetails = await gplay.app({ appId });
    return {
      appId: appDetails.appId,
      title: appDetails.title,
      url: appDetails.url,
      score: appDetails.score,
      price: appDetails.price,
      free: appDetails.free,
      category: appDetails.genre,
      installs: appDetails.installs,
      description: appDetails.description, // description 추가
      icon: appDetails.icon, // icon 추가
    };
  } catch (err) {
    console.log(`Error fetching details for appId ${appId}: ${err.message}`);
    return null;
  }
}

// 카테고리와 컬렉션에서 데이터를 가져오는 함수
async function fetchAppsByCategoryAndCollection(
  category,
  collection,
  count = 1000
) {
  let appsInCategory = [];
  let page = 0;

  while (appsInCategory.length < count && totalApps.length < 100000) {
    try {
      let apps = await gplay.list({
        category: category,
        collection: collection,
        num: 1000,
        start: page * 1000,
        country: "us",
        lang: "en",
      });

      // 각 앱의 상세 정보를 가져오기 (description 및 icon 포함)
      for (const app of apps) {
        const appDetails = await fetchAppDetails(app.appId);
        if (appDetails) {
          appsInCategory.push(appDetails);
          totalApps.push(appDetails);
          saveToCSV([appDetails]); // 데이터를 즉시 CSV에 저장
        }
      }

      console.log(
        `Category: ${category}, Collection: ${collection}, Page: ${page}, Total Apps: ${totalApps.length}`
      );
      page += 1;
    } catch (err) {
      console.log(
        `Error fetching data from category ${category}, collection ${collection}: ${err.message}`
      );
      break; // 오류 발생 시 해당 페이지 건너뛰고 다음으로 진행
    }
  }

  return appsInCategory;
}

// CSV로 저장하는 함수
function saveToCSV(data) {
  const fields = [
    "appId",
    "title",
    "url",
    "score",
    "price",
    "free",
    "category",
    "installs",
    "description",
    "icon",
  ]; // icon 필드 추가
  const opts = { fields, header: false }; // 헤더는 처음에만 추가됨
  const json2csvParser = new Parser(opts);

  try {
    const csv = json2csvParser.parse(data);
    // 파일이 이미 있으면 추가 모드로 저장
    fs.appendFileSync("apps_data.csv", csv + "\n");
    console.log("데이터가 CSV 파일에 저장되었습니다.");
  } catch (err) {
    console.error("CSV 저장 중 오류:", err);
  }
}

// 크롤링 시작
async function crawlAllCategoriesAndCollections() {
  for (const category of categories) {
    if (totalApps.length >= 100000) break; // 50,000개 데이터에 도달하면 종료
    for (const collection of collections) {
      if (totalApps.length >= 100000) break;
      await fetchAppsByCategoryAndCollection(category, collection);
    }
  }

  console.log(`총 수집된 앱: ${totalApps.length}`);
}

crawlAllCategoriesAndCollections();
