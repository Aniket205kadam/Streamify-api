package com.streamify.user;

import com.streamify.common.Mapper;
import com.streamify.common.PageResponse;
import com.streamify.story.StoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final StoryRepository storyRepository;

    public UserService(UserRepository userRepository, Mapper mapper, StoryRepository storyRepository) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.storyRepository = storyRepository;
    }

    public User findUserById(@NonNull String userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("This user is not found with ID: " + userId)
                );
    }

    public UserResponse findUserByUsername(@NonNull String username) {
        return userRepository.findByUsername(username)
                .map(mapper::toUserResponse)
                .orElseThrow(
                        () -> new EntityNotFoundException("This user is not found with username: " + username)
                );
    }

    @Transactional
    public String followUser(String followingId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        User followUser = findUserById(followingId);

        // Add user to followUser's followers list
        followUser.getFollowers().add(user);
        int followerCount = followUser.getFollowers().size();
        followUser.setFollowerCount(followerCount);

        // Add followUser to user's following list
        user.getFollowing().add(followUser);
        int followingCount = user.getFollowing().size();
        user.setFollowingCount(followingCount);

        // Save both users to update both sides of the relationship
        userRepository.save(user);
        userRepository.save(followUser);
        return "Followed " + followUser.getUsername();
    }

    @Transactional
    public List<UserDto> findUserFollowers(Authentication connectedUser) {
        Objects.requireNonNull(connectedUser, "Connected user must not be null");
        User user = (User) connectedUser.getPrincipal();
        return userRepository.findFollowersWithDetails(user.getId())
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }

    @Transactional
    public List<UserDto> findUserFollowings(Authentication connectedUser) {
        Objects.requireNonNull(connectedUser, "Connected user must not be null");
        User user = (User) connectedUser.getPrincipal();
        return userRepository.findFollowingsWithDetails(user.getId())
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }

    public boolean connectedUserHasStory(Authentication connectedUser) {
        Objects.requireNonNull(connectedUser, "Connected user must not be null");
        User user = (User) connectedUser.getPrincipal();
        return storyRepository.isValidStoryExist(user.getId());
    }

    @Transactional
    public PageResponse<UserDto> searchUsers(String query, int page, int size, Authentication connectedUser) {
        Objects.requireNonNull(connectedUser, "Connected user must not be null");

        User currentUser = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.searchUsers(pageable, query);
        List<UserDto> searchedUsers = users.stream()
                .map(user -> mapper.toSearchedUser(user, currentUser))
                .toList();
        return PageResponse.<UserDto>builder()
                .content(searchedUsers)
                .number(users.getNumber())
                .size(users.getSize())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .last(users.isLast())
                .first(users.isFirst())
                .build();
    }

    @Transactional
    public String addRecentSearch(String username, Authentication connectedUser) {
        Objects.requireNonNull(username, "Username must not be null");
        Objects.requireNonNull(connectedUser, "Connected user must not be null");

        User user = (User) connectedUser.getPrincipal();
        User searchedUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Username is not found with username: " + username)
                );
        /*user.getRecentSearchedUser().add(mapper.toSearchedUser(searchedUser, user));*/
        /*return userRepository.save(user).getId();*/
/*
        userRepository.findRecentSearchesWithDetails(user.getId()).add(mapper.toSearchedUser(searchedUser, user))
*/
        return null;
    }

    @Transactional
    public List<UserDto> getRecentSearch(Authentication connectedUser) {
        Objects.requireNonNull(connectedUser, "Connected user must not be null");

        User user = (User) connectedUser.getPrincipal();
        return userRepository.findRecentSearchesWithDetails(user.getId())
                .stream()
                .map(mapper::toUserDto)
                .distinct()
                .toList();
    }

    public Boolean isFollowingUser(String userId, Authentication connectedUser) {
        User currentUser = (User) connectedUser.getPrincipal();
        return currentUser
                .getFollowing()
                .stream()
                .anyMatch((user) ->
                        user.getId().equals(userId)
                );
    }

    /*public PageResponse<UserDto> getSuggestedUsers(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Set<User> suggestedFriends = new HashSet<>();
        Map<String, Integer> priority = new HashMap<>();

        // Suggest friends of friends
        if (!user.getFollowing().isEmpty()) {
            for (User following : user.getFollowing()) {
                following.getFollowing().forEach(friend -> {
                    if (!friend.equals(user)) {
                        suggestedFriends.add(friend);
                        priority.merge(friend.getUsername(), 1, Integer::sum);
                    }
                });
            }
        }

        // If suggested friends are less than 20, add famous users
        int remainingCount = Math.max(0, 20 - suggestedFriends.size());
        if (remainingCount > 0) {
            Pageable pageable = PageRequest.of(0, remainingCount);
            Page<User> famousUsers = userRepository.findMostFollowingCountUsers(pageable);
            famousUsers.forEach(famousUser -> {
                suggestedFriends.add(famousUser);
                priority.putIfAbsent(famousUser.getUsername(), 0);
            });
        }

        // Sort users based on priority (higher values first)
        List<String> sortedUsers = priority.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        // Convert sorted users to UserDto list
        List<UserDto> prioritySuggestedUsers = sortedUsers.stream()
                .map(username -> suggestedFriends.stream()
                        .filter(userObj -> userObj.getUsername().equals(username))
                        .findFirst()
                        .map(mapper::toUserDto)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        // Pagination logic
        int startPoint = page * size;
        int endPoint = Math.min(startPoint + size, prioritySuggestedUsers.size());
        int totalPages = (int) Math.ceil((double) prioritySuggestedUsers.size() / size);

        return PageResponse.<UserDto>builder()
                .content(prioritySuggestedUsers.subList(startPoint, endPoint))
                .totalPages(totalPages)
                .totalElements(prioritySuggestedUsers.size())
                .first(page == 0)
                .last(endPoint == prioritySuggestedUsers.size())
                .build();
    }*/

    public PageResponse<UserDto> getSuggestedUsers2(int page, int size, Authentication connectedUser) {
        User currentUser = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("followingCount").ascending());
        Page<User> users = userRepository.findAll(pageable);
        return PageResponse.<UserDto>builder()
                .content(
                        (users.stream().map(mapper::toUserDto).toList())
                                .stream().filter(user -> user.getId() != currentUser.getId()).toList())
                .first(users.isFirst())
                .last(users.isLast())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .size(users.getTotalPages())
                .number(users.getNumber())
                .build();
    }
}
