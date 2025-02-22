package com.streamify.Storage;

import com.streamify.ffmpeg.FfmpegService;
import com.streamify.post.PostMedia;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {
    private final Logger LOGGER = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final FfmpegService ffmpegService;

    @Value("${application.file.upload.content-base-url.post}")
    private String postBaseUrl;

    @Value("${application.file.upload.content-base-url.story}")
    private String storyBaseUrl;

    public MediaServiceImpl(FfmpegService ffmpegService) {
        this.ffmpegService = ffmpegService;
    }

    @PostConstruct
    public void init() {
        postBaseUrl = postBaseUrl.replace("/", File.separator);
        storyBaseUrl = storyBaseUrl.replace("/", File.separator);
    }

    @Override
    public String uploadPostContent(MultipartFile sourceFile, String userId, String postId) throws IOException {
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        final String finalFileUploadPath = postBaseUrl + File.separator + userId + File.separator + postId;
        File targetFolder = new File(finalFileUploadPath);
        if (!targetFolder.exists()) {
            boolean isFolderCreated = targetFolder.mkdirs();
            if (!isFolderCreated) {
                LOGGER.error("Failed to create the target folder");
                throw new IllegalStateException("Failed to create the target folder");
            }
            LOGGER.info("Successfully create the target folder");
        }
        if (fileExtension == null) {
            throw new IllegalStateException("File extension is not supported");
        }
        String targetFilePath = finalFileUploadPath + File.separator + UUID.randomUUID() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            LOGGER.info("Post Content saved to {}", targetFilePath);
            if (Objects.requireNonNull(sourceFile.getContentType()).startsWith("video/")) {
                // process the video
                ffmpegService.processPostVideoWithFfmpeg(targetPath, postId, userId);
            }
            return targetFilePath;
        } catch (IOException exception) {
            LOGGER.error("Post Content was not saved @error: {}", exception.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean deletePostContent(List<PostMedia> sourceFiles) throws IOException {
       for (PostMedia postMedia : sourceFiles) {
           Files.delete(Paths.get(postMedia.getMediaUrl()));
       }
       // delete the parent folder
       Files.delete(
               Paths.get(
                       sourceFiles.getFirst().getMediaUrl()
                               .substring(
                                       0,
                                       sourceFiles.getFirst().getMediaUrl().lastIndexOf(File.separator)
                               )
               )
       );
       return true;
    }

    @Override
    public String uploadStoryContent(MultipartFile sourceFile, String storyId, String userId) throws IOException, InterruptedException {
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        final String finalFileUploadPath = storyBaseUrl + File.separator + userId;
        File targetFolder = new File(finalFileUploadPath);
        if (!targetFolder.exists()) {
            boolean isFolderCreated = targetFolder.mkdirs();
            if (!isFolderCreated) {
                LOGGER.error("Failed to create the target folder for story");
                throw new IllegalStateException("Failed to create the target folder");
            }
            LOGGER.info("Successfully create the target folder for story");
        }
        if (fileExtension == null) {
            throw new IllegalStateException("File extension is not supported");
        }
        String targetFilePath = finalFileUploadPath + File.separator + UUID.randomUUID() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        if (sourceFile.getContentType().startsWith("image/")) {
            Files.write(targetPath, sourceFile.getBytes());
            LOGGER.info("Story image saved to {}", targetFilePath);
            return targetFilePath;
        } else if (sourceFile.getContentType().startsWith("video/")) {
            if (!isValidStoryVideo(sourceFile)) {
                throw new IllegalStateException("You can only upload 15s story video!");
            }
            Files.write(targetPath, sourceFile.getBytes());
            LOGGER.info("Story video saved to {}", targetFilePath);

            // process the video using the ffmpeg
            ffmpegService.processStoryVideoWithFfmpeg(targetPath, storyId, userId);
            return targetFilePath;
        } else {
            throw new IllegalStateException("Only image and videos are allowed for story!");
        }
    }

    @Override
    public boolean deleteStoryContent(String mediaUrl) throws IOException {
        // delete the file content
        Files.delete(Paths.get(mediaUrl));
        return true;
    }

    private boolean isValidStoryVideo(MultipartFile sourceFile) throws IOException, InterruptedException {
        // todo -> here we check the video duration is 15s
        return ffmpegService.isValidStoryVideo(sourceFile);
    }


    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIdx = filename.lastIndexOf(".");
        if (lastDotIdx == -1) {
            return null;
        }
        return filename.substring(lastDotIdx + 1).toLowerCase();
    }
}
