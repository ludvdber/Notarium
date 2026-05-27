package be.freenote.entity;

import be.freenote.enums.AvatarSource;
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

    @Column(length = 100)
    private String displayName;

    @Column(length = 500)
    private String bio;

    private String website;

    private String github;

    private String linkedin;

    private String discord;

    @Column(nullable = false)
    @Builder.Default
    private boolean profilePublic = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean showInCarousel = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean adFree = false;

    private LocalDateTime adFreeUntil;

    private LocalDateTime termsAcceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_source", nullable = false, length = 20)
    @Builder.Default
    private AvatarSource avatarSource = AvatarSource.AUTO;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "display_real_name", nullable = false)
    @Builder.Default
    private boolean displayRealName = false;
}
