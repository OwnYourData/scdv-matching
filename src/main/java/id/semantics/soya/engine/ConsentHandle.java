package id.semantics.soya.engine;

import id.semantics.soya.vocab.DPVO;
import id.semantics.soya.vocab.DPVO_SOYA;
import id.semantics.soya.helper.UnregisteredTermException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import static id.semantics.soya.Service.DATA_CONTROLLER;
import static id.semantics.soya.Service.DATA_SUBJECT;

public enum ConsentHandle {

    INSTANCE;

    public static final String NS_INSTANCE = "http://example.org/instance-ns#";
    public static final String HANDLING = "http://example.org/id/handling";
    public static final String CONSENT = "http://example.org/id/consent";

    private static final Logger log = LoggerFactory.getLogger(ConsentHandle.class);
    public Model dpvModel = ModelFactory.createDefaultModel();

    ConsentHandle() {
        if (dpvModel.isEmpty()) {
            InputStream dpvo_lite = ConsentHandle.class.getClassLoader().getResourceAsStream("dpv/dpv.ttl");
            InputStream scdv = ConsentHandle.class.getClassLoader().getResourceAsStream("dpv/scdv.ttl");
            // InputStream dpvo_soya = ConsentHandle.class.getClassLoader().getResourceAsStream("dpv/dpvo-05-soya.ttl");
            // InputStream dpvo_pd = ConsentHandle.class.getClassLoader().getResourceAsStream("dpv/dpvo-pd.ttl");

            RDFDataMgr.read(dpvModel, dpvo_lite, Lang.TURTLE);
            RDFDataMgr.read(dpvModel, scdv, Lang.TURTLE);
            // RDFDataMgr.read(dpvModel, dpvo_soya, Lang.TURTLE);
            // RDFDataMgr.read(dpvModel, dpvo_pd, Lang.TURTLE);

//            try {
//                RDFDataMgr.write(new FileOutputStream("dpv-full.ttl"), dpvModel, Lang.TURTLE);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
        }
    }

    public String consentJsonToString(JSONObject dpvInput) throws UnregisteredTermException {
        log.info("here");
        JSONObject userConsent = dpvInput.getJSONObject(DATA_SUBJECT).getJSONArray("@graph").getJSONObject(0);
        log.info("here1");
        JSONObject controllerHandling = dpvInput.getJSONObject(DATA_CONTROLLER).getJSONArray("@graph").getJSONObject(0);
        log.info("here2");

        Model model = ModelFactory.createDefaultModel();
        log.info("here3");

        model.add(getPolicy("https://w3id.org/soya/dpv#"+DATA_SUBJECT, userConsent));
        log.info("here4");
        model.add(getPolicy("https://w3id.org/soya/dpv#"+DATA_CONTROLLER, controllerHandling));
        log.info("here5");

        log.info(model.toString());

        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, model, Lang.TURTLE);
        // System.out.println(sw.toString());
        return sw.toString();
    }

    public Model getPolicy(String uri, JSONObject consent) throws UnregisteredTermException {
        Resource handlingResource = ResourceFactory.createResource(uri);
        return getModel(consent, handlingResource);
    }

    private Model getModel(JSONObject consent, Resource consentResource) throws UnregisteredTermException {

        String processing = getJsonString(consent, "dpvo:hasProcessing");
        String purpose = getJsonString(consent, "dpvo:hasPurpose");
        String recipient = getJsonString(consent, "dpvo:hasRecipient");
        String location = getJsonString(consent, "dpvo:hasLocation");
        String expiry = consent.getString("dpvo:hasExpiryTime");

        return getConsent(consentResource, null, processing, purpose, recipient, location, expiry);
    }

    private String getJsonString(JSONObject consent, String property) {
        Object object = consent.get(property);
        if (object instanceof JSONArray) {
            JSONArray array = consent.getJSONArray(property);
            StringJoiner joiner = new StringJoiner(",");
            array.forEach(val -> joiner.add(val.toString()));
            return joiner.toString();
        } else {
            return consent.getString(property);
        }
    }

    private Model getConsent(Resource consent, String data, String processing, String purpose, String recipient, String location, String expiry)
            throws UnregisteredTermException {
        Model consentModel = ModelFactory.createDefaultModel();
        consentModel.setNsPrefix("dpvo", DPVO.NS);
        consentModel.setNsPrefix("owl", OWL.NS);
        consentModel.setNsPrefix("xsd", XSD.NS);

        Resource equivClass = consentModel.createResource();

        // Resource bnodeData =
        //         setRestriction(consentModel, data, DPVO.HAS_PERSONAL_DATA, DPVO.PERSONAL_DATA);
        Resource bnodeProcessing = setRestriction(consentModel, processing, DPVO.HAS_PROCESSING, DPVO.PROCESSING);
        Resource bnodePurpose = setRestriction(consentModel, purpose, DPVO.HAS_PURPOSE, DPVO.PURPOSE);
        Resource bnodeRecipient = setRestriction(consentModel, recipient, DPVO.HAS_RECIPIENT, DPVO.RECIPIENT);
        Resource bnodeLocation = setRestriction(consentModel, location, DPVO.HAS_LOCATION, DPVO.LOCATION);
        Resource bnodeExpiryTime = setExpiryDate(consentModel, expiry);

        // RDFList policyList = consentModel.createList().with(bnodeData);
        RDFList policyList = consentModel.createList().with(bnodeProcessing);
        // policyList.add(bnodeProcessing);
        policyList.add(bnodePurpose);
        policyList.add(bnodeRecipient);
        policyList.add(bnodeLocation);
        policyList.add(bnodeExpiryTime);
        equivClass.addProperty(OWL.intersectionOf, policyList);

        consentModel.add(consent, RDF.type, OWL.Class);
        consentModel.add(consent, OWL.equivalentClass, equivClass);

        return consentModel;
    }

    private Resource setRestriction(Model consentModel, String input, Property property, Resource defaultValue)
            throws UnregisteredTermException {

        Resource restriction = consentModel.createResource();
        Resource value = getRestrictionValue(consentModel, input, defaultValue);

        restriction.addProperty(RDF.type, OWL.Restriction);
        restriction.addProperty(OWL.onProperty, property);
        restriction.addProperty(OWL.someValuesFrom, value);

        return restriction;
    }

    private Resource setExpiryDate(Model consentModel, String expiryDate) {

        Resource restriction = consentModel.createResource();

        Resource maxInclusive = consentModel.createResource();
        Literal dateTime = consentModel.createTypedLiteral(expiryDate, XSD.dateTime.getURI());
        maxInclusive.addProperty(consentModel.createProperty(XSD.NS, "maxInclusive"), dateTime);

        Resource value = consentModel.createResource();
        value.addProperty(RDF.type, RDFS.Datatype);
        value.addProperty(OWL2.onDatatype, XSD.dateTime);
        value.addProperty(OWL2.withRestrictions, consentModel.createList(maxInclusive));

        restriction.addProperty(RDF.type, OWL.Restriction);
        restriction.addProperty(OWL.onProperty, DPVO.HAS_EXPIRY_TIME);
        restriction.addProperty(OWL.someValuesFrom, value);

        return restriction;
    }

    private Resource getRestrictionValue(Model consentModel, String input, Resource defaultValue) throws UnregisteredTermException {
        Resource restrictionValue;

        if (input.contains(",")) {
            List<String> values = Arrays.asList(input.trim().split("\\s*,\\s*"));
            restrictionValue = consentModel.createResource();
            RDFList unionOf = null;
            for (int i = 0; i < values.size(); i++) {
                Resource value = checkExistingResource(values.get(i), defaultValue);
                if (unionOf == null) {
                    unionOf = consentModel.createList().with(value);
                } else {
                    unionOf.add(value);
                }
            }
            restrictionValue.addProperty(OWL.unionOf, unionOf);
        } else {
            restrictionValue = checkExistingResource(input, defaultValue);
        }

        return restrictionValue;
    }

    private Resource checkExistingResource(String input, Resource defaultValue) throws UnregisteredTermException {
        Resource restrictionValue;

        if (input.isEmpty()) {
            restrictionValue = defaultValue; // empty means default!
        } else {
            Resource resDPV;
            if (input.startsWith("dpvo:")) {
                String inputValue = input.split(":")[1];
                resDPV = dpvModel.createResource(DPVO.NS + inputValue);
            } else if (input.startsWith("scdv:")){
                String inputValue = input.split(":")[1];
                resDPV = dpvModel.createResource("https://w3id.org/scdv/v1#" + inputValue);
            } else {
                resDPV = dpvModel.createResource(DPVO.NS + input);
            }
            log.info(resDPV.toString());
            if (dpvModel.containsResource(resDPV)) {
                restrictionValue = resDPV;
            } else {
                throw new UnregisteredTermException("'" + input + "' is not registered as terms of consent");
            }
        }

        return restrictionValue;
    }

}
