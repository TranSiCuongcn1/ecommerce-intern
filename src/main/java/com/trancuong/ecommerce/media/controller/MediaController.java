package com.trancuong.ecommerce.media.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/media")
public class MediaController {

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload() {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Upload media successfully",
                Map.of("message", "TODO: upload file to MinIO and return imageUrl")
        );
    }
}
