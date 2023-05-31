package id.semantics.soya.test;

import id.semantics.soya.Service;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

public class TestService {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    public static TestResponse request(String method, String path, String requestBody) {
        try {
            URL url = new URL("http://localhost:2806" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            if (method.equals("POST")) {
                connection.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter osWriter = new OutputStreamWriter(connection.getOutputStream());
                osWriter.write(requestBody);
                osWriter.flush();
            }
            connection.connect();
            try {
                String body = IOUtils.toString(connection.getInputStream(), "UTF-8");
                return new TestResponse(connection.getResponseCode(), body);
            } catch (IOException e) {
                String error = IOUtils.toString(connection.getErrorStream(), "UTF-8");
                return new TestResponse(connection.getResponseCode(), error);
            }

        } catch (IOException e) {
            e.printStackTrace();
            fail("Sending request failed: " + e.getMessage());
            return new TestResponse();
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        Service.main(null);
        awaitInitialization();

    }

    @AfterClass
    public static void tearDown() throws Exception {
        stop();
    }

    @Test
    public void testHello() {
        String testUrl = "/hello";
        TestResponse res = request("GET", testUrl, null);
        assertEquals(200, res.status);
        assertEquals("hello", res.body);
        log.info(res.toString());
    }

    @Test
    public void testUsagePolicyEqual() throws IOException {
        String testUrl = "/api/validate/usagepolicy";

        InputStream dpvIS = TestService.class.getClassLoader().getResourceAsStream("dpv/tester-equal.json");
        String usagePolicyString = IOUtils.toString(dpvIS, "UTF-8");
        dpvIS.close();
        TestResponse res = request("POST", testUrl, usagePolicyString);
        assertEquals(200, res.status);
    }

    @Test
    public void testUsagePolicyValid() throws IOException {
        String testUrl = "/api/validate/usagepolicy";

        InputStream dpvIS = TestService.class.getClassLoader().getResourceAsStream("dpv/tester.json");
        String usagePolicyString = IOUtils.toString(dpvIS, "UTF-8");
        dpvIS.close();
        TestResponse res = request("POST", testUrl, usagePolicyString);
        assertEquals(200, res.status);
    }

    @Test
    public void testUsagePolicyInvalidTime() throws IOException {
        String testUrl = "/api/validate/usagepolicy";

        InputStream dpvIS = TestService.class.getClassLoader().getResourceAsStream("dpv/tester-invalid-time.json");
        String usagePolicyString = IOUtils.toString(dpvIS, "UTF-8");
        dpvIS.close();
        TestResponse res = request("POST", testUrl, usagePolicyString);
        assertEquals(500, res.status);
    }

    @Test
    public void testUsagePolicyInvalidProcessing() throws IOException {
        String testUrl = "/api/validate/usagepolicy";

        InputStream dpvIS = TestService.class.getClassLoader().getResourceAsStream("dpv/tester-invalid-processing.json");
        String usagePolicyString = IOUtils.toString(dpvIS, "UTF-8");
        dpvIS.close();
        TestResponse res = request("POST", testUrl, usagePolicyString);
        assertEquals(500, res.status);
    }

    public static class TestResponse {

        public final String body;
        public final int status;

        public TestResponse() {
            body = "";
            status = 500;
        }

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(System.lineSeparator());
            sb.append("status: ").append(status).append(System.lineSeparator());
            sb.append("body: ").append(body);

            return sb.toString();
        }
    }

}
