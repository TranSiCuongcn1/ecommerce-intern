package com.trancuong.ecommerce.media.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.media.dto.MediaUploadResponse;
import com.trancuong.ecommerce.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaUploadResponse> upload(@RequestPart MultipartFile file) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Upload media successfully",
                mediaService.upload(file)
        );
    }
}
