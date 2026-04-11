package be.notarium.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "userId")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 500)
    private String bio;

    private String website;

    private String github;

    private String linkedin;

    private String discord;

    private String discordId;

    @Column(nullable = false)
    @Builder.Default
    private boolean profilePublic = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean showInCarousel = false;

    @Column(nullable = false)
    @Builder.Default
    private String themePref = "dark";

    @Column(nullable = false)
    @Builder.Default
    private boolean adFree = false;

    private LocalDateTime adFreeUntil;
}
