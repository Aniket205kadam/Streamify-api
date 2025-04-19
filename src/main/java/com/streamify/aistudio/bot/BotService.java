package com.streamify.aistudio.bot;

import com.streamify.Storage.FileUtils;
import com.streamify.common.Mapper;
import com.streamify.exception.OperationNotPermittedException;
import com.streamify.user.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.io.File.separator;

@Service
public class BotService {
    private static final Logger log = LoggerFactory.getLogger(BotService.class);
    private final BotRepository repository;
    private final Mapper mapper;

    @Value("${application.file.upload.content-base-url.bot}")
    private String botAvtarUrl;

    public BotService(BotRepository repository, Mapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        botAvtarUrl = botAvtarUrl.replace("/", separator);
    }

    @Transactional
    public void createBot(BotRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Bot bot = Bot.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .profession(request.getProfession())
                .age(request.getAge())
                .gender(request.getGender())
                .ethnicity(request.getEthnicity())
                .bio(request.getBio())
                .interests(request.getInterests())
                .avtar(request.getAvtar())
                .personality(request.getPersonality())
                .creator(user)
                .type(BotType.PRIVATE)
                .build();
        repository.save(bot);
        log.info("Bot is successfully created with name {} {}", request.getFirstname(), request.getLastname());
    }

    public String uploadBotAvtar(MultipartFile avtar, Authentication connectedUser) {
        if (!Objects.requireNonNull(avtar.getContentType()).startsWith("image/")) {
            throw new OperationNotPermittedException("Bot avtar is only in the image form!");
        }
        File targetFolder = new File(botAvtarUrl);
        if (!targetFolder.exists()) {
            boolean isFolderCreated = targetFolder.mkdirs();
            if (!isFolderCreated) {
                log.error("Failed to create the target folder");
                throw new IllegalStateException("Failed to create the target folder");
            }
            log.info("Successfully create the target folder");
        }
        final String fileExtension = FileUtils.getFileExtension(avtar.getOriginalFilename());
        final String filename = UUID.randomUUID() + "." + fileExtension;
        final String avtarPath = botAvtarUrl + File.separator + filename;
        Path targetPath = Paths.get(avtarPath);
        try {
            Files.write(targetPath, avtar.getBytes());
            return filename;
        } catch (IOException e) {
            throw new OperationNotPermittedException("Failed to save bot avtar!");
        }
    }

    public List<BotResponse> getMyBots(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return repository.findMyBot(user.getId())
                .stream()
                .map(bot -> mapper.toBotResponse(bot, botAvtarUrl + separator + bot.getAvtar()))
                .toList();
    }
}
