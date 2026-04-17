package com.smartRestaurant.restaurant.service;

import com.smartRestaurant.restaurant.dto.RestaurantInfoDTO;
import com.smartRestaurant.restaurant.dto.UpdateRestaurantInfoDTO;

public interface RestaurantInfoService {
    RestaurantInfoDTO get();
    RestaurantInfoDTO update(UpdateRestaurantInfoDTO dto);
    boolean isOpen();
}
