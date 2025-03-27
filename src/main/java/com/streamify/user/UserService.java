package com.streamify.user;

import com.streamify.Storage.MediaService;
import com.streamify.common.Mapper;
import com.streamify.common.PageResponse;
import com.streamify.story.StoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final StoryRepository storyRepository;
    private final MediaService mediaService;

    public UserService(UserRepository userRepository, Mapper mapper, StoryRepository storyRepository, MediaService mediaService) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.storyRepository = storyRepository;
        this.mediaService = mediaService;
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
                .orElseThrow(() -> new EntityNotFoundException("User is not found with Id: " + user.getId()))
                .getFollowers()
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }

    @Transactional
    public List<UserDto> findUserFollowings(Authentication connectedUser) {
        Objects.requireNonNull(connectedUser, "Connected user must not be null");
        User user = (User) connectedUser.getPrincipal();
        return userRepository.findFollowingsWithDetails(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User is not found with Id: " + user.getId()))
                .getFollowing()
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
        Set<User> currentUserFollowings = currentUser.getFollowing();
        List<UserDto> filterUsers = users
                .stream()
                .map(mapper::toUserDto)
                .toList()
                .stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .filter(user -> !currentUserFollowings.stream()
                        .anyMatch(follow -> follow.getId().equals(user.getId()))
                )
                .toList();
        return PageResponse.<UserDto>builder()
                .content(filterUsers)
                .first(users.isFirst())
                .last(users.isLast())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .size(users.getTotalPages())
                .number(users.getNumber())
                .build();
    }

    public AboutAccount findAboutInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User is not found with username: " + username));
        return AboutAccount.builder()
                .id(user.getId())
                .username(user.getUsername())
                .joinedDate(generateJoinedDate(user.getCreateAt()))
                .accountBasedOn("India") //todo -> later go dynamic...
                .build();
    }

    private String generateJoinedDate(LocalDate createAt) {
        String month = createAt.getMonth().toString();
        String year = String.valueOf(createAt.getYear());
        return (month.charAt(0) + month.substring(1).toLowerCase()) + " " + year;
    }

    public Boolean isMutualFriend(String friendUsername, Authentication connectedUser) {
        User currentUser = (User) connectedUser.getPrincipal();
        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new EntityNotFoundException("User is not find with username: " + friendUsername));
        return currentUser.getFollowing().stream().anyMatch(u -> u.getId().equals(friend.getId()))
                &&
                currentUser.getFollowers().stream().anyMatch(u -> u.getId().equals(friend.getId()));
    }

    @Transactional
    public String uploadProfile(Authentication connectedUser, MultipartFile avtar) throws Exception {
        User user = (User) connectedUser.getPrincipal();
        String avtarUrl = mediaService.uploadProfile(user, avtar);
        if (avtarUrl.isBlank() || avtarUrl.isEmpty()) {
            throw new Exception("Failed to save avtar, try again!");
        }
        String previousAvtar = user.getProfilePictureUrl();
        user.setProfilePictureUrl(avtarUrl);
        User savedUser = userRepository.save(user);
        return mapper.toUserDto(savedUser).getAvtar();
    }

    @Transactional
    public String removeProfile(Authentication connectedUser) throws Exception {
        User user = (User) connectedUser.getPrincipal();
        String previousAvtar = user.getProfilePictureUrl();

        // now delete previous avtar
        if (previousAvtar != null) {
            boolean status = mediaService.deleteFile(previousAvtar);
            if (status) {
                log.info("Remove the previous profile of {} user", user.getUsername());
            } else {
                //todo -> also here the email to admin
                log.warn("Failed to remove previous profile of {} user", user.getUsername());
                log.warn("Avatar url is: {}", previousAvtar);
                throw new Exception("Something went wrong we are failed to remove profile!");
            }
        }
        // after deleting the file we are also remove from database
        user.setProfilePictureUrl(null);
        User savedUser = userRepository.save(user);
        // here we return the default profile in Base64 form
        return mapper
                .toUserDto(savedUser)
                .getAvtar();
    }

    @Transactional
    public void updateAccount(UpdateRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        user.setWebsite(request.getWebsite());
        user.setBio(request.getBio());
        user.setGender(request.getGender());
        // update user
        userRepository.save(user);
    }
}
