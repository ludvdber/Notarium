package be.freenote.integration;

import be.freenote.entity.*;
import be.freenote.enums.Category;
import be.freenote.repository.*;
import be.freenote.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests. Starts PostgreSQL and Redis via Testcontainers
 * using the singleton pattern so that containers are shared across all test classes
 * even when Spring creates separate ApplicationContexts (different @MockitoBean sets).
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.autoconfigure.exclude=org.springframework.boot.mail.autoconfigure.MailHealthContributorAutoConfiguration"
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer postgres = createPostgres();
    @SuppressWarnings("resource")
    static final GenericContainer<?> redis = createRedis();

    static {
        postgres.start();
        redis.start();
    }

    @SuppressWarnings({"rawtypes", "unchecked", "resource"})
    private static PostgreSQLContainer createPostgres() {
        return new PostgreSQLContainer(
                DockerImageName.parse("pgvector/pgvector:pg17")
        ).withDatabaseName("freenote_test")
         .withUsername("freenote")
         .withPassword("freenote");
    }

    @SuppressWarnings("resource")
    private static GenericContainer<?> createRedis() {
        return new GenericContainer<>(
                DockerImageName.parse("redis:7-alpine")
        ).withExposedPorts(6379);
    }

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JwtTokenProvider jwtTokenProvider;
    @Autowired protected StringRedisTemplate redisTemplate;

    @Autowired protected UserRepository userRepository;
    @Autowired protected SectionRepository sectionRepository;
    @Autowired protected CourseRepository courseRepository;
    @Autowired protected DocumentRepository documentRepository;
    @Autowired protected RatingRepository ratingRepository;
    @Autowired protected FavoriteRepository favoriteRepository;
    @Autowired protected BadgeRepository badgeRepository;
    @Autowired protected DonationRepository donationRepository;

    // --- Factory helpers ---

    protected User createUser(String username, boolean verified, String role) {
        User user = User.builder()
                .oauthProvider("DISCORD")
                .oauthId("oauth-" + username)
                .username(username)
                .verified(verified)
                .role(role)
                .xp(0)
                .build();
        user = userRepository.save(user);
        UserProfile profile = UserProfile.builder().user(user).build();
        user.setProfile(profile);
        return userRepository.save(user);
    }

    protected User createVerifiedUser(String username) {
        return createUser(username, true, "USER");
    }

    protected Section createSection(String name) {
        return sectionRepository.save(
                Section.builder().name(name).approved(true).build()
        );
    }

    protected Course createCourse(String name, Section section, User createdBy) {
        return courseRepository.save(
                Course.builder().name(name).section(section).approved(true).createdBy(createdBy).build()
        );
    }

    protected Document createDocument(String title, Course course, User user) {
        return documentRepository.save(
                Document.builder()
                        .title(title)
                        .course(course)
                        .user(user)
                        .category(Category.SYNTHESE)
                        .fileKey("test/" + title.toLowerCase().replace(' ', '-') + ".pdf")
                        .fileSize(1024L)
                        .language("FR")
                        .build()
        );
    }

    protected String jwtFor(User user) {
        return jwtTokenProvider.generateToken(user);
    }
}
