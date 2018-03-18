package de.simonscholz.bot.telegram.service;

import static de.simonscholz.bot.telegram.weather.DmiApi.getWeatherImageUrl;
import static de.simonscholz.telegram.bot.api.TelegramHelper.getMessage;
import static de.simonscholz.telegram.bot.api.TelegramHelper.getUserName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.simonscholz.bot.telegram.weather.DmiApi;
import de.simonscholz.telegram.bot.api.TelegramBotClient;
import de.simonscholz.telegram.bot.api.domain.Chat;
import de.simonscholz.telegram.bot.api.domain.Location;
import de.simonscholz.telegram.bot.api.domain.Message;
import de.simonscholz.telegram.bot.api.domain.Update;
import reactor.core.publisher.Mono;

@Service
public class DmiUpdateHandlerService {

	private Logger LOG = LoggerFactory.getLogger(DmiUpdateHandlerService.class);

	private TelegramBotClient botClient;

	private DmiCityService dmiCityService;

	public DmiUpdateHandlerService(TelegramBotClient botClient, DmiCityService dmiCityService) {
		this.botClient = botClient;
		this.dmiCityService = dmiCityService;
	}

	public Mono<Void> handleUpdate(Update update) {
		Mono<Message> messageMono = getMessage(update);

		messageMono.subscribe(message -> {

			long chatId = message.getChat().getId();
			String text = message.getText();
			Location location = message.getLocation();

			if (text != null) {
				LOG.debug("Chat id:" + chatId);
				LOG.debug("Text : " + text);

				int indexOf = text.indexOf(" ");

				if (indexOf > -1) {
					String queryString = text.substring(indexOf);

					if (text.startsWith("/now")) {
						Mono<Integer> dmiCityId = dmiCityService.getDmiCityId(queryString.trim());
						sendDmiPhoto(chatId, dmiCityId, DmiApi.MODE_NOW);
					} else if (text.startsWith("/week")) {
						Mono<Integer> dmiCityId = dmiCityService.getDmiCityId(queryString.trim());
						sendDmiPhoto(chatId, dmiCityId, DmiApi.MODE_WEEK);
					}
				} else if (text.toLowerCase().startsWith("/chatid")) {
					long id = message.getChat().getId();
					botClient.sendMessage(id, "Your chat id is: " + id).subscribe();
				} else if (text.startsWith("/start") || text.startsWith("/help")) {
					String username = getUserName(message);
					StringBuilder sb = new StringBuilder();
					sb.append("Hello ");
					sb.append(username);
					sb.append(",");
					sb.append(System.lineSeparator());
					sb.append(System.lineSeparator());
					sb.append("Nice to meet you. I am the Dmi.dk weather bot.");
					sb.append(System.lineSeparator());
					sb.append("I was developed by Simon Scholz, a java developer, located in Hamburg.");
					sb.append(System.lineSeparator());
					sb.append("My source code can be found here: https://github.com/SimonScholz/telegram-bot/");
					sb.append(System.lineSeparator());
					sb.append(System.lineSeparator());
					sb.append("But enough of this technical stuff.");
					sb.append(System.lineSeparator());
					sb.append("You wanna have these nice dmi.dk weather charts, right? ");
					sb.append(System.lineSeparator());
					sb.append(
							"You can get these by using the /now + {your home town name} or /week + {your home town name} or by simply sending me your location. ");
					sb.append(System.lineSeparator());
					sb.append(
							"The /now command shows the weather forecast for the next 3 days and the /week command is used for the week beginning after the next 3 days.");
					botClient.sendMessage(chatId, sb.toString()).subscribe(m -> {
						LOG.debug(m.getText());
					});
				} else {
					Chat chat = message.getChat();
					if (Chat.TYPE_PRIVATE.equals(chat.getType())) {
						Mono<Message> sendMessage = botClient.sendMessage(chatId,
								"This is not a proper command. \n You can send /help to get help.");
						sendMessage.subscribe(m -> {
							LOG.debug(m.getText());
						});
					}
				}
			} else if (location != null) {
				Mono<Integer> dmiCityId = dmiCityService.getDmiCityId(location.getLongitude(), location.getLatitude());
				sendDmiPhoto(chatId, dmiCityId, DmiApi.MODE_NOW);
			}
		});
		return Mono.empty();
	}

	private void sendDmiPhoto(long chatId, Mono<Integer> dmiCityId, String mode) {
		dmiCityId.subscribe(cityId -> {
			String weatherImageUrl = getWeatherImageUrl(String.valueOf(cityId), mode);
			Mono<Message> sendPhoto = botClient.sendPhoto(chatId, weatherImageUrl);
			sendPhoto.subscribe(m -> {
				LOG.debug(m.getText());
			});
		}, err -> LOG.error(err.getMessage(), err));
	}
}
