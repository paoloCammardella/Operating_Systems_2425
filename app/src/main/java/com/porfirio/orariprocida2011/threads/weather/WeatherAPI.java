package com.porfirio.orariprocida2011.threads.weather;

import com.porfirio.orariprocida2011.entity.Osservazione;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class WeatherAPI {

    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/forecast?id=3169807&APPID=dc8cfde44c4955e792406e26a562945e&units=metric";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private WeatherAPI() {

    }

    /**
     * Sends a request to receive the latest forecasts for the next 24 hours. The returned list will contain 8 elements, representing a forecast every three hours.
     *
     * @return a list of forecasts
     * @throws Exception if an error occurs during the request
     */
    public static List<Osservazione> getForecasts() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(WEATHER_URL).openStream(), StandardCharsets.UTF_8))) {
            String jsonText = readAll(reader);
            JSONObject jsonObject = new JSONObject(jsonText);

            String controlCode = (String) jsonObject.get("cod");

            if (controlCode.equals("200")) {
                ArrayList<Osservazione> forecasts = new ArrayList<>();

                JSONArray list = jsonObject.getJSONArray("list");

                // 8 observations, one every three hours

                for (int i = 0; i < 8; i++) {
                    JSONObject cond = list.getJSONObject(i);

                    double windDir = cond.getJSONObject("wind").getDouble("deg");
                    double windSpeed = cond.getJSONObject("wind").getDouble("speed") * 3.6;
                    String timestamp = cond.getString("dt_txt");

                    ZonedDateTime time = ZonedDateTime.of(LocalDateTime.parse(timestamp, TIME_FORMATTER), ZoneOffset.UTC);

                    forecasts.add(new Osservazione((float) windSpeed, (float) windDir, time));
                }

                return forecasts;
            }

            throw new Exception(controlCode);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;

        while ((cp = rd.read()) != -1)
            sb.append((char) cp);

        return sb.toString();
    }

}
