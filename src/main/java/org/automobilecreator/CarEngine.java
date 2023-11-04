package org.automobilecreator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.math.BigDecimal;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record CarEngine(String engineName, BigDecimal v, Boolean isTurbo) {
}

