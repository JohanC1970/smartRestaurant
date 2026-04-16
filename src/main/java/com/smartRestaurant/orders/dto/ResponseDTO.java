package com.smartRestaurant.orders.dto;

/**
 * DTO genérico para respuestas consistentes
 */
public record ResponseDTO<T>(T data, Boolean hasError) {}

