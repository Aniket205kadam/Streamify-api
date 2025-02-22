package com.streamify.story;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, String> {
}
