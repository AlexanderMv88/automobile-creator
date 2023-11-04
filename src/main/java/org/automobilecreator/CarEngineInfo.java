package org.automobilecreator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record CarEngineInfo(CarEngine engine, Integer donePercent) {
}

