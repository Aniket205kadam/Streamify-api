package com.streamify.Storage;

import com.streamify.post.PostMedia;
import com.streamify.user.User;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Service
public interface MediaService {
    String uploadPostContent(MultipartFile sourceFile, String userId, String postId) throws IOException;

    boolean deletePostContent(List<PostMedia> sourceFiles) throws IOException;

    String uploadStoryContent(MultipartFile sourceFile, String storyId, String userId) throws IOException, InterruptedException;

    boolean deleteStoryContent(String mediaUrl) throws IOException;

    Resource getUserProfileImageByUsername(String username) throws MalformedURLException;

    Resource getPostPreviewImage(String postId) throws IOException, InterruptedException;

    Resource getPostContent(String postMediaId) throws MalformedURLException;

    Resource getStoryContent(String storyId) throws MalformedURLException;

    String uploadChatContent(MultipartFile file, String senderId);

    String uploadProfile(User user, MultipartFile avtar);

    boolean deleteFile(String previousAvtar);
}
