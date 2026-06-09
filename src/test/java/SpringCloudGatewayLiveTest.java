import com.hch.SpringCloudGatewayApplication;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = SpringCloudGatewayApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public class SpringCloudGatewayLiveTest {

    private static final String TEST_URL = "http://localhost:8080";

    private final TestRestTemplate testRestTemplate = new TestRestTemplate().withRedirects(HttpRedirects.DONT_FOLLOW);

    @Test
    public void generalTest() {
        ResponseEntity<String> response = testRestTemplate
                .getForEntity(TEST_URL + "/book-service/books", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        response = testRestTemplate.getForEntity(TEST_URL + "/home/browser/index.html", String.class);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("/login", Objects.requireNonNull(response.getHeaders().get("Location")).get(0));

        HttpEntity<String> httpEntity = new HttpEntity<>(loginHeaders("user", "password"));
        response = testRestTemplate.exchange(TEST_URL + "/book-service/books/1", HttpMethod.GET, httpEntity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        response = testRestTemplate.exchange(TEST_URL + "/rating-service/ratings/all", HttpMethod.GET, httpEntity, String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        httpEntity = new HttpEntity<>(loginHeaders("admin", "admin"));
        response = testRestTemplate.exchange(TEST_URL + "/rating-service/ratings/all", HttpMethod.GET, httpEntity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        var params = Map.of("user", "admin", "password", "admin");
        response = testRestTemplate.getForEntity("http://localhost:8080", String.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void whenAccessingBooksWithoutAuthentication_thenRedirectsToLogin() {
        ResponseEntity<String> response = testRestTemplate
                .getForEntity(TEST_URL + "/book-service/books", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void whenUserIsAuthenticated_thenCanAccessBook() {
        HttpEntity<String> httpEntity = new HttpEntity<>(loginHeaders("user", "password"));

        ResponseEntity<String> response = testRestTemplate
                .exchange(TEST_URL + "/book-service/books/1", HttpMethod.GET, httpEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void whenAdminIsAuthenticated_thenCanAccessAllRatings() {
        HttpEntity<String> httpEntity = new HttpEntity<>(loginHeaders("admin", "admin"));

        ResponseEntity<String> response = testRestTemplate
                .exchange(TEST_URL + "/rating-service/ratings/all", HttpMethod.GET, httpEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    private HttpHeaders loginHeaders(String username, String password) {
        TestRestTemplate loginClient = new TestRestTemplate().withRedirects(HttpRedirects.DONT_FOLLOW);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("password", password);

        ResponseEntity<String> response = loginClient.postForEntity(TEST_URL + "/login", form, String.class);
        String sessionCookie = Objects.requireNonNull(response.getHeaders().get("Set-Cookie")).get(0).split(";")[0];

        return new HttpHeaders(MultiValueMap.fromSingleValue(Map.of("Cookie", sessionCookie)));
    }
}
