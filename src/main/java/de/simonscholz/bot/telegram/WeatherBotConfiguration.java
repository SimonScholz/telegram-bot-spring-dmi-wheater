package de.simonscholz.bot.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory;

import de.simonscholz.bot.telegram.weather.DmiApi;
import de.simonscholz.osm.api.OpenStreetMapApi;
import de.simonscholz.telegram.bot.api.TelegramBotClient;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@PropertySource("classpath:weatherbot.properties")
public class WeatherBotConfiguration {

	@Bean
	public Builder retrofitBuilder() {
		return new Retrofit.Builder().addConverterFactory(JacksonConverterFactory.create())
				.addCallAdapterFactory(ReactorCallAdapterFactory.create());
	}

	@Bean
	public DmiApi dmiApi(Builder builder) {
		return builder.baseUrl(DmiApi.BASE_URL).build().create(DmiApi.class);
	}

	@Bean
	public OpenStreetMapApi locationApi(Builder builder) {
		return builder.baseUrl(OpenStreetMapApi.BASE_URL).build().create(OpenStreetMapApi.class);
	}

	@Bean
	public TelegramBotClient getTelegramBotClientDmiWeather(@Value("${bot.api.url}") String apiUrl,
			@Value("${bot.dmiweather.token}") String botToken) {
		return TelegramBotClient.createReactorJackson(apiUrl, botToken);
	}
}
