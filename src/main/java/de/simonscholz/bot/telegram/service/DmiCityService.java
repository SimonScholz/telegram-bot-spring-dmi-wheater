package de.simonscholz.bot.telegram.service;

import org.springframework.stereotype.Service;

import de.simonscholz.bot.telegram.data.DmiCityRepository;
import de.simonscholz.bot.telegram.weather.DmiApi;
import de.simonscholz.bot.telegram.weather.DmiCity;
import de.simonscholz.osm.api.OSMLocation;
import de.simonscholz.osm.api.OpenStreetMapApi;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DmiCityService {

	private OpenStreetMapApi osmApi;
	private DmiApi dmiApi;
	private DmiCityRepository cityRepository;

	public DmiCityService(OpenStreetMapApi osmApi, DmiApi dmiApi, DmiCityRepository cityRepository) {
		this.osmApi = osmApi;
		this.dmiApi = dmiApi;
		this.cityRepository = cityRepository;
	}

	public Mono<Integer> getDmiCityId(String cityname) {
		Mono<DmiCity> findByCity = cityRepository.findByCityIgnoreCase(cityname);

		return findByCity.switchIfEmpty(dmiApi.getDmiCities(cityname).next().map(citylist -> citylist.get(0)))
				.flatMap(dmiCity -> {
					dmiCity.setCity(cityname);
					return cityRepository.save(dmiCity);
				}).map(DmiCity::getId);
	}

	public Mono<Integer> getDmiCityId(double longitude, double latitude) {
		Mono<OSMLocation> locationData = osmApi.getLocationData(latitude, longitude);

		return locationData.map(location -> location.getAddress().getState()).flatMap(this::getDmiCityId);
	}
}
