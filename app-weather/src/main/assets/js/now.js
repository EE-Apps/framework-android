// DOM - используем функции для отложенного поиска элементов
const getNOWtemperatureElement = () => document.querySelector("#home .page-header h1")
const getNOWtemperatureFeelsElement = () => document.querySelector("#home .page-header h3")
/*const getNOWwindSpeedElement = () => document.getElementById("wind-speed")
const getNOWhumidityElement = () => document.getElementById("humidity")*/
const getNOWwindSpeedElement = () => document.querySelector("#home .page-header #wenow .wind")
const getNOWhumidityElement = () => document.querySelector("#home .page-header #wenow .humidity")
const getNOWpressureElement = () => document.querySelector("#pressure_text")

const backgroundWeatherIcons = {
    0: "sun", 1: "cloudy", 2: "cloudy", 3: "cloudy",
    45: "fog", 48: "fog",
    51: "rain", 53: "rain", 55: "rain",
    56: "rain", 57: "rain",
    61: "rain", 63: "rain", 65: "rain",
    66: "rain", 67: "rain",
    71: "snow", 73: "snow", 75: "snow", 77: "snow",
    80: "rain", 81: "rain", 82: "rain",
    85: "snow", 86: "snow",
    95: "thunderstorm", 96: "thunderstorm", 99: "thunderstorm",
};

const hPaToMm = hPa => (hPa * 0.75006375541921).toFixed(1);
const kmhToMs = kmh => (kmh / 3.6).toFixed(1);

/**
 * Использует объект current из общего ответа Open-Meteo
 */
function NOWrenderCurrent(current, data) {
    if (!current) return;

    if (!document.getElementById('wenow')) {
        const wenow = document.createElement('div')
        wenow.id = 'wenow'
        document.querySelector("#home .page-header").appendChild(wenow)

        const wind = document.createElement('div')
        wind.className = 'row'
        wind.innerHTML = `<img src="img/wind.svg" class="detalis-ico wind_ico"><h2 class="wind"></h2>`
        const humidity = document.createElement('div')
        humidity.className = 'row'
        humidity.innerHTML = `<img src="img/humidity.svg" class="detalis-ico"><h2 class="humidity"></h2>`
        const pressure = document.createElement('div')
        pressure.className = 'row'
        pressure.innerHTML = `<img src="img/pressure.svg" class="detalis-ico"><h2 class="pressure"></h2>`
        const weatherType = document.createElement('div')
        weatherType.className = 'row'
        weatherType.innerHTML = `<img src="${weatherIcon(current.weather_code, current.is_day)}" class="detalis-ico"><h2 class="weatherType">${weatherTypes[current.weather_code][isRuLang ? "ru" : "en"]}</h2>`

        wenow.appendChild(wind)
        wenow.appendChild(humidity)
        wenow.appendChild(weatherType)
        //wenow.appendChild(pressure)
    }

    const tempEl = getNOWtemperatureElement()
    const tempFeelsEl = getNOWtemperatureFeelsElement()
    const windEl = getNOWwindSpeedElement()
    const humidEl = getNOWhumidityElement()
    const pressEl = getNOWpressureElement()
    const windSpeedEl = document.getElementById("wind_speed")
    const windDirrectionEl = document.getElementById("wind_dirrection")
    const cloudsEl = document.getElementById("clouds_text")
    const falloutEl = document.getElementById("fallout_text")
    const sundayEl = document.getElementById("sunday_text")
    const sundayStartEl = document.getElementById("sunday_start")
    const sundayEndEl = document.getElementById("sunday_end")
    const sundayDurationEl = document.getElementById("sunday_duration")

    if (!tempEl || !windEl || !humidEl) {
        console.warn('Элементы для отображения текущей погоды ещё не инициализированы');
        return;
    }

    tempFeelsEl.className = "temperature_apparent"
    tempEl.className = "temperature_exact"
    document.querySelectorAll('.wind_ico').forEach(el => {
        const correcting = el.classList.contains("detalis-ico") ? 90 : 0
        el.style.transform = `rotate(${correcting + current.wind_direction_10m}deg)`
    })

    const wd = current.wind_direction_10m
    const sunriseDate = new Date(data.daily.sunrise[0] * 1000); 
    const sunsetDate = new Date(data.daily.sunset[0] * 1000); 
    const sunsetDuration = new Date((data.daily.sunset[0] - data.daily.sunrise[0]) * 1000); 

    tempEl.textContent = ` ${Math.round(current.temperature_2m)}°`
    tempFeelsEl.textContent = `${isRuLang ? "Ощущается как" : "Feels like"} ${Math.round(current.apparent_temperature)}`
    windEl.textContent = `${kmhToMs(current.wind_speed_10m)} м/с`
    windSpeedEl.textContent = `${kmhToMs(current.wind_speed_10m)} м/с`
    windDirrectionEl.textContent =  returnDirrection(
                                    wd < 23 ? "N" : 
                                    wd < 68 ? "NE" : 
                                    wd < 113 ? "E" :
                                    wd < 158 ? "SE" :
                                    wd < 203 ? "S" :
                                    wd < 248 ? "SW" :
                                    wd < 293 ? "W" :
                                    wd < 338 ? "NW" :
                                               "N")
    cloudsEl.textContent = `${current.cloud_cover}%`
    falloutEl.textContent = `${current.precipitation} мм.`
    humidEl.textContent = current.relative_humidity_2m != null ? `${current.relative_humidity_2m}%` : (isRuLang ? 'н/д' : 'unknow')
    pressEl.textContent =
        current.surface_pressure != null
            ? `${hPaToMm(current.surface_pressure)} ${isRuLang ? 'мм рт. ст.' : 'mmHg Art.'}`
            : "н/д";
    sundayStartEl.textContent = `${sunriseDate.getHours()}:${sunriseDate.getMinutes()}`
    sundayEndEl.textContent = `${sunsetDate.getHours()}:${sunriseDate.getMinutes()}`
    sundayDurationEl.textContent = `${sunsetDuration.getHours()} ч. ${sunsetDuration.getMinutes()} мин.`

    setBackground(backgroundWeatherIcons[current.weather_code] || "default")

}
