package com.smartRestaurant.inventory.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


public interface ImageService {

    Map upload(MultipartFile file) throws Exception;
    Map delete(String imageId) throws Exception;
}
