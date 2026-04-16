package be.freenote;

import be.freenote.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("integration")
class FreenoteApplicationTests extends AbstractIntegrationTest {

    @MockitoBean private JavaMailSender mailSender;

    @Test
    void contextLoads() {
    }
}
