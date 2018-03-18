package de.simonscholz.bot.telegram.weather;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@Document
@JsonIgnoreProperties(ignoreUnknown = true)
public class DmiCity {
	@Id
	private int id;

	private String label;

	private double longitude;

	private double latitude;

	private String country;

	private String country_code;

	private String city;
}
