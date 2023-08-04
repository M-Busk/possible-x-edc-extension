package org.eclipse.edc.extension.possible.rdf;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.HashMap;

public class JenaExample {
     public static String writeRDF(String assetId) {
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
        String edcApiVersion = "edcApiVersion";
        String contractOfferId = "contractOfferId";
        String predicateAssetId = "assetId";
        String protocol = "protocol";
        String idsMultipart = "IdsMultipart";
        String hasPolicy = "hasPolicy";

         for (String key : prefixes.keySet())
                model.setNsPrefix(key, prefixes.get(key));

         Resource subjectData = model.createResource("https://possible.fokus.fraunhofer.de/set/data/test-dataset");
         Resource subjectDistribution = model.createResource("https://possible.fokus.fraunhofer.de/set/distribution/1");
         Resource objectPolicy = model.createResource()
                 .addProperty(RDF.type, model.createResource(prefixes.get(Constants.PREF_POSSIBLE_X)+"Policy"))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"policyType"), model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"Set"))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"uid"), "231802-bb34-11ec-8422-0242ac120002")
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"hasPermissions"), model.createResource()
                    .addProperty(RDF.type, model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"Permission"))
                    .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"target"), assetId)
                    .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"action"), model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"Use"))
                    .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+"edcType"), "dataspaceconnector:permission")
                 );

         subjectData.addProperty(RDF.type, DCAT.Dataset)
                 .addProperty(RDF.type, model.createResource(prefixes.get(Constants.PREF_GAX_TRUST_FRAMEWORK)+dataResource))
                 .addProperty(DCTerms.description, "This is an example for a Gaia-X Data Resource", "en")
                 .addProperty(DCTerms.title, "Example Gaia-X Data Resource", "en")
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_GAX_TRUST_FRAMEWORK)+producedBy), model.createResource("https://piveau.io/set/resource/legal-person/some-legal-person-2"))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_GAX_TRUST_FRAMEWORK)+exposedThrough), model.createResource("http://85.215.202.146:8282/"))
                 // not exaclty as expected
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_GAX_TRUST_FRAMEWORK)+containsPII), model.createTypedLiteral("false", XSDDatatype.XSDboolean))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+edcApiVersion), "1")
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+contractOfferId), "1:50f75a7a-5f81-4764-b2f9-ac258c3628e2")
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+predicateAssetId), assetId)
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+protocol), model.createResource(prefixes.get(Constants.PREF_POSSIBLE_X)+idsMultipart))
                 .addProperty(model.createProperty(prefixes.get(Constants.PREF_POSSIBLE_X)+hasPolicy), objectPolicy)
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

    public static void TestSendRequest() throws IOException {
         PossibleDataResource possibleDataResource = new PossibleDataResource("assetId", "h_test_friday_2");
         String payload = writeRDF(possibleDataResource.getAssetId());
        URL urlForGetRequest = new URL("https://possible.fokus.fraunhofer.de/api/hub/repo/catalogues/test-provider/datasets/origin?originalId="+possibleDataResource.getDatasetId());
        String readLine = null;

        String reqBody = payload;
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestProperty("Content-Type","text/turtle");
        connection.setRequestProperty("Authorization","Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib3BlcmF0b3IiXX19.lMkYKTViVNVPFH49ntdkruLe5EaWRUYt1YL-1Y7b0gc");
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream());
        out.write(reqBody);
  
        out.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in .readLine()) != null) {
                response.append(readLine);
            } in .close();
            // print result
            System.out.println("JSON String Result " + response.toString());
        } else {
            System.out.println("Not WORKED "+responseCode);
        }
    }

    public static void main(String[] args) {
        try {
            TestSendRequest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
