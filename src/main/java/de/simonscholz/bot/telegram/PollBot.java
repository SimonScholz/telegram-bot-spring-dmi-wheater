package de.simonscholz.bot.telegram;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.simonscholz.bot.telegram.service.DmiUpdateHandlerService;
import de.simonscholz.telegram.bot.api.TelegramBotClient;
import de.simonscholz.telegram.bot.api.domain.TelegramListResponse;
import de.simonscholz.telegram.bot.api.domain.Update;
import reactor.core.publisher.Mono;

@Component
@Profile("poll")
public class PollBot {

	private static final Logger log = LoggerFactory.getLogger(PollBot.class);

	private DmiUpdateHandlerService updateHandler;
	private TelegramBotClient botClient;

	public PollBot(DmiUpdateHandlerService updateHandler, TelegramBotClient botClient) {
		this.updateHandler = updateHandler;
		this.botClient = botClient;
	}

	private int updateIdOffset = 0;

	@Scheduled(fixedRate = 10000)
	public void reportCurrentTime() {
		Mono<TelegramListResponse<Update>> updatesMaybe = botClient.getUpdates(updateIdOffset);
		updatesMaybe.subscribe(res -> {
			List<Update> updates = res.getResult();
			updates.stream().forEach(PollBot.this::handleUpdate);
		}, err -> {
			log.error(err.getMessage(), err);
		});
	}

	private void handleUpdate(Update update) {
		updateIdOffset = update.getUpdate_id() + 1;
		updateHandler.handleUpdate(update);
	}
}
