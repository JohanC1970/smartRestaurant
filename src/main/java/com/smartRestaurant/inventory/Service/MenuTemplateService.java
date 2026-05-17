package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.menu.request.CreateMenuTemplateRequest;
import com.smartRestaurant.inventory.dto.menu.response.MenuTemplateDTO;

import java.util.List;

public interface MenuTemplateService {
    void create(CreateMenuTemplateRequest request);
    List<MenuTemplateDTO> getAll();
    MenuTemplateDTO getById(String id);
    void delete(String id);
}
