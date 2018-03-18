package de.simonscholz.bot.telegram;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.simonscholz.bot.telegram.service.DmiUpdateHandlerService;
import de.simonscholz.telegram.bot.api.TelegramBotClient;
import de.simonscholz.telegram.bot.api.domain.TelegramListResponse;
import de.simonscholz.telegram.bot.api.domain.Update;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Mono;

@RestController
class DmiWeatherBotController {

	private static final Logger LOG = LoggerFactory.getLogger(DmiWeatherBotController.class);

	private DmiUpdateHandlerService updateHandler;
	private TelegramBotClient botClient;

	private Counter requestCounter;

	public DmiWeatherBotController(DmiUpdateHandlerService updateHandler, TelegramBotClient botClient, MeterRegistry meterRegistry) {
		this.updateHandler = updateHandler;
		this.botClient = botClient;
		requestCounter = meterRegistry.counter("de.simonscholz.request.count");
	}

	@PostMapping("/dmiWebhook")
	public Mono<Void> webhook(@RequestBody Update update) {
		requestCounter.increment();
		return updateHandler.handleUpdate(update);
	}

	@GetMapping("/dmiPoll")
	public String poll(@RequestParam(name = "count", required = false, defaultValue = "1") int count) {
		Mono<TelegramListResponse<Update>> updatesMaybe = botClient.getUpdates();
		updatesMaybe.subscribe(res -> {
			List<Update> updates = res.getResult();
			updates.stream().limit(count).forEach(this::webhook);
		}, err -> {
			LOG.error(err.getMessage(), err);
		});
		return "Polling updates";
	}

	@GetMapping("/dmiPrintUpdates")
	public void getUpdates() {
		Mono<TelegramListResponse<Update>> updatesMaybe = botClient.getUpdates();
		updatesMaybe.subscribe(res -> {
			List<Update> updates = res.getResult();
			String collect = updates.stream().map(update -> update.getMessage().getText())
					.collect(Collectors.joining(","));
			LOG.info(collect);
		}, err -> {
			LOG.error(err.getMessage(), err);
		});
	}
}
