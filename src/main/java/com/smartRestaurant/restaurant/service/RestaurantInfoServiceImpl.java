package com.smartRestaurant.restaurant.service;

import com.smartRestaurant.restaurant.dto.RestaurantInfoDTO;
import com.smartRestaurant.restaurant.dto.UpdateRestaurantInfoDTO;
import com.smartRestaurant.restaurant.model.RestaurantInfo;
import com.smartRestaurant.restaurant.repository.RestaurantInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class RestaurantInfoServiceImpl implements RestaurantInfoService {

    private static final String SINGLETON_ID = "main";

    private final RestaurantInfoRepository repository;

    /** Siempre actualiza los datos del restaurante al arrancar */
    @PostConstruct
    public void init() {
        RestaurantInfo info = repository.findById(SINGLETON_ID).orElse(new RestaurantInfo());
        info.setId(SINGLETON_ID);
        info.setName("SmartRestaurant");
        info.setDescription("Gastronomía de autor con ingredientes de origen.");
        info.setAddress("Av. Bolívar #N 12-67, Universidad del Quindío");
        info.setCity("Armenia, Quindío");
        info.setPhone("3244454996");
        info.setEmail("Smartrestauran15@gmail.com");
        info.setOpeningTime(LocalTime.of(11, 0));
        info.setClosingTime(LocalTime.of(23, 0));
        info.setOpenDays("Lunes - Domingo");
        repository.save(info);
    }

    @Override
    public RestaurantInfoDTO get() {
        RestaurantInfo info = repository.findById(SINGLETON_ID)
                .orElseThrow(() -> new RuntimeException("Información del restaurante no encontrada"));
        return toDTO(info);
    }

    @Override
    public RestaurantInfoDTO update(UpdateRestaurantInfoDTO dto) {
        RestaurantInfo info = repository.findById(SINGLETON_ID)
                .orElseThrow(() -> new RuntimeException("Información del restaurante no encontrada"));
        info.setName(dto.name());
        info.setDescription(dto.description());
        info.setAddress(dto.address());
        info.setCity(dto.city());
        info.setPhone(dto.phone());
        info.setEmail(dto.email());
        info.setOpeningTime(dto.openingTime());
        info.setClosingTime(dto.closingTime());
        info.setOpenDays(dto.openDays());
        info.setLogoUrl(dto.logoUrl());
        return toDTO(repository.save(info));
    }

    @Override
    public boolean isOpen() {
        RestaurantInfo info = repository.findById(SINGLETON_ID)
                .orElseThrow(() -> new RuntimeException("Información del restaurante no encontrada"));
        LocalTime now = LocalTime.now();
        return !now.isBefore(info.getOpeningTime()) && !now.isAfter(info.getClosingTime());
    }

    private RestaurantInfoDTO toDTO(RestaurantInfo info) {
        return new RestaurantInfoDTO(
                info.getId(),
                info.getName(),
                info.getDescription(),
                info.getAddress(),
                info.getCity(),
                info.getPhone(),
                info.getEmail(),
                info.getOpeningTime(),
                info.getClosingTime(),
                info.getOpenDays(),
                info.getLogoUrl()
        );
    }
}
