package com.braja;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import okhttp3.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MainTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(8080), false);

    private OkHttpClient client;

    @Before
    public void configureStubs() {

        stubFor(get(urlEqualTo("/test/bodyFile"))
                .willReturn(aResponse().withBodyFile("output.json")));
        stubFor(get(urlMatching("/test/.*")).atPriority(10)
                .willReturn(aResponse().withStatus(401)));
        stubFor(get(urlEqualTo("/test/abc"))
                .willReturn(aResponse()
                        .withHeader("x-trace-id","abc")
                        .withHeader("x-token","def")
                        .withHeader("x-token","ghi")
                        .withHeader("x-array", "jkl", "mno")
                        .withBody("Test success!")));
        stubFor(delete("/test/delete").willReturn(okJson("Deleted!")));
        stubFor(get("/test/get").willReturn(okJson("{\"name\": \"John\"}")));
        stubFor(post("/test/post").withRequestBody(equalToJson("{\"name\":\"Hendro\"}")).willReturn(okJson("Posted!")));
        stubFor(post("/test/unauthorized").willReturn(unauthorized()));
        stubFor(put("/test/status-only").willReturn(status(418)));

        client = new OkHttpClient();
    }

    @Test
    public void exampleTestBodyFile() throws IOException {

        Request request = new Request.Builder()
                .url("http://localhost:8080/test/bodyFile")
                .get()
                .build();

        Response response = client.newCall(request).execute();
        assertNotNull(response);
        assertNotNull(response.body());
        System.out.println("resp body : " + response.body().string());
    }
    @Test
    public void exampleTest401() throws IOException {

        Request request = new Request.Builder()
                .url("http://localhost:8080/test/abcde")
                .get()
                .build();

        Response response = client.newCall(request).execute();
        assertNotNull(response);

        int status = response.code();
        System.out.println(status);

        assertEquals(401, status);
    }

    @Test
    public void exampleTestAbc() throws IOException {

        Request request = new Request.Builder()
                .url("http://localhost:8080/test/abc")
                .get()
                .build();

        Response response = client.newCall(request).execute();
        assertNotNull(response);
        assertNotNull(response.body());

        String responseBody = response.body().string();
        String responseHeader = response.header("x-trace-id");
        System.out.println("resp body : " + responseBody);
        System.out.println("resp header : " + responseHeader);
        System.out.println("resp header : " + response.header("x-token"));
        System.out.println("resp header : " + response.headers("x-token"));
        System.out.println("resp header : " + response.header("x-array"));
        System.out.println("resp header : " + response.headers("x-array"));

        assertEquals("Test success!", responseBody);
        assertEquals("abc", responseHeader);
    }

    @Test
    public void exampleTestDelete() throws IOException {

        Request request = new Request.Builder()
                .url("http://localhost:8080/test/delete")
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        assertNotNull(response);
        assertNotNull(response.body());

        String responseBody = response.body().string();
        System.out.println(responseBody);

        assertEquals("Deleted!", responseBody);
    }

    @Test
    public void exampleTestPost() throws IOException {

        String json = "{\"name\":\"Hendro\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("http://localhost:8080/test/post")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        assertNotNull(response);
        assertNotNull(response.body());
        assertEquals(200, response.code());

        String responseBody = response.body().string();
        System.out.println(responseBody);

        assertEquals("Posted!", responseBody);
    }

    @Test
    public void exampleTestUnauthorized() throws IOException {

        RequestBody body = RequestBody.create("", MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("http://localhost:8080/test/unauthorized")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        assertNotNull(response);

        System.out.println(response.code());

        assertEquals(401, response.code());
    }

    @Test
    public void exampleTestFileMapping() throws IOException {

        Request request = new Request.Builder()
                .url("http://localhost:8080/mappings/other")
                .get()
                .build();

        Response response = client.newCall(request).execute();
        assertNotNull(response);
        assertNotNull(response.body());

        String responseBody = response.body().string();
        System.out.println(responseBody);

        assertEquals(200, response.code());
    }

}
