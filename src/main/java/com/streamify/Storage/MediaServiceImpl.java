package com.streamify.Storage;

import com.streamify.exception.OperationNotPermittedException;
import com.streamify.ffmpeg.FfmpegService;
import com.streamify.post.Post;
import com.streamify.post.PostMedia;
import com.streamify.post.PostMediaRepository;
import com.streamify.post.PostRepository;
import com.streamify.story.Story;
import com.streamify.story.StoryRepository;
import com.streamify.user.User;
import com.streamify.user.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;

@Service
public class MediaServiceImpl implements MediaService {
    private final Logger LOGGER = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final FfmpegService ffmpegService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final StoryRepository storyRepository;

    @Value("${application.file.upload.content-base-url.post}")
    private String postBaseUrl;

    @Value("${application.file.upload.content-base-url.story}")
    private String storyBaseUrl;

    @Value("${application.file.upload.content-base-url.profile}")
    private String profileBaseUrl;

    @Value("${application.file.upload.content-base-url.thumbnail}")
    private String thumbnailUrl;

    @Value("${application.file.upload.content-base-url.chat}")
    private String chatBaseUrl;

    public MediaServiceImpl(FfmpegService ffmpegService, UserRepository userRepository, PostRepository postRepository, PostMediaRepository postMediaRepository, StoryRepository storyRepository) {
        this.ffmpegService = ffmpegService;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.storyRepository = storyRepository;
    }

    @PostConstruct
    public void init() {
        postBaseUrl = postBaseUrl.replace("/", separator);
        storyBaseUrl = storyBaseUrl.replace("/", separator);
        profileBaseUrl = profileBaseUrl.replace("/", separator);
        thumbnailUrl = thumbnailUrl.replace("/", separator);
        chatBaseUrl = chatBaseUrl.replace("/", separator);
    }

    @Override
    public String uploadPostContent(MultipartFile sourceFile, String userId, String postId) throws IOException {
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        final String finalFileUploadPath = postBaseUrl + separator + userId + separator + postId;
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
        String targetFilePath = finalFileUploadPath + separator + UUID.randomUUID() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            LOGGER.info("Post Content saved to {}", targetFilePath);
            //todo -> for now we are not hls the video, there is bug later updated we are resolve this..!
            /*if (Objects.requireNonNull(sourceFile.getContentType()).startsWith("video/")) {
                // process the video
                ffmpegService.processPostVideoWithFfmpeg(targetPath, postId, userId);
            }*/
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
                                       sourceFiles.getFirst().getMediaUrl().lastIndexOf(separator)
                               )
               )
       );
       return true;
    }

    @Override
    public String uploadStoryContent(MultipartFile sourceFile, String storyId, String userId) throws IOException, InterruptedException {
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        final String finalFileUploadPath = storyBaseUrl + separator + userId;
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
        String targetFilePath = finalFileUploadPath + separator + UUID.randomUUID() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        if (sourceFile.getContentType().startsWith("image/")) {
            Files.write(targetPath, sourceFile.getBytes());
            LOGGER.info("Story image saved to {}", targetFilePath);
            return targetFilePath;
        } else if (sourceFile.getContentType().startsWith("video/")) {
            if (!isValidStoryVideo(sourceFile)) {
                throw new OperationNotPermittedException("You can only upload 15s story video!");
            }
            Files.write(targetPath, sourceFile.getBytes());
            LOGGER.info("Story video saved to {}", targetFilePath);

            // process the video using the ffmpeg
            //ffmpegService.processStoryVideoWithFfmpeg(targetPath, storyId, userId);
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

    @Override
    public Resource getUserProfileImageByUsername(String username) throws MalformedURLException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User is not found with username: " + username));
        String userProfileImage = user.getProfilePictureUrl();

        if (!StringUtils.hasText(userProfileImage)) {
           userProfileImage = Paths.get(profileBaseUrl, "common-avatar", "no-profile-image.jpg").toString();
        }
        Path imagePath = Paths.get(userProfileImage).normalize();
        Resource resource = new UrlResource(imagePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new OperationNotPermittedException("Image not found or not readable: " + userProfileImage);
    }

    @Override
    public String uploadProfile(User user, MultipartFile avtar) {
        if (!Objects.requireNonNull(avtar.getContentType()).startsWith("image/")) {
            throw new OperationNotPermittedException("You can upload only image as the avtar!");
        }
        final String fileExtension = getFileExtension(avtar.getOriginalFilename());
        final String avtarPath = profileBaseUrl + separator + user.getUsername() + "." + fileExtension;
        Path targetPath = Paths.get(avtarPath);
        try {
            Files.write(targetPath, avtar.getBytes());
            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload the avtar, try again!");
        }
    }

    @Override
    public boolean deleteFile(String previousAvtar) {
        try {
            Files.delete(Paths.get(previousAvtar));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // todo -> Return the image preview
    @Override
    public Resource getPostPreviewImage(String postId) throws IOException, InterruptedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post is not found with ID: " + postId));
        PostMedia postMedia = post.getPostMedia().getFirst();
        if (postMedia.getType().startsWith("video/")) {
            Path targetPath = Paths.get(thumbnailUrl, postMedia.getId() + ".jpg");
            Files.createDirectories(Paths.get(thumbnailUrl));
            if (Files.exists(targetPath)) {
                System.out.println("This video path all ready exist");
                Resource resource = new UrlResource(targetPath.normalize(). toUri());
                if (resource.exists() && resource.isReadable()) {
                    return resource;
                }
            }
            Path thumbnailPath = ffmpegService
                    .generateThumbnail(
                            postMedia.getMediaUrl(),
                            targetPath.toString()
                    );
            Resource resource = new UrlResource(thumbnailPath.normalize().toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        }
        String postImage = postMedia.getMediaUrl();
        if (!StringUtils.hasText(postImage)) {
            throw new OperationNotPermittedException("PostMedia url is not stored in database!");
        }
        Path imagePath = Paths.get(postImage).normalize();
        Resource resource = new UrlResource(imagePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new OperationNotPermittedException("Image not found or not readable: " + postImage);
    }

    //@Override
    /*public Resource getPostContent(String postMediaId) throws MalformedURLException {
        PostMedia postMedia = postMediaRepository.findById(postMediaId)
                .orElseThrow(() -> new EntityNotFoundException("PostMedia is not found with id: " + postMediaId));
        Path mediaPath;
        if (postMedia.getType().startsWith("image/")) {
            mediaPath = Paths.get(postMedia.getMediaUrl()).toAbsolutePath().normalize();
        } else {
            mediaPath = Paths.get(postMedia.getMediaUrl() + File.separator + "master.m3u8").toAbsolutePath().normalize();
        }

        if (!Files.exists(mediaPath) || !Files.isReadable(mediaPath)) {
            throw new OperationNotPermittedException("Media file not found or not readable: " + mediaPath);
        }
        return new UrlResource(mediaPath.toUri());
    }*/

    @Override
    public Resource getPostContent(String postMediaId) throws MalformedURLException {
        PostMedia postMedia = postMediaRepository.findById(postMediaId)
                .orElseThrow(() -> new EntityNotFoundException("PostMedia is not found with id: " + postMediaId));

        Path mediaPath = Paths.get(postMedia.getMediaUrl()).toAbsolutePath().normalize();

        if (!Files.exists(mediaPath) || !Files.isReadable(mediaPath) || Files.isDirectory(mediaPath)) {
            throw new OperationNotPermittedException("Media file not found, not readable, or is a directory: " + mediaPath);
        }

        return new UrlResource(mediaPath.toUri());
    }

    @Override
    public Resource getStoryContent(String storyId) throws MalformedURLException {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new EntityNotFoundException("Story is not found with ID: " + storyId));
        Path mediaPath = Paths.get(story.getMediaUrl()).toAbsolutePath().normalize();

        if (!Files.exists(mediaPath) || !Files.isReadable(mediaPath) || Files.isDirectory(mediaPath)) {
            throw new OperationNotPermittedException("Media file not found, not readable, or is a directory: " + mediaPath);
        }
        return new UrlResource(mediaPath.toUri());
    }

    @Override
    public String uploadChatContent(
            @NonNull MultipartFile sourceFile,
            @NonNull String userId
    ) {
        final String finalUploadPath = chatBaseUrl + separator + "users" + separator + userId;
        File targetFolder = new File(finalUploadPath);
        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();
            if (!folderCreated) {
                LOGGER.warn("Failed to create the target folder: {}", targetFolder);
                return null;
            }
        }
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        final String targetFilePath = finalUploadPath + separator + currentTimeMillis() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            LOGGER.info("File saved to: {}", targetFilePath);
            return targetFilePath;
        } catch (IOException e) {
            LOGGER.error("File was not saved {0}", e);
            throw new OperationNotPermittedException("Internal error file is corrupted!");
        }
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
