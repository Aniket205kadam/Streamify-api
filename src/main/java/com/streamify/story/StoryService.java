package com.streamify.story;

import com.streamify.Storage.MediaService;
import com.streamify.common.Mapper;
import com.streamify.common.PageResponse;
import com.streamify.exception.OperationNotPermittedException;
import com.streamify.user.User;
import com.streamify.user.UserDto;
import com.streamify.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StoryService {
    private final MediaService mediaService;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final StoryViewRepository storyViewRepository;
    private final StoryReplyRepository storyReplyRepository;
    private final Mapper mapper;

    public StoryService(MediaService mediaService, StoryRepository storyRepository, UserRepository userRepository, StoryViewRepository storyViewRepository, StoryReplyRepository storyReplyRepository, Mapper mapper) {
        this.mediaService = mediaService;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.storyViewRepository = storyViewRepository;
        this.storyReplyRepository = storyReplyRepository;
        this.mapper = mapper;
    }

    @Transactional
    public String addStory(String caption, boolean isArchived, MultipartFile content, Authentication connectedUser) throws IOException, InterruptedException {
        User user = (User) connectedUser.getPrincipal();
        Story story = Story.builder()
                .caption(caption)
                .isArchived(isArchived)
                .type(StoryType.IMAGE)
                .user(user)
                .mediaUrl("")
                .expiredAt(LocalDateTime.now().plusHours(12))
                .build();
        Story savedStory = storyRepository.save(story);
        String storageUrl = mediaService.uploadStoryContent(content, savedStory.getId(), user.getId());
        savedStory.setMediaUrl(storageUrl);
        return storyRepository.save(savedStory).getId();
    }

    public StoryResponse findStoryById(String storyId) {
        Story story = storyRepository.findValidStoryById(storyId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Story is not found with ID: " + storyId)
                );
        Set<StoryViewDto> storyViewers = story.getViews()
                .stream()
                .map(view -> StoryViewDto.builder()
                    .id(view.getId())
                    .storyId(view.getStory().getId())
                    .viewer(UserDto.builder()
                            .id(view.getViewer().getId())
                            .username(view.getViewer().getUsername())
                            .avtarUrl(null) //todo -> impl user avtar
                            .build()
                    )
                    .viewedAt(view.getViewedAt())
                    .build()
                ).collect(Collectors.toCollection(LinkedHashSet::new));
        return StoryResponse.builder()
                .id(story.getId())
                .caption(story.getCaption())
                .type(story.getType())
                .user(UserDto.builder()
                        .id(story.getUser().getId())
                        .username(story.getUser().getUsername())
                        .avtarUrl(null)
                        .build()
                )
                .viewer(storyViewers)
                .createdAt(story.getCreatedAt())
                .modifiedAt(story.getModifiedAt())
                .expiredAt(story.getExpiredAt())
                .build();
    }

    public List<StoryResponse> findStoriesByUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found with ID: " + userId)
                );
        List<Story> stories = storyRepository.findAllValidUserStories(user.getId());
        return stories.stream().map(story -> {
            List<StoryViewDto> storyViewers = story.getViews()
                    .stream()
                    .map(view -> StoryViewDto.builder()
                            .id(view.getId())
                            .storyId(view.getStory().getId())
                            .viewer(UserDto.builder()
                                    .id(view.getViewer().getId())
                                    .username(view.getViewer().getUsername())
                                    .avtarUrl(null) //todo -> impl user avtar
                                    .build()
                            )
                            .viewedAt(view.getViewedAt())
                            .build()
                    ).toList();
            return StoryResponse.builder()
                    .id(story.getId())
                    .caption(story.getCaption())
                    .type(story.getType())
                    .user(UserDto.builder()
                            .id(story.getUser().getId())
                            .username(story.getUser().getUsername())
                            .avtarUrl(null)
                            .build()
                    )
                    .viewer(storyViewers
                            .stream()
                            .sorted((firstStory, secondStory) ->
                                    secondStory.getViewedAt().compareTo(firstStory.getViewedAt()))
                            .collect(Collectors.toCollection(LinkedHashSet::new))
                    )
                    .createdAt(story.getCreatedAt())
                    .modifiedAt(story.getModifiedAt())
                    .expiredAt(story.getExpiredAt())
                    .build();
        }).toList();
    }

    private Story getStoryById(@NonNull String storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Story is not found with ID: " + storyId)
                );
    }

    private User getUserById(@NonNull String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User is not found with ID: " + userId)
                );
    }

    public void viewStory(String storyId, String viewerId) {
        Story story = getStoryById(storyId);
        User viewer = getUserById(viewerId);
        if (story.getViews().isEmpty()) {
            story.setViews(new LinkedHashSet<>());
        }
        boolean isViewerAlreadyPresent = story.getViews()
                .stream()
                .anyMatch(storyView ->
                        storyView.getViewer().getId().equals(viewer.getId())
                );
        if (isViewerAlreadyPresent) return;
        StoryView view = storyViewRepository.save(StoryView.builder()
                .story(story)
                .viewer(viewer)
                .viewedAt(LocalDateTime.now())
                .build()
        );
        story.getViews().add(view);
        storyRepository.save(story);
    }

    @Transactional
    public void sendReplyToStory(String content, String storyId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Story story = getStoryById(storyId);
        StoryReply reply = StoryReply.builder()
                .content(content)
                .user(user)
                .story(story)
                .build();
        storyReplyRepository.save(reply);
    }

    public PageResponse<StoryReplyResponse> findAllStoryReplies(String storyId, Authentication connectedUser, int page, int size) {
        User user = (User) connectedUser.getPrincipal();
        Story story = getStoryById(storyId);
        // only story owner can read the reply on the story
        if (!story.getUser().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are not the story owner, so you can't read the replies");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<StoryReply> storyReplies = storyReplyRepository.findAllStoryReplies(pageable, story.getId());
        List<StoryReplyResponse> replyResponses = storyReplies
                .stream()
                .map(mapper::toStoryReplyResponse)
                .toList();
        return PageResponse.<StoryReplyResponse>builder()
                .content(replyResponses
                        .stream()
                        .sorted((firstReply, secondReply) ->
                                secondReply.getCreatedAt().compareTo(firstReply.getCreatedAt())
                        )
                        .toList()
                )
                .number(storyReplies.getNumber())
                .size(storyReplies.getSize())
                .totalPages(storyReplies.getTotalPages())
                .totalElements(storyReplies.getTotalElements())
                .first(storyReplies.isFirst())
                .last(storyReplies.isLast())
                .build();
    }

    @Transactional
    public String updateStory(String storyId, String caption, boolean isArchived, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Story story = getStoryById(storyId);
        if (!story.getUser().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You don;t have to permission to update the story");
        }
        story.setCaption(caption);
        story.setArchived(isArchived);
        return storyRepository.save(story).getId();
    }

    @Transactional
    public void deleteStoryById(String storyId, Authentication connectedUser) throws IOException {
        User user = (User) connectedUser.getPrincipal();
        Story story = getStoryById(storyId);
        if (!story.getUser().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You don;t have to permission to delete the story");
        }
        boolean isStoryContentDeleted = mediaService.deleteStoryContent(story.getMediaUrl());
        if (!isStoryContentDeleted) {
            throw new OperationNotPermittedException("Story is not deleted due to some reasons!");
        }
        storyRepository.deleteById(storyId);
    }

    public List<FollowingsStoryResponse> findAllFollowingsStories(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        List<FollowingsStoryResponse> followingsStoryResponses = new ArrayList<>();

        System.out.println("Followings count: " + user.getFollowing().size());

        for (User following : user.getFollowing()) {
            List<Story> followingsStories = storyRepository.findAllValidUserStories(following.getId());

            if (!followingsStories.isEmpty()) {
                boolean isViewAllStories = followingsStories.stream()
                        .allMatch(story -> story.getViews().stream()
                                .anyMatch(view -> view.getViewer().equals(user))
                        );

                followingsStoryResponses.add(FollowingsStoryResponse.builder()
                        .id(following.getId())
                        .username(following.getUsername())
                        .allStoriesSeen(isViewAllStories)
                        .build());
            }
        }
        return followingsStoryResponses;
    }

}
