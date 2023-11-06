package org.automobilecreator.dto;

import java.util.List;

public record CreationResult(CarEngineInfo engineInfo, CarBodyInfo carBodyInfo, List<CarWheelInfo> carWheelsInfo) {
}
