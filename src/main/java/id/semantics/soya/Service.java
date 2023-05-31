package id.semantics.soya;

import id.semantics.soya.engine.ConsentHandle;
import id.semantics.soya.engine.UsagePolicyEngine;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class Service {

    public static final String CONTENT_DATA = "content-data";
    public static final String CONTENT_CONSTRAINTS = "content-constraints";
    public static final String USAGE_POLICY = "usage-policy";
    public static final String BASE_CONFIG = "base-config";
    public static final String IMAGE_CONSTRAINTS = "image-constraints";

    public static final String DATA_SUBJECT = "data-subject";
    public static final String DATA_CONTROLLER = "data-controller";

    private static final Logger log = LoggerFactory.getLogger(Service.class);
    private static final UsagePolicyEngine usagePolicyEngine = new UsagePolicyEngine();

    public static void main(String[] args) {
        log.info("starting semantic services");
        Service service = new Service();
        service.establishRoutes();
        log.info("semantic services started!");
    }

    public void establishRoutes() {

        // set port
        port(2806);

        // hello world for testing
        get("/hello", (request, response) -> "hello");

        // DPV usage policy checker
        // @10.04.2022 - FJE
        post("/api/validate/usagepolicy", (request, response) -> {

            // default response
            response.status(500);
            response.type("application/json");

            String body = request.body();
            log.info(request.headers().toString());
            log.info(body);
            String validationResult;
            try {
                JSONObject rootObject = new JSONObject(body);
                String usagePolicy = ConsentHandle.INSTANCE.consentJsonToString(rootObject);
                log.info(usagePolicy);
                validationResult = usagePolicyEngine.policyCheck(usagePolicy);

                if (validationResult.isEmpty()) {
                    response.status(200);
                }
            } catch (Exception e) {
                validationResult = e.getMessage();
            }

            return validationResult;
        });
    }
}
