package de.simonscholz.bot.telegram.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import de.simonscholz.bot.telegram.weather.DmiCity;
import reactor.core.publisher.Mono;

public interface DmiCityRepository extends ReactiveCrudRepository<DmiCity, Integer> {

	Mono<DmiCity> findByCityIgnoreCase(String city);
}
