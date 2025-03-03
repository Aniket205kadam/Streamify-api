package com.streamify.ffmpeg;

import com.streamify.exception.OperationNotPermittedException;
import com.streamify.post.Post;
import com.streamify.post.PostMedia;
import com.streamify.post.PostMediaRepository;
import com.streamify.post.PostRepository;
import com.streamify.story.Story;
import com.streamify.story.StoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FfmpegService {
    private final Logger LOGGER = LoggerFactory.getLogger(FfmpegService.class);

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final StoryRepository storyRepository;

    @Value("${application.file.upload.content-base-url.post}")
    private String postBaseUrl;

    @Value("${application.file.upload.content-base-url.story}")
    private String storyBaseUrl;

    @Value("${application.file.upload.content-base-url.temp}")
    private String tempUrl;

    public FfmpegService(PostRepository postRepository, PostMediaRepository postMediaRepository, StoryRepository storyRepository) {
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.storyRepository = storyRepository;
    }

    @PostConstruct
    public void init() {
        postBaseUrl = postBaseUrl.replace("/", File.separator);
        tempUrl = tempUrl.replace("/", File.separator);
    }

    @Async
    public void processPostVideoWithFfmpeg(Path fileUrl, String postId, String userId) throws IOException {
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
        String targetFilePath = finalFileUploadPath + File.separator + UUID.randomUUID();
        Path targetPath = Paths.get(targetFilePath);
        // create the target folder for ffmpeg
        boolean isTargetFolderCreated = new File(targetFilePath).mkdirs();
        if (!isTargetFolderCreated) {
            throw new IllegalStateException("Target folder for the ffmpeg is failed to create!");
        }
        try {
            Process process = getProcess(fileUrl, targetPath);
            int status = process.waitFor();
            LOGGER.info("Process: status is: {}", status);
            ProcessHandle.Info info = process.info();
            LOGGER.info(info.toString());
            if (status != 0) {
                throw new Exception("Failed: to process the video!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Post is not found with ID: " + postId)
                );
        PostMedia postMedia = post.getPostMedia().stream()
                .filter(media ->
                        media.getMediaUrl().equals(fileUrl.toString())
                )
                .findAny()
                .orElseThrow(() ->
                        new EntityNotFoundException("PostMedia is not found with URL: " + fileUrl.toString())
                );
        postMedia.setMediaUrl(targetPath.toString() + File.separator + "master.m3u8"); //todo -> if some problem
        postMediaRepository.save(postMedia);

        // remove the temp post video from the location
        Files.delete(fileUrl);
        LOGGER.info("Post Video Processing is done");
    }

    private static Process getProcess(Path fileUrl, Path targetPath) throws IOException {
        String ffmpegCmd = String.format(
                "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                fileUrl,
                targetPath,
                targetPath
        );
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
        processBuilder.inheritIO();
        return processBuilder.start();
    }

    @Async
    public void isValidReel(
            @NonNull Path fileUrl,
            @NonNull String postId
    ) {
        File file = new File(fileUrl.toString());
        if (!file.exists()) {
            throw new IllegalStateException("Enter valid file location for check the video is reel or not!");
        }
        try {
            String ffmpegCmd = String.format(
                    "ffprobe -i \"%s\" -show_entries format=duration -v quiet -of csv=\"p=0\"",
                    fileUrl.toString()
            );
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder durationInString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                durationInString.append(line).append("\n");
            }
            int status = process.waitFor();
            LOGGER.info("isValidReel Process status: {}", status);
            ProcessHandle.Info info = process.info();
            LOGGER.info(info.toString());
            if (status != 0) {
                throw new IllegalStateException("Failed: to process the video!");
            }
            if (durationInString.isEmpty()) {
                throw new IllegalStateException("Failed: to process the video!");
            }
            double duration = Double.parseDouble(durationInString.toString());

            // check the video is less than 90s
            if (duration <= 90) {
                Post post = postRepository.findById(postId)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Post is not found with ID: " + postId)
                        );
                PostMedia postMedia = post.getPostMedia()
                        .stream()
                        .filter(media ->
                                media.getMediaUrl().equals(fileUrl.toString())
                        )
                        .findAny()
                        .orElseThrow(() ->
                                new EntityNotFoundException("Reel is not found when we check the video duration")
                        );
                if (postMedia == null) {
                    throw new IllegalStateException("The given URL is not include in the post");
                }
                post.setReel(true);
                postRepository.save(post);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void processStoryVideoWithFfmpeg(Path fileUrl, String storyId, String userId) throws IOException {
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
        String targetFilePath = finalFileUploadPath + File.separator + UUID.randomUUID();
        Path targetPath = Paths.get(targetFilePath);
        // create the target folder for ffmpeg
        boolean isTargetFolderCreated = new File(targetFilePath).mkdirs();
        if (!isTargetFolderCreated) {
            throw new OperationNotPermittedException("Target folder for the ffmpeg is failed to create!");
        }
        try {
            Process process = getProcess(fileUrl, targetPath);
            int status = process.waitFor();
            LOGGER.info("Process for story: status is: {}", status);
            ProcessHandle.Info info = process.info();
            LOGGER.info(info.toString());
            if (status != 0) {
                throw new OperationNotPermittedException("Failed: to process the video!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Story story = storyRepository.findById(storyId)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Story is not found with ID: " + storyId));
        if (!story.getMediaUrl().equals(fileUrl.toString())) {
            Files.deleteIfExists(targetPath);
            throw new OperationNotPermittedException("Story media URL is not valid!");
        }
        story.setMediaUrl(targetPath.toString());
        storyRepository.save(story);
        // remove the temp post video from the location
        Files.delete(fileUrl);
        LOGGER.info("Post Video Processing is done");
    }

    public boolean isValidStoryVideo(MultipartFile sourceFile) throws IOException, InterruptedException {
        File tempFolder = new File(tempUrl);
        boolean isTempFolderCreated = !tempFolder.exists() && tempFolder.mkdirs();
        LOGGER.info(isTempFolderCreated ? "Temp folder is created!" : "Temp folder is already created!");
        Path targetFilePath = Paths.get(tempUrl, sourceFile.getOriginalFilename());
        Files.write(targetFilePath, sourceFile.getBytes());

        String ffmpegCmd = String.format(
                "ffprobe -i \"%s\" -show_entries format=duration -v quiet -of csv=\"p=0\"",
                targetFilePath.toString()
        );
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder durationInString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            durationInString.append(line).append("\n");
        }
        int status = process.waitFor();
        LOGGER.info("isValidStory Process status: {}", status);
        ProcessHandle.Info info = process.info();
        LOGGER.info(info.toString());
        if (status != 0) {
            throw new OperationNotPermittedException("Failed: to process the video!");
        }
        if (durationInString.isEmpty()) {
            throw new OperationNotPermittedException("Failed: to process the video!");
        }
        double duration = Double.parseDouble(durationInString.toString());
        Files.deleteIfExists(targetFilePath);
        return duration <= 15;
    }

    public Resource getImagePreview(String fileUrl, String scale) {
        scale = (scale == null || scale.isEmpty()) ? "200" : scale;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg", "-i", fileUrl, "-vf", "scale=" + scale + ":-1", "-q:v", "5", "-f", "image2pipe", "pipe:1"
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            return new InputStreamResource(process.getInputStream());
        } catch (IOException e) {
            throw new OperationNotPermittedException(e.getMessage());
        }
    }

    public Resource getImage(String fileUrl) throws FileNotFoundException {
        File image = new File(fileUrl);
        if (!image.exists()) {
            throw new EntityNotFoundException("Image is not found with URL: " + fileUrl);
        }
        InputStream inputStream = new FileInputStream(image);
        return new InputStreamResource(inputStream);
    }

    public Path generateThumbnail(String mediaUrl, String thumbnailPath) throws IOException, InterruptedException {
        // ffmpeg -i master.m3u8 -ss 00:00:10 -vframes 1 thumbnail.png
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", mediaUrl,
                "-ss", "00:00:05",
                "-vframes", "1",
                thumbnailPath
        );
        Process process = processBuilder.start();

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroy();
            throw new OperationNotPermittedException("Failed to create the video thumbnail!");
        }
        return Paths.get(thumbnailPath);
    }
}
