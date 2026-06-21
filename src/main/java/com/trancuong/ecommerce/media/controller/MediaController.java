package com.trancuong.ecommerce.media.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/media")
public class MediaController {

    @PostMapping("/upload")
    public Map<String, String> upload() {
        return Map.of("message", "TODO: upload file to Cloudinary and return imageUrl");
    }
}
