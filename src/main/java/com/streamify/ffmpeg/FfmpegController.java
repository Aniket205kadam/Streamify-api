package com.streamify.ffmpeg;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.Map;

@RestController
@RequestMapping("ffmpeg")
@Tag(name = "Ffmpeg")
public class FfmpegController {
    private final FfmpegService ffmpegService;

    private static final Map<String, MediaType> MEDIA_TYPE_MAP = Map.of(
            "jpg", MediaType.IMAGE_JPEG,
            "jpeg", MediaType.IMAGE_JPEG,
            "png", MediaType.IMAGE_PNG
    );

    public FfmpegController(FfmpegService ffmpegService) {
        this.ffmpegService = ffmpegService;
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> getImage(
            @RequestParam("file-url") String fileUrl
    ) throws FileNotFoundException {
        // set http response header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MEDIA_TYPE_MAP.get(fileUrl.substring(fileUrl.lastIndexOf(".") + 1))
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(ffmpegService.getImage(fileUrl));
    }

    @GetMapping("/image/preview/{file-url}/{scale}")
    public ResponseEntity<Resource> getImagePreview(
            @PathVariable("file-url") String fileUrl,
            @PathVariable("scale") String scale
    ) {
        // set http response header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MEDIA_TYPE_MAP.get(fileUrl.substring(fileUrl.lastIndexOf(".") + 1))
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(ffmpegService.getImagePreview(fileUrl, scale));
    }

}
