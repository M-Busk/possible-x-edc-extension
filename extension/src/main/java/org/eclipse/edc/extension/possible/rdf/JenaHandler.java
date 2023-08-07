package org.eclipse.edc.extension.possible.rdf;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import java.util.HashMap;

public class JenaHandler {

    public JenaHandler() {
    }

    public String writeRDF(String edcApiVersion, String contractOfferId, String assetId, String policyId, String target, String description, String title) {
        Model model = ModelFactory.createDefaultModel();

        HashMap<String,String> prefixes = new HashMap<>();
        prefixes.put(Constants.PREF_DCAT, Constants.PREF_DCAT_URI);
        prefixes.put(Constants.PREF_DCT, Constants.PREF_DCT_URI);
        prefixes.put(Constants.PREF_XSD, Constants.PREF_XSD_URI);
        prefixes.put(Constants.PREF_GAX_CORE, Constants.PREF_GAX_CORE_URI);
        prefixes.put(Constants.PREF_GAX_TRUST_FRAMEWORK, Constants.PREF_GAX_TRUST_FRAMEWORK_URI);
        prefixes.put(Constants.PREF_POSSIBLE_X, Constants.PREF_POSSIBLE_X_URI);

        // gax-trust-framework
        String dataResource  = "DataResource";
        String producedBy = "producedBy";
        String exposedThrough = "exposedThrough";
        String containsPII = "containsPII";
        // possible-x
        String predicateEdcApiVersion = "edcApiVersion";
        String predicateContractOfferId = "contractOfferId";
        String predicateAssetId = "assetId";
        String predicateProtocol = "protocol";
        String predicateIdsMultipart = "IdsMultipart";
        String predicateHasPolicy = "hasPolicy";

         for (String key : prefixes.keySet())
                model.setNsPrefix(key, prefixes.get(key));

         Resource subjectData = model.createResource("https://possible.fokus.fraunhofer.de/set/data/test-dataset");
         Resource subjectDistribution = model.createResource("https://possible.fokus.fraunhofer.de/set/distribution/1");
         Resource objectPolicy = model.createResource()
                 .addProperty(RDF.type, model.createResource(prefixes.get(Constants.PREF_POSSIBLE_X)+"Policy"))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"policyType"), model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"Set"))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"uid"), policyId)
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"hasPermissions"), model.createResource()
                    .addProperty(RDF.type, model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"Permission"))
                    .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"target"), target)
                    .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"action"), model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"Use"))
                    .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"edcType"), "dataspaceconnector:permission")
                 );

         subjectData.addProperty(RDF.type, DCAT.Dataset)
                 .addProperty(RDF.type, model.createResource(prefixes.get(Constants.PREF_GAX_TRUST_FRAMEWORK)+dataResource))
                 .addProperty(DCTerms.description, description, "en")
                 .addProperty(DCTerms.title, title, "en")
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_GAX_TRUST_FRAMEWORK)+producedBy), model.createResource("https://piveau.io/set/resource/legal-person/some-legal-person-2"))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_GAX_TRUST_FRAMEWORK)+exposedThrough), model.createResource("http://85.215.202.146:8282/"))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_GAX_TRUST_FRAMEWORK)+containsPII), model.createTypedLiteral("false", XSDDatatype.XSDboolean))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+predicateEdcApiVersion), edcApiVersion)
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+predicateContractOfferId), contractOfferId)
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+predicateAssetId), assetId)
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+predicateProtocol), model.createResource(prefixes.get(Constants.PREF_POSSIBLE_X)+predicateIdsMultipart))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+predicateHasPolicy), objectPolicy)
                 .addProperty(DCAT.distribution, model.createResource("https://possible.fokus.fraunhofer.de/set/distribution/1"));
        subjectDistribution.addProperty(RDF.type, DCAT.distribution)
                .addProperty(DCTerms.license, model.createResource("http://dcat-ap.de/def/licenses/gfdl"))
                .addProperty(DCAT.accessURL, model.createResource("http://85.215.193.145:9192/api/v1/data/assets/test-document_company2"));

        // Add all subjects to the model
        model.add(subjectData.getModel())
                .add(subjectDistribution.getModel());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(byteArrayOutputStream, model, Lang.TURTLE) ;

        return new String(byteArrayOutputStream.toByteArray());
    }
}
