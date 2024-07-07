package kr.kdev.demo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@SpringBootTest
class FcmDemoApplicationTests {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private static final String SERVICE_ACCOUNT = "fcm/mambo-fcm-demo-firebase-adminsdk-duoca-b2161274f9.json";

    @Test
    void contextLoads() {
        Assertions.assertDoesNotThrow(this::init);
    }

    private void init() {
        ClassPathResource resource = new ClassPathResource(SERVICE_ACCOUNT);
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("Firebase App Initialize")
    @Test
    void givenServiceAccount_whenInitialize_thenSuccess() {
        ClassPathResource resource = new ClassPathResource(SERVICE_ACCOUNT);
        Assertions.assertTrue(resource.exists());

        Assertions.assertDoesNotThrow(() -> {
            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        });
    }

    @DisplayName("FCM send")
    @Test
    void givenToken_whenSend_thenSuccess() {
        Assertions.assertDoesNotThrow(this::init);

        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        Assertions.assertNotNull(firebaseApp);

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        Assertions.assertNotNull(firebaseMessaging);

        // given - token
        String token = "e6ZmJ3_GlntU_RUnYiegPS:APA91bGDelwwSka0lAs6f8PBoE7X6x98QtUztHT-oBf_NJ1Nr3Jl0brU6rfsQNaI-HAylhAenCKI2y9_Xr8VFFLWOcJh44FF6qRUZJfm-RQHOWTG3evTlaZBU2eAFBdQvFQmIqF6e12m";
        Message message = Message.builder()
                .setToken(token)
                .putData("title", "Notification")
                .putData("body", "Notification Body")
                .setNotification(Notification.builder()
                        .setTitle("Notification")
                        .setBody("Notification Body")
                        .build())
                .build();

        Assertions.assertDoesNotThrow(() -> {
            // when - send
            String messageId = firebaseMessaging.send(message);
            log.info("messageId: {}", messageId);

            // then - success
            Assertions.assertNotNull(messageId);
        });
    }
}
