package kr.kdev.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.Map;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Amazon SNS Mobile Push using FCM Platform")
class AmazonSnsFcmTests {
    Logger log = LoggerFactory.getLogger(getClass());
    Gson gson = new GsonBuilder().create();

    static SnsClient snsClient;
    String platformApplicationArn;
    String endpointArn;
    String fcmDeviceToken = // for test
            "e6ZmJ3_GlntU_RUnYiegPS:APA91bGDelwwSka0lAs6f8PBoE7X6x98QtUztHT-oBf_NJ1Nr3Jl0brU6rfsQNaI-HAylhAenCKI2y9_Xr8VFFLWOcJh44FF6qRUZJfm-RQHOWTG3evTlaZBU2eAFBdQvFQmIqF6e12m";

    @BeforeAll
    static void init() {
        snsClient = SnsClient.builder()
                .credentialsProvider(ProfileCredentialsProvider.create())
                .region(Region.AP_NORTHEAST_2)
                .build();
    }

    @DisplayName("플랫폼 애플리케이션 유무 확인")
    @Order(0)
    @Test
    void whenGetPlatformApplications_thenFindFirst() {
        // when - get platform applications
        ListPlatformApplicationsResponse response = snsClient.listPlatformApplications();
        Assertions.assertTrue(response.hasPlatformApplications());

        Optional<PlatformApplication> maybePlatformApplication = response.platformApplications().stream().findFirst();
        Assertions.assertTrue(maybePlatformApplication.isPresent());

        log.info("{}", maybePlatformApplication.get());
        platformApplicationArn = maybePlatformApplication.get().platformApplicationArn();
    }

    @Order(1)
    @DisplayName("디바이스 토큰 또는 등록 ID 추가")
    @Test
    void givenToken_whenCreateEndpoint_whenSuccess() {
        // NOTE: https://docs.aws.amazon.com/ko_kr/sns/latest/dg/mobile-push-send-devicetoken.html
        Assertions.assertNotNull(platformApplicationArn);

        CreatePlatformEndpointRequest endpointRequest = CreatePlatformEndpointRequest.builder()
                .platformApplicationArn(platformApplicationArn)
                .token(fcmDeviceToken)
                .build();

        // when - 디바이스 엔드포인트 생성
        CreatePlatformEndpointResponse response = snsClient.createPlatformEndpoint(endpointRequest);

        // then - 엔드포인트를 만들면 EndpointArn이 반환
        this.endpointArn = response.endpointArn();
        log.info("{}", endpointArn);
        Assertions.assertNotNull(endpointArn);
        Assertions.assertTrue(endpointArn.endsWith("36d2b8ee-af26-3fd5-8fe0-91b7624ee188"));
    }

    @Order(2)
    @DisplayName("디바이스 엔드포인트로 메시지 게시")
    @Test
    void givenMessage_whenPublishMessage_thenSuccess() {
        // given - endpoint_arn
        Assertions.assertNotNull(endpointArn);

        String fcmMessage = gson.toJson(Map.of("data", Map.of("title", "Hi!", "body", "Mambo?!")));
        String message = gson.toJson(Map.of("GCM", fcmMessage));
        log.info("{}", message);

        PublishRequest publishRequest = PublishRequest.builder()
                .messageStructure("json")
                .message(message)
                .targetArn(endpointArn)
                .build();

        // when - publish
        PublishResponse response = snsClient.publish(publishRequest);
        String messageId = response.messageId();
        log.info("{}", messageId);

        // then - published
        Assertions.assertNotNull(messageId);
    }

    @Order(3)
    @DisplayName("디바이스에 대한 엔드포인트 삭제")
    @Test
    void givenEndpointArn_whenDeleteEndpoint_thenSuccess() {
        // given - endpoint_arn
        Assertions.assertNotNull(endpointArn);
        DeleteEndpointRequest endpointRequest = DeleteEndpointRequest.builder()
                .endpointArn(endpointArn)
                .build();

        // when - delete endpoint
        DeleteEndpointResponse response = snsClient.deleteEndpoint(endpointRequest);
        log.info("{}", response);

        // then - success
        Assertions.assertNotNull(response);
    }

    @AfterAll
    static void close() {
        if (snsClient != null) {
            snsClient.close();
        }
    }
}
