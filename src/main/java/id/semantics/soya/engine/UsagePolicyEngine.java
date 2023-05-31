package id.semantics.soya.engine;

import id.semantics.soya.Service;
import id.semantics.soya.helper.SwissKnife;
import id.semantics.soya.vocab.DPVO_SOYA;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class UsagePolicyEngine {

    private final Resource dataSubjectPolicy;
    private final Resource dataControllerPolicy;

    public UsagePolicyEngine() {
        dataSubjectPolicy = ResourceFactory.createResource("https://w3id.org/soya/dpv#" + Service.DATA_SUBJECT);
        dataControllerPolicy = ResourceFactory.createResource("https://w3id.org/soya/dpv#" + Service.DATA_CONTROLLER);
    }

    public String policyCheck(String policyString) {
        String result = "";
        if (!SwissKnife.policyCheck(policyString, dataSubjectPolicy, dataControllerPolicy)) {
            result = "DataControllerPolicy does not conform to DataSubjectPolicy!";
        }
        return result;
    }
}
