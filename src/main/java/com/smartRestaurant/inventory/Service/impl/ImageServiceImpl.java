//package com.smartRestaurant.inventory.Service.impl;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import com.smartRestaurant.inventory.Service.ImageService;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class ImageServiceImpl implements ImageService {
//
//    @Value("${cloudinary.cloud_name}")
//    private String cloudName;
//    @Value("${cloudinary.api_key}")
//    private String apiKey;
//    @Value("${cloudinary.api_secret}")
//    private String apiSecret;
//
//    private Cloudinary cloudinary;
//
//    @PostConstruct
//    public void init() {
//        Map<String, String> config = new HashMap<>();
//        config.put("cloud_name", cloudName);
//        config.put("api_key", apiKey);
//        config.put("api_secret", apiSecret);
//        cloudinary = new Cloudinary(config);
//    }
//
//    @Override
//    public Map upload(MultipartFile image) throws Exception {
//        File file = convert(image);
//        Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "SmartRestaurant"));
//        return result;
//    }
//
//    @Override
//    public Map delete(String imageId) throws Exception {
//        return cloudinary.uploader().destroy(imageId, ObjectUtils.emptyMap());
//    }
//
//    private File convert(MultipartFile image) throws IOException {
//        File file = File.createTempFile(image.getOriginalFilename(), null);
//        FileOutputStream fos = new FileOutputStream(file);
//        fos.write(image.getBytes());
//        fos.close();
//        return file;
//    }
//
//}
