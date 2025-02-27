package com.streamify.user;

import com.streamify.common.Mapper;
import com.streamify.common.PageResponse;
import com.streamify.story.StoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public List<UserDto> findUserFollowers(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return user.getFollowers()
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }

    public List<UserDto> findUserFollowings(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return user.getFollowing()
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }

    public boolean connectedUserHasStory(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return storyRepository.isValidStoryExist(user.getId());
    }

    public PageResponse<UserDto> searchUsers(String query, int page, int size, Authentication connectedUser) {
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

    public String addRecentSearch(String username, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        User searchedUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Username is not found with username: " + username)
                );
        user.getRecentSearchedUser().add(mapper.toSearchedUser(searchedUser, user));
        /*prevRecentSearched.add(mapper.toSearchedUser(searchedUser, user));
        user.setRecentSearchedUser(prevRecentSearched);*/
        return userRepository.save(user).getId();
    }

    public List<UserDto> getRecentSearch(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return findUserById(user.getId())
                .getRecentSearchedUser()
                .stream()
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
}
