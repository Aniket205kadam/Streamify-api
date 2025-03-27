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
import java.util.*;
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

        // user can only upload image and video
        if (!(content.getContentType().startsWith("image/") || content.getContentType().startsWith("video/"))) {
            throw new OperationNotPermittedException("For the story you can only upload the image or 15s video");
        }
        Story story = Story.builder()
                .caption(caption)
                .isArchived(isArchived)
                .type(content.getContentType().startsWith("image/") ? StoryType.IMAGE : StoryType.VIDEO)
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

    public List<StoryResponse> findStoriesByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found with username: " + username)
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
                                    .avtarUrl(null)
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

    @Transactional
    public void viewStory(String storyId, Authentication connectedUser) {
        User currentUser = (User) connectedUser.getPrincipal();
        Story story = getStoryById(storyId);
        User viewer = getUserById(currentUser.getId());
        if (story.getViews() == null) {
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
        story.setReplyCount(story.getReplyCount() + 1);
        storyReplyRepository.save(reply);
        storyRepository.save(story);
    }

    public PageResponse<StoryViewDto> findAllStoryViewer(String storyId, Authentication connectedUser, int page, int size) {
        User user = (User) connectedUser.getPrincipal();
        Story story = getStoryById(storyId);
        // only story owner can see the views on the story
        if (!story.getUser().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are not the story owner, so you can't read the replies");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("viewedAt").ascending());
        Page<StoryView> storyViews = storyViewRepository.findAllStoryViews(pageable, story.getId());
        List<StoryViewDto> views = storyViews
                .stream()
                .map(mapper::toStoryViewDto)
                .toList();
        return PageResponse.<StoryViewDto>builder()
                // filter out the owner of the story, if exists
                .content(views
                        .stream()
                        .filter(view ->
                                !view.getViewer()
                                        .getId()
                                        .equals(user.getId())
                        ).toList()
                )
                .number(storyViews.getNumber())
                .size(storyViews.getSize())
                .totalPages(storyViews.getTotalPages())
                .totalElements(storyViews.getTotalElements())
                .first(storyViews.isFirst())
                .last(storyViews.isLast())
                .build();
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

    @Transactional
    public void likeStory(String storyId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Story story = storyRepository.findStoryWithLikedUsersById(storyId)
                .orElseThrow(() -> new EntityNotFoundException("Story is not found with ID: " + storyId));
        if (LocalDateTime.now().isAfter(story.getExpiredAt())) {
            throw new OperationNotPermittedException("This story has expired. You can't like it!");
        }
        Set<UserDto> storyLikedUsers = story.getLikedUsers();
       if (storyLikedUsers.stream().anyMatch(likes -> likes.getId().equals(user.getId()))) {
           // Unlike the story
           Optional<UserDto> likedUsers = storyLikedUsers.stream().filter(likes -> likes.getId().equals(user.getId())).findFirst();
           storyLikedUsers.remove(likedUsers.orElseThrow(() -> new OperationNotPermittedException("Contact to the admin, this is Internal error.")));
       } else {
           // Like the story
           storyLikedUsers.add(mapper.toUserDto(user));
       }
       story.setLikedUsers(storyLikedUsers);
       story.setLikeCount(storyLikedUsers.size());
       storyRepository.save(story);
    }

    public Boolean isLikedStoryUser(String storyId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Story story = storyRepository.findStoryWithLikedUsersById(storyId)
                .orElseThrow(() -> new EntityNotFoundException("Story is not found with ID: " + storyId));
        for (UserDto userDto : story.getLikedUsers()) {
            System.out.println("LIKE BY :" + userDto.getUsername());
        }
        return story
                .getLikedUsers()
                .stream().anyMatch(likes -> likes.getId().equals(user.getId()));
    }

    public List<UserDto> findAllStoryLikedUsers(String storyId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Story story = storyRepository.findStoryWithLikedUsersById(storyId)
                .orElseThrow(() -> new EntityNotFoundException("Story is not found with ID: " + storyId));
        if (!user.getId().equals(story.getUser().getId())) {
            throw new OperationNotPermittedException("You are not the story owner, so you can't see the story liked users");
        }
        Set<UserDto> likedUsers = story.getLikedUsers();
        Set<User> orgUsers = new HashSet<>();
        // optimized later
        for (UserDto u : likedUsers) {
            orgUsers.add(userRepository
                    .findById(u.getId())
                    .orElseThrow(() ->
                            new EntityNotFoundException("User is not found with Id: " + u.getId())
                    ));
        }
        return orgUsers
                .stream()
                .map(mapper::toUserDto).toList();
    }
}
