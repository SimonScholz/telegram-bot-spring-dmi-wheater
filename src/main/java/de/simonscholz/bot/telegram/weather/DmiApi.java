package de.simonscholz.bot.telegram.weather;

import java.util.List;

import okhttp3.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface DmiApi {
	
	static final String BASE_URL = "http://www.dmi.dk/Data4DmiDk/";
	
	static final String MODE_NOW = "dag1_2";

	static final String MODE_WEEK = "dag3_9";
	
	@GET("getData?type=forecast")
	Flux<List<DmiCity>> getDmiCities(@Query("term") String cityName);

	@Streaming
	@GET("http://servlet.dmi.dk/byvejr/servlet/world_image")
	Mono<ResponseBody> getWeatherImage(@Query("city") String cityId, @Query("mode") String mode);
	
	static String getWeatherImageUrl(String cityId, String mode) {
		// added System.currentTimeMillis() at the end of the image url, because telegram caches image urls
		return "http://www.dmi.dk/byvejr/servlet/world_image?city=" + cityId + "&mode=" + mode + "&time=" + System.currentTimeMillis();
	}
}
