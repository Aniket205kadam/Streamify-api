package com.streamify.aistudio.bot;

import com.streamify.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bots")
public class Bot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    private String profession;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String ethnicity;

    @Column(nullable = false, length = 500)
    private String bio;

    @ElementCollection
    @CollectionTable(name = "bot_interests", joinColumns = @JoinColumn(name = "bot_id"))
    @Column(name = "interest")
    private List<String> interests;

    @Column(nullable = false)
    private String avtar;

    @Column(nullable = false, length = 200)
    private String personality;

    @ManyToOne(fetch = FetchType.EAGER)
    private User creator;

    @Enumerated(EnumType.STRING)
    private BotType type;
}
