package com.streamify.Storage;

import com.streamify.post.PostMedia;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface MediaService {
    String uploadPostContent(MultipartFile sourceFile, String userId, String postId) throws IOException;

    boolean deletePostContent(List<PostMedia> sourceFiles) throws IOException;

    String uploadStoryContent(MultipartFile sourceFile, String storyId, String userId) throws IOException, InterruptedException;

    boolean deleteStoryContent(String mediaUrl) throws IOException;
}
