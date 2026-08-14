let globalLat = null;
let globalLon = null;

const weatherTypes = {
    0: {
        ico: "ico/gg/sunny.webp",
        ico_night: "ico/gg/clear_night.webp",
        en: "Clear sky",
        ru: "Ясно",
    },
    1: {
        ico: "ico/gg/mostly_sunny.webp",
        ico_night: "ico/gg/mostly_clear_night.webp",
        en: "Mainly clear",
        ru: "Преимущественно солнечно",
    },
    2: {
        ico: "ico/gg/partly_cloudy.webp",
        ico_night: "ico/gg/partly_cloudy_night.webp",
        en: "Partly cloudy",
        ru: "Частично облачно",
    },
    3: {
        ico: "ico/gg/cloudy.webp",
        ico_night: "ico/gg/mostly_cloudy_night.webp",
        en: "Overcast",
        ru: "Пасмурно",
    },
    45: {
        ico: "ico/gg/haze_fog_dust_smoke.webp",
        en: "Fog",
        ru: "Туман",
    },
    48: {
        ico: "ico/gg/haze_fog_dust_smoke.webp",
        en: "Fog",
        ru: "Холодный туман",
    },
    51: {
        ico: "ico/gg/drizzle.webp",
        en: "Sunny",
        ru: "Легко моросит",
    },
    53: {
        ico: "ico/gg/showers_rain.webp",
        en: "Sunny",
        ru: "Умеренно моросит",
    },
    55: {
        ico: "ico/gg/scattered_showers_day.webp",
        ico_night: "ico/gg/scattered_showers_night.webp",
        en: "Sunny",
        ru: "Сильно моросит",
    },
    56: {
        ico: "ico/gg/freezing_rain.webp",
        en: "Freezing rain",
        ru: "Морозный дождь",
    },
    57: {
        ico: "ico/gg/freezing_rain.webp",
        en: "Heavy freezing rain",
        ru: "Сильный морозный дождь",
    },
    61: {
        ico: "ico/gg/showers_rain.webp",
        en: "Low rain",
        ru: "Слабый дождь",
    },
    63: {
        ico: "ico/gg/heavy_rain.webp",
        en: "Moderete rain",
        ru: "Дождь",
    },
    65: {
        ico: "ico/gg/heavy_rain.webp",
        en: "Heavy rain",
        ru: "Сильный дождь",
    },
    66: {
        ico: "ico/gg/freezing_rain.webp",
        en: "Freezing rain",
        ru: "Морозный дождь",
    },
    67: {
        ico: "ico/gg/freezing_rain.webp",
        en: "Heavy freezing rain",
        ru: "Сильный морозный дождь",
    },
    71: {
        ico: "ico/gg/light_snow.webp",
        en: "Light snowfall",
        ru: "Лёгкий снегопад",
    },
    73: {
        ico: "ico/gg/moderate_snow.webp",
        en: "Snowfall",
        ru: "Снегопад",
    },
    75: {
        ico: "ico/gg/heavy_snow.webp",
        en: "Heavy snowfall",
        ru: "Сильный снегопад",
    },
    77: {
        ico: "ico/gg/snow_showers_snow.webp",
        en: "Snow grains",
        ru: "Снежная крупа",
    },
    80: {
        ico: "ico/gg/showers_rain.webp",
        en: "Light rain showers",
        ru: "Лёгкий ливень",
    },
    81: {
        ico: "ico/gg/heavy_rain_showers.webp",
        en: "Rain showers",
        ru: "Ливень",
    },
    82: {
        ico: "ico/gg/extreme_rain_showers.webp",
        en: "Heavy rain showers",
        ru: "Сильный ливень",
    },
    85: {
        ico: "ico/gg/snow_showers_snow.webp",
        en: "Snow showers",
        ru: "Метель",
    },
    86: {
        ico: "ico/gg/snow_showers_snow.webp",
        en: "Heavy snow showers",
        ru: "Сильная метель",
    },
    95: {
        ico: "ico/gg/isolated_scattered_tstorms_day.webp",
        ico_night: "ico/gg/isolated_scattered_tstorms_night.webp",
        en: "Thunderstorm",
        ru: "Гроза",
    },
    96: {
        ico: "ico/gg/isolated_scattered_tstorms_day.webp",
        ico_night: "ico/gg/isolated_scattered_tstorms_night.webp",
        en: "Thunderstorm with slight hail",
        ru: "Гроза с небольшим градом",
    },
    99: {
        ico: "ico/gg/strong_tstorms.webp",
        en: "Thunderstorm with heavy hail",
        ru: "гроза с сильным градом",
    },
}

const dirrections = {
    N: {
        ru: "Северный",
        en: "Northern"
    },
    NE: {
        ru: "Северовосточный",
        en: "North-Eastrn"
    },
    E: {
        ru: "Восточный",
        en: "Easten"
    },
    SE: {
        ru: "Юговосточный",
        en: "South-Eastern"
    },
    S: {
        ru: "Южный",
        en: "Southern"
    },
    SW: {
        ru: "Югозападный",
        en: "South-Western"
    },
    W: {
        ru: "Западный",
        en: "Western"
    },
    NW: {
        ru: "Северозападный",
        en: "North-Western"
    }
}

function returnDirrection(l) {
    return(dirrections[l][isRuLang ? "ru" : "en"])
}

function weatherIcon(weather_code, is_day) {
    return(weatherTypes[weather_code].ico_night && is_day == 0 ? weatherTypes[weather_code].ico_night : weatherTypes[weather_code].ico)
}

const getShortLang = () => {
    const lang = navigator.language || "en"; // по умолчанию английский
    return lang.split('-')[0]; // обрезаем "ru-RU" до "ru"
};

const currentLang = getShortLang();

if (currentLang === 'ru') {
    console.log("Привет мир!");
} else {
    console.log("Hello world!");
}
const isRuLang = (currentLang == 'ru' || currentLang == 'uk' || currentLang == 'by' || currentLang == 'kz') ? true : false;

/* ---------- FETCH ---------- */

async function fetchFromFile() {
    if (window.bridge.env != "Android") return

    return(JSON.parse(await window.bridge.readFile('savedWeather.json')))
}

async function fetchWeather(lat, lon) {
    const url =
        `https://api.open-meteo.com/v1/forecast` +
        `?latitude=${lat}&longitude=${lon}&timeformat=unixtime` +
        `&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,wind_speed_10m,wind_direction_10m,precipitation,weather_code,cloud_cover,surface_pressure` +
        `&hourly=temperature_2m,weather_code,precipitation_probability,precipitation` +
        `&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_hours` +
        `&timezone=auto&forecast_days=7`;
        
    if (!navigator.onLine) {
        const saved = localStorage.getItem("WeatherAll");
        return saved ? JSON.parse(saved) : null;
    }

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error(res.status);
        const data = await res.json();
        localStorage.setItem("WeatherAll", JSON.stringify(data));
        return data;
    } catch {
        const saved = localStorage.getItem("WeatherAll");
        return saved ? JSON.parse(saved) : null;
    }
}

/* ---------- RENDER ---------- */

async function updateAll() {
    if (globalLat == null || globalLon == null) return;

    const savedData = await fetchFromFile()
    let data
    if (savedData && ((new Date).getTime() - savedData.current.time * 1000 < 300000 || !navigator.onLine)) {
        data = savedData
        window.notification.success('saved')
    } else {
        data = await fetchWeather(globalLat, globalLon)
        window.bridge.writeFile('savedWeather.json', JSON.stringify(data, null, 2))
    }
    if (!data) return

    // текущая погода
    NOWrenderCurrent(data.current, data)

    renderImportantTimes(data.hourly)

    renderHourly(data.hourly)
    renderImportantTimes(data.hourly)

    renderHourly(data.hourly)
    renderDaily(data.daily)
}

/* ---------- IMPORTANT TIMES ---------- */

function renderImportantTimes(hourly) {
    const settings = settingsManager ? settingsManager.get('weather') : {};
    const container = document.getElementById('important-times-weather');
    
    if (!settings.showImportantTimes) {
        container.style.display = 'none';
        return;
    }

    const importantTimesSettings = settingsManager ? settingsManager.get('importantTimes') : {};
    const times = [
        importantTimesSettings.time1,
        importantTimesSettings.time2,
        importantTimesSettings.time3
    ].filter(t => t && t.match(/^\d{1,2}:\d{2}$/));

    if (times.length === 0) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'flex';
    container.innerHTML = '';

    times.forEach(timeStr => {
        const [hour, minute] = timeStr.split(':').map(Number);
        
        // Найти ближайший временной слот в почасовом прогнозе
        let matchingIndex = -1;
        
        hourly.time.forEach((t, i) => {
            const forecastHour = new Date(t * 1000).getHours();
            const forecastMinute = new Date(t * 1000).getMinutes();
            
            if (forecastHour === hour && forecastMinute === minute) {
                matchingIndex = i;
            }
        });

        if (matchingIndex === -1) {
            // Если точного совпадения нет, ищем ближайший час
            hourly.time.forEach((t, i) => {
                const forecastHour = new Date(t * 1000).getHours();
                
                if (forecastHour === hour && matchingIndex === -1) {
                    matchingIndex = i;
                }
            });
        }

        if (matchingIndex !== -1) {
            const card = document.createElement('div');
            card.className = 'important-time-card';
            
            const temp = Math.round(hourly.temperature_2m[matchingIndex]);
            const weatherCode = hourly.weather_code[matchingIndex];
            const weatherIconThis = weatherIcon(weatherCode, hour < 6 || hour > 19 ? 0 : 1) || 'ico/gg/sunny.webp';
            
            card.innerHTML = `
                <div class="time">${timeStr}</div>
                <img src="${weatherIconThis}" alt="weather">
                <div class="temperature">${temp}°C</div>
            `;
            
            container.appendChild(card);
        }
    });
}

/* ---------- HOURLY ---------- */

function renderHourly(hourly) {
    const container = document.getElementById("hours-container");
    if (!container) return;

    container.innerHTML = "";
    const now = Date.now();

    hourly.time.slice(0, 59).forEach((t, i) => {
        const ts = new Date(t * 1000).getTime();
        if (ts < now) return;

        const hour = new Date(t * 1000).getHours().toString().padStart(2, "0");
        const day = new Date(t * 1000).getDate();
        const month = new Date(t * 1000).getMonth();
        const card = document.createElement("div");
        card.className = `card time-${hour}`;
        card.innerHTML = `
            <!-- p class="c-time c-time-day">${day != new Date().getDate() ? day : ''}${month != new Date().getMonth() ? '.' : ''}${month != new Date().getMonth() ? month + 1 : ''}</p-->
            <p class="c-time">${hour}:00</p>
            <img class="c-img" src="${weatherIcon(hourly.weather_code[i], hour < 6 || hour > 19 ? 0 : 1)}">
            <p class="c-temp">${Math.round(hourly.temperature_2m[i])}°C</p>
        `;
        container.appendChild(card);
    });
}

/* ---------- DAILY ---------- */

function renderDaily(daily) {
    const container = document.getElementById("days-container");
    if (!container) return;

    container.innerHTML = "";
    let days = ["ВС","ПН","ВТ","СР","ЧТ","ПТ","СБ"];
    if (!isRuLang) days = ["SUN","MON","TUE","WED","THU","FRI","SAT"]

    daily.time.forEach((t, i) => {
        const card = document.createElement("div");
        card.className = "card";
        card.innerHTML = `
            <p class="c-time">${days[new Date(t * 1000).getDay()]}</p>
            <img class="c-img" src="${weatherIcon(daily.weather_code[i], 1)}">
            <p class="c-temp">
                ${Math.round(daily.temperature_2m_min[i])}° 
                <br>–<br>
                ${Math.round(daily.temperature_2m_max[i])}°
            </p>
        `;
        container.appendChild(card);
    });
}

function getAppSettings() {
    let settings = JSON.parse(localStorage.getItem("appSettings"));

    if (!settings || typeof settings !== "object") {
        settings = {
            weather: {
                townName: null
            }
        };
        localStorage.setItem("appSettings", JSON.stringify(settings));
    }

    if (!settings.weather) {
        settings.weather = { townName: null };
        localStorage.setItem("appSettings", JSON.stringify(settings));
    }

    return settings;
}

/* ---------- CUSTOM DROPDOWN ---------- */

document.addEventListener("DOMContentLoaded", () => {
    const container = document.createElement('div')
    container.id = 'city-select-custom'
    document.querySelector('#home .page-header').appendChild(container)
    //const container = document.getElementById("city-select-custom");

    const cities = [
        // ПМР
        { value: 'Tiraspol', label: '[ПМР] Тирасполь', img: '../../home/img/flag/md.svg', lat: 46.8406, lon: 29.4744 },
        { value: 'Bender', label: '[ПМР] Бендеры', img: '../../home/img/flag/md.svg', lat: 46.1833, lon: 29.0833 },
        { value: 'Rîbnița', label: '[ПМР] Рыбница', img: '../../home/img/flag/md.svg', lat: 47.7686, lon: 29.0000 },
        { value: 'Dubăsari', label: '[ПМР] Дубоссары', img: '../../home/img/flag/md.svg', lat: 47.2656, lon: 29.1667 },

        // Молдова
        { value: 'Chișinău', label: '[Молд] Кишинэу', img: '../../home/img/flag/md.svg', lat: 47.0105, lon: 28.8638 },
        { value: 'Bălți', label: '[Молд] Бэлць', img: '../../home/img/flag/md.svg', lat: 47.7530, lon: 27.9184 },
        { value: 'Cahul', label: '[Молд] Кахул', img: '../../home/img/flag/md.svg', lat: 45.9075, lon: 28.1944 },

        // Украина
        { value: 'Kyiv', label: '[Укр] Київ', img: '../../home/img/flag/ua.svg', lat: 50.4501, lon: 30.5236 },
        { value: 'Brovary', label: '[Укр] Бровари', img: '../../home/img/flag/ua.svg', lat: 50.5110, lon: 30.7909 },
        { value: 'Kharkiv', label: '[Укр] Харків', img: '../../home/img/flag/ua.svg', lat: 49.9935, lon: 36.2304 },
        { value: 'Dnipro', label: '[Укр] Дніпропетровськ', img: '../../home/img/flag/ua.svg', lat: 48.4647, lon: 35.0462 },
        { value: 'Donetsk', label: '[Укр] Донецьк', img: '../../home/img/flag/ua.svg', lat: 48.0159, lon: 37.8029 },
        { value: 'Luhansk', label: '[Укр] Сталіно', img: '../../home/img/flag/ua.svg', lat: 48.5740, lon: 39.3078 },
        { value: 'Zaporizhzhia', label: '[Укр] Запоріжжя', img: '../../home/img/flag/ua.svg', lat: 47.8388, lon: 35.1396 },
        { value: 'Odesa', label: '[Укр] Одеса', img: '../../home/img/flag/ua.svg', lat: 46.4825, lon: 30.7233 },
        { value: 'Mykolaiv', label: '[Укр] Миколаїв', img: '../../home/img/flag/ua.svg', lat: 46.9750, lon: 31.9946 },
        { value: 'Kherson', label: '[Укр] Херсон', img: '../../home/img/flag/ua.svg', lat: 46.6354, lon: 32.6169 },
        { value: 'Lviv', label: '[Укр] Львів', img: '../../home/img/flag/ua.svg', lat: 49.8397, lon: 24.0297 },
        { value: 'Poltava', label: '[Укр] Полтава', img: '../../home/img/flag/ua.svg', lat: 49.5883, lon: 34.5514 },
        { value: 'Chernihiv', label: '[Укр] Чернигів', img: '../../home/img/flag/ua.svg', lat: 51.4982, lon: 31.2893 },
        { value: 'Kryvyi Rih', label: '[Укр] Кривий Ріг', img: '../../home/img/flag/ua.svg', lat: 47.9105, lon: 33.3918 },
        { value: 'Makeevka', label: '[Укр] Макеївка', img: '../../home/img/flag/ua.svg', lat: 48.0708, lon: 37.9578 },
        { value: 'Vinnytsia', label: '[Укр] Винниця', img: '../../home/img/flag/ua.svg', lat: 49.2331, lon: 28.4682 },
        { value: 'Khmelnytskyi', label: '[Укр] Хмельницький', img: '../../home/img/flag/ua.svg', lat: 49.4229, lon: 26.9871 },
        { value: 'Chernivtsi', label: '[Укр] Черновці', img: '../../home/img/flag/ua.svg', lat: 48.2917, lon: 25.9352 },
        { value: 'Zhytomyr', label: '[Укр] Житомир', img: '../../home/img/flag/ua.svg', lat: 50.2546, lon: 28.6587 },
        { value: 'Sumy', label: '[Укр] Суми', img: '../../home/img/flag/ua.svg', lat: 50.9077, lon: 34.7981 },
        { value: 'Kropyvnytskyi', label: '[Укр] Кировоград', img: '../../home/img/flag/ua.svg', lat: 48.5079, lon: 32.2623 },
        { value: 'Ternopil', label: '[Укр] Тернопіль', img: '../../home/img/flag/ua.svg', lat: 49.5535, lon: 25.5948 },
        { value: 'Ivano-Frankivsk', label: '[Укр] Івано-Франківськ', img: '../../home/img/flag/ua.svg', lat: 48.9226, lon: 24.7111 },
        { value: 'Lutsk', label: '[Укр] Луцьк', img: '../../home/img/flag/ua.svg', lat: 50.7472, lon: 25.3254 },
        { value: 'Uzhhorod', label: '[Укр] Ужгород', img: '../../home/img/flag/ua.svg', lat: 48.6208, lon: 22.2879 },
        { value: 'Rivne', label: '[Укр] Рівне', img: '../../home/img/flag/ua.svg', lat: 50.6199, lon: 26.2516 },

        // Крым
        { value: 'Simferopol', label: '[Крым] Симферополь', img: '../../home/img/flag/ru.svg', lat: 44.9521, lon: 34.1024 },
        { value: 'Sevastopol', label: '[Крым] Севастополь', img: '../../home/img/flag/ru.svg', lat: 44.6167, lon: 33.5254 },
        { value: 'Yalta', label: '[Крым] Ялта', img: '../../home/img/flag/ru.svg', lat: 44.4952, lon: 34.1663 },
        { value: 'Kerch', label: '[Крым] Керчь', img: '../../home/img/flag/ru.svg', lat: 45.3567, lon: 36.4744 },

        // Россия
        { value: 'Moscow', label: '[Рос] Москва', img: '../../home/img/flag/ru.svg', lat: 55.7558, lon: 37.6173 },
        { value: 'Saint Petersburg', label: '[Рос] Ленинград', img: '../../home/img/flag/ru.svg', lat: 59.9343, lon: 30.3351 },
        { value: 'Gorky', label: '[Рос] Горький', img: '../../home/img/flag/ru.svg', lat: 56.2965, lon: 43.9361 },
        { value: 'Sverdlovsk', label: '[Рос] Свердловск', img: '../../home/img/flag/ru.svg', lat: 56.8389, lon: 60.6057 },
        { value: 'Kuybyshev', label: '[Рос] Куйбышев', img: '../../home/img/flag/ru.svg', lat: 53.1959, lon: 50.1002 },
        { value: 'Volgograd', label: '[Рос] Сталинград', img: '../../home/img/flag/ru.svg', lat: 48.7080, lon: 44.5133 },
        { value: 'Kazan', label: '[Рос] Казань', img: '../../home/img/flag/ru.svg', lat: 55.7961, lon: 49.1064 },
        { value: 'Chelyabinsk', label: '[Рос] Челябинск', img: '../../home/img/flag/ru.svg', lat: 55.1644, lon: 61.4368 },
        { value: 'Perm', label: '[Рос] Молотов', img: '../../home/img/flag/ru.svg', lat: 58.0105, lon: 56.2502 },
        { value: 'Novosibirsk', label: '[Рос] Новосибирск', img: '../../home/img/flag/ru.svg', lat: 55.0084, lon: 82.9357 },
        { value: 'Omsk', label: '[Рос] Омск', img: '../../home/img/flag/ru.svg', lat: 54.9885, lon: 73.3242 },
        { value: 'Krasnoyarsk', label: '[Рос] Красноярск', img: '../../home/img/flag/ru.svg', lat: 56.0153, lon: 92.8932 },
        { value: 'Irkutsk', label: '[Рос] Иркутск', img: '../../home/img/flag/ru.svg', lat: 52.2871, lon: 104.2810 },
        { value: 'Vladivostok', label: '[Рос] Владивосток', img: '../../home/img/flag/ru.svg', lat: 43.1155, lon: 131.8855 },
        { value: 'Rostov-on-Don', label: '[Рос] Ростов-на-Дону', img: '../../home/img/flag/ru.svg', lat: 47.2224, lon: 39.7189 },
        { value: 'Ufa', label: '[Рос] Уфа', img: '../../home/img/flag/ru.svg', lat: 54.7355, lon: 55.9587 },
        { value: 'Krasnodar', label: '[Рос] Краснодар', img: '../../home/img/flag/ru.svg', lat: 45.0355, lon: 38.9753 },
        { value: 'Voronezh', label: '[Рос] Воронеж', img: '../../home/img/flag/ru.svg', lat: 51.6608, lon: 39.2003 },
        { value: 'Saratov', label: '[Рос] Саратов', img: '../../home/img/flag/ru.svg', lat: 51.5336, lon: 46.0343 },
        { value: 'Tolyatti', label: '[Рос] Тольятти', img: '../../home/img/flag/ru.svg', lat: 53.5078, lon: 49.4204 },
        { value: 'Izhevsk', label: '[Рос] Ижевск', img: '../../home/img/flag/ru.svg', lat: 56.8498, lon: 53.2045 },
        { value: 'Yaroslavl', label: '[Рос] Ярославль', img: '../../home/img/flag/ru.svg', lat: 57.6261, lon: 39.8845 },
        { value: 'Barnaul', label: '[Рос] Барнаул', img: '../../home/img/flag/ru.svg', lat: 53.3548, lon: 83.7698 },
        { value: 'Vladikavkaz', label: '[Рос] Орджоникидзе', img: '../../home/img/flag/ru.svg', lat: 43.0259, lon: 44.6353 },
        { value: 'Murmansk', label: '[Рос] Мурманск', img: '../../home/img/flag/ru.svg', lat: 68.9707, lon: 33.0759 },
        { value: 'Tula', label: '[Рос] Тула', img: '../../home/img/flag/ru.svg', lat: 54.1930, lon: 37.6178 },
        { value: 'Kemerovo', label: '[Рос] Кемерово', img: '../../home/img/flag/ru.svg', lat: 55.3547, lon: 86.0873 },

        // Казахстан
        { value: 'Almaty', label: '[Каз] Алма-Ата', img: '../../home/img/flag/kz.svg', lat: 43.2389, lon: 76.8897 },
        { value: 'Astana', label: '[Каз] Целиноград', img: '../../home/img/flag/kz.svg', lat: 51.1694, lon: 71.4491 },
        { value: 'Karaganda', label: '[Каз] Караганда', img: '../../home/img/flag/kz.svg', lat: 49.8060, lon: 73.0850 },
        { value: 'Pavlodar', label: '[Каз] Павлодар', img: '../../home/img/flag/kz.svg', lat: 52.2873, lon: 76.9674 },
        { value: 'Ust-Kamenogorsk', label: '[Каз] Усть-Каменогорск', img: '../../home/img/flag/kz.svg', lat: 49.9483, lon: 82.6285 },
        { value: 'Shymkent', label: '[Каз] Чимкент', img: '../../home/img/flag/kz.svg', lat: 42.3150, lon: 69.5850 },
        { value: 'Semipalatinsk', label: '[Каз] Семипалатинск', img: '../../home/img/flag/kz.svg', lat: 50.4230, lon: 80.2434 },
        { value: 'Uralsk', label: '[Каз] Уральск', img: '../../home/img/flag/kz.svg', lat: 51.2278, lon: 51.3866 },
        { value: 'Petropavlovsk', label: '[Каз] Петропавловск', img: '../../home/img/flag/kz.svg', lat: 54.8752, lon: 69.1628 },
        { value: 'Aktobe', label: '[Каз] Актюбинск', img: '../../home/img/flag/kz.svg', lat: 50.3004, lon: 57.1546 },

        // Беларусь
        { value: 'Minsk', label: '[Бел] Минск', img: '../../home/img/flag/by.svg', lat: 53.9000, lon: 27.5667 },
        { value: 'Gomel', label: '[Бел] Гомель', img: '../../home/img/flag/by.svg', lat: 52.4443, lon: 30.9754 },
        { value: 'Mogilev', label: '[Бел] Могилёв', img: '../../home/img/flag/by.svg', lat: 53.9168, lon: 30.3449 },
        { value: 'Vitebsk', label: '[Бел] Витебск', img: '../../home/img/flag/by.svg', lat: 55.1848, lon: 30.2016 },
        { value: 'Grodno', label: '[Бел] Гродно', img: '../../home/img/flag/by.svg', lat: 53.6694, lon: 23.8131 },
        { value: 'Brest', label: '[Бел] Брест', img: '../../home/img/flag/by.svg', lat: 52.0976, lon: 23.7341 },
        { value: 'Bobruysk', label: '[Бел] Бобруйск', img: '../../home/img/flag/by.svg', lat: 53.1384, lon: 29.2214 },
        { value: 'Baranovichi', label: '[Бел] Барановичи', img: '../../home/img/flag/by.svg', lat: 53.1323, lon: 26.0139 },
        { value: 'Borisov', label: '[Бел] Борисов', img: '../../home/img/flag/by.svg', lat: 54.2244, lon: 28.5108 },

        // Кавказ
        { value: 'Tbilisi', label: '[GEO] თბილისი', img: '../../home/img/flag/ge.svg', lat: 41.7151, lon: 44.8271 },
        { value: 'Kutaisi', label: '[GEO] ქუთაისი', img: '../../home/img/flag/ge.svg', lat: 42.2679, lon: 42.6946 },
        { value: 'Yerevan', label: '[ARM] Երևան', img: '../../home/img/flag/am.svg', lat: 40.1792, lon: 44.4991 },
        { value: 'Gyumri', label: '[ARM] Լենինական', img: '../../home/img/flag/am.svg', lat: 40.7894, lon: 43.8470 },
        { value: 'Baku', label: '[AZE] Bakı', img: '../../home/img/flag/az.svg', lat: 40.4093, lon: 49.8671 },
        { value: 'Ganja', label: '[AZE] Kirovabad', img: '../../home/img/flag/az.svg', lat: 40.6828, lon: 46.3606 },

        // Прибалтика
        { value: 'Riga', label: '[LVA] Rīga', img: '../../home/img/flag/lv.svg', lat: 56.9496, lon: 24.1052 },
        { value: 'Daugavpils', label: '[LVA] Daugavpils', img: '../../home/img/flag/lv.svg', lat: 55.8740, lon: 26.5362 },
        { value: 'Vilnius', label: '[LTU] Vilnius', img: '../../home/img/flag/lt.svg', lat: 54.6872, lon: 25.2797 },
        { value: 'Kaunas', label: '[LTU] Kaunas', img: '../../home/img/flag/lt.svg', lat: 54.8985, lon: 23.9036 },
        { value: 'Tallinn', label: '[EST] Tallinn', img: '../../home/img/flag/ee.svg', lat: 59.4370, lon: 24.7536 },
        { value: 'Tartu', label: '[EST] Tartu', img: '../../home/img/flag/ee.svg', lat: 58.3776, lon: 26.7290 },

        // Центральная Азия
        { value: 'Tashkent', label: '[UZB] Toshkent', img: '../../home/img/flag/uz.svg', lat: 41.2995, lon: 69.2401 },
        { value: 'Samarkand', label: '[UZB] Samarqand', img: '../../home/img/flag/uz.svg', lat: 39.6542, lon: 66.9597 },
        { value: 'Dushanbe', label: '[TJK] Душанбе', img: '../../home/img/flag/tj.svg', lat: 38.5598, lon: 68.7870 },
        { value: 'Khujand', label: '[TJK] Ленинабад', img: '../../home/img/flag/tj.svg', lat: 40.2823, lon: 69.6223 },
        { value: 'Ashgabat', label: '[TKM] Aşgabat', img: '../../home/img/flag/tm.svg', lat: 37.9601, lon: 58.3261 },
        { value: 'Turkmenabat', label: '[TKM] Türkmenabat', img: '../../home/img/flag/tm.svg', lat: 39.0767, lon: 63.6105 },
        { value: 'Bishkek', label: '[KGZ] Фрунзе', img: '../../home/img/flag/kg.svg', lat: 42.8746, lon: 74.5698 },
        { value: 'Osh', label: '[KGZ] Ош', img: '../../home/img/flag/kg.svg', lat: 40.5138, lon: 72.8161 },

        // Европа
        { value: 'Warsaw', label: '[POL] Warszawa', img: '../../home/img/flag/pl.svg', lat: 52.2297, lon: 21.0122 },
        { value: 'Krakow', label: '[POL] Kraków', img: '../../home/img/flag/pl.svg', lat: 50.0647, lon: 19.9450 },
        { value: 'Prague', label: '[CZE] Praha', img: '../../home/img/flag/cz.svg', lat: 50.0755, lon: 14.4378 },
        { value: 'Brno', label: '[CZE] Brno', img: '../../home/img/flag/cz.svg', lat: 49.1951, lon: 16.6068 },
        { value: 'Bratislava', label: '[SVK] Bratislava', img: '../../home/img/flag/cz.svg', lat: 48.1486, lon: 17.1077 },
        { value: 'Kosice', label: '[SVK] Košice', img: '../../home/img/flag/cz.svg', lat: 48.7164, lon: 21.2611 },
        { value: 'Budapest', label: '[HUN] Budapest', img: '../../home/img/flag/hu.svg', lat: 47.4979, lon: 19.0402 },
        { value: 'Debrecen', label: '[HUN] Debrecen', img: '../../home/img/flag/hu.svg', lat: 47.5316, lon: 21.6273 },
        { value: 'Bucharest', label: '[ROU] București', img: '../../home/img/flag/ro.svg', lat: 44.4328, lon: 26.1043 },
        { value: 'Cluj-Napoca', label: '[ROU] Cluj-Napoca', img: '../../home/img/flag/ro.svg', lat: 46.7712, lon: 23.6236 },
        { value: 'Berlin', label: '[GER] Berlin', img: '../../home/img/flag/de.svg', lat: 52.5200, lon: 13.4050 },
        { value: 'Munich', label: '[GER] München', img: '../../home/img/flag/de.svg', lat: 48.1351, lon: 11.5820 },
        { value: 'Vienna', label: '[AUT] Wien', img: '../../home/img/flag/at.svg', lat: 48.2082, lon: 16.3738 },
        { value: 'Graz', label: '[AUT] Graz', img: '../../home/img/flag/at.svg', lat: 47.0707, lon: 15.4395 },
        { value: 'Belgrade', label: '[SRB] Белград', img: '../../home/img/flag/yu.svg', lat: 44.8176, lon: 20.4569 },
        { value: 'Zagreb', label: '[HRV] Загреб', img: '../../home/img/flag/yu.svg', lat: 45.8150, lon: 15.9819 },
        { value: 'Ljubljana', label: '[SVN] Любляна', img: '../../home/img/flag/yu.svg', lat: 46.0569, lon: 14.5058 },
        { value: 'Sarajevo', label: '[BIH] Сараево', img: '../../home/img/flag/yu.svg', lat: 43.8563, lon: 18.4131 },
        { value: 'Skopje', label: '[MKD] Skopje', img: '../../home/img/flag/yu.svg', lat: 41.9981, lon: 21.4254 },
        { value: 'Sofia', label: '[BGR] София', img: '../../home/img/flag/bg.svg', lat: 42.6977, lon: 23.3219 },
        { value: 'Plovdiv', label: '[BGR] Пловдив', img: '../../home/img/flag/bg.svg', lat: 42.1354, lon: 24.7453 },
        { value: 'Ulaanbaatar', label: '[MNG] Улаанбаатар', img: '../../home/img/flag/mn.svg', lat: 47.8864, lon: 106.9057 },
        { value: 'Erdenet', label: '[MNG] Эрдэнэт', img: '../../home/img/flag/mn.svg', lat: 49.0340, lon: 104.0567 },{ value: 'Istanbul', label: '[TR] İstanbul', img: '../../home/img/flag/tr.svg', lat: 41.0082, lon: 28.9784 },
        { value: 'Ankara', label: '[TUR] Ankara', img: '../../home/img/flag/tr.svg', lat: 39.9334, lon: 32.8597 },
        { value: 'Izmir', label: '[TUR] İzmir', img: '../../home/img/flag/tr.svg', lat: 38.4192, lon: 27.1287 },
        { value: 'Bursa', label: '[TUR] Bursa', img: '../../home/img/flag/tr.svg', lat: 40.1828, lon: 29.0660 },
        { value: 'Adana', label: '[TUR] Adana', img: '../../home/img/flag/tr.svg', lat: 37.0017, lon: 35.3289 },
        { value: 'Gaziantep', label: '[TUR] Gaziantep', img: '../../home/img/flag/tr.svg', lat: 37.0662, lon: 37.3833 },
        { value: 'Trabzon', label: '[TUR] Trabzon', img: '../../home/img/flag/tr.svg', lat: 41.0027, lon: 39.7178 },
        { value: 'Carsibasi', label: '[TUR] Çarşibaşı', img: '../../home/img/flag/tr.svg', lat: 41.1680, lon: 39.1120 },
        { value: 'Kovanli', label: '[TUR] Kovanlı', img: '../../home/img/flag/tr.svg', lat: 37.9500, lon: 38.4500 },
        { value: 'Antalya', label: '[TUR] Antalya', img: '../../home/img/flag/tr.svg', lat: 36.8969, lon: 30.7133 },
    ];

    const dropdown = createCustomDropdown(
        container,
        cities.map(c => ({ value: c.value, label: c.label })),
        { placeholder: "Город", searchable: true }
    );

    const saved = getAppSettings().weather.townName;
    if (saved) dropdown.setValue(saved);

    function applyCity(value) {
        const city = cities.find(c => c.value === value);
        if (!city) return;
        globalLat = city.lat;
        globalLon = city.lon;
        let appSettings = JSON.parse(localStorage.getItem("appSettings"));
        appSettings.weather.townName = value;
        localStorage.setItem("appSettings", JSON.stringify(appSettings));
        updateAll();
    }

    dropdown.addEventListener("change", e => applyCity(e.detail.value));

    if (dropdown.getValue()) applyCity(dropdown.getValue());
    else applyCity(cities[0].value);
});

/* ---------- BACKGROUND ---------- */

function setBackground(type) {
    //const nowBack = document.getElementById("nowBack");

    const nowBack = document.querySelector("#home .page-header")
    if (!nowBack) return;

    if (!document.getElementById('blacker')) {
        const blacker = document.createElement('div')
        blacker.id = 'blacker'
        nowBack.appendChild(blacker)
    }
    nowBack.classList.remove('weather-rain')
    nowBack.classList.remove('weather-cloudy')
    nowBack.classList.remove('weather-sun')
    nowBack.classList.remove('weather-snow')
    nowBack.classList.remove('weather-fog')
    nowBack.classList.add(`weather-${type}`);

    const hour = new Date().getHours().toString().padStart(2, "0");
}
