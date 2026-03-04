package com.smartRestaurant.inventory.dto;

public record ResponseDTO<T>( T message, Boolean error) {
}
