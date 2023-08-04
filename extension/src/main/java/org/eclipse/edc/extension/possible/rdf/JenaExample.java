package org.eclipse.edc.extension.possible.rdf;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

//import org.eclipse.edc.spi.monitor.Monitor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.HashMap;


public class JenaExample {
     // CONSTANTS
     final String preDcat = "dcat";
     final String preDct = "dct";
     final String preXsd = "xsd";
     final String preGaxCore = "gax-core";
     final String preGaxTrust = "gax-trust-framework";
     final String prePossibleX = "possible-x";

     public String testRDF() {
        Model model = ModelFactory.createDefaultModel();

        HashMap<String,String> prefixes = new HashMap<>();
        prefixes.put(preDcat ,"http://www.w3.org/ns/dcat#");
        prefixes.put(preDct,"http://purl.org/dc/terms/");
        prefixes.put(preXsd,"http://www.w3.org/2001/XMLSchema#");
        prefixes.put(preGaxCore,"http://w3id.org/gaia-x/core#");
        prefixes.put(preGaxTrust,"http://w3id.org/gaia-x/gax-trust-framework#");
        prefixes.put(prePossibleX,"http://w3id.org/gaia-x/possible-x#");

        // gax-trust-framework
        String dataResource  = "DataResource";
        String producedBy = "producedBy";
        String exposedThrough = "exposedThrough";
        String containsPII = "containsPII";
        // possible-x
        String edcApiVersion = "edcApiVersion";
        String contractOfferId = "contractOfferId";
        String assetId = "assetId";
        String protocol = "protocol";
        String idsMultipart = "IdsMultipart";
        String hasPolicy = "hasPolicy";

         for (String key : prefixes.keySet())
                model.setNsPrefix(key, prefixes.get(key));

         Resource subjectData = model.createResource("https://possible.fokus.fraunhofer.de/set/data/test-dataset");
         Resource subjectDistribution = model.createResource("https://possible.fokus.fraunhofer.de/set/distribution/1");
         Resource objectPolicy = model.createResource()
                 .addProperty(RDF.type, model.createResource(prefixes.get(prePossibleX)+"Policy"))
                 .addProperty(model.createProperty(prefixes.get(prePossibleX)+"policyType"), model.createProperty(prefixes.get(prePossibleX)+"Set"))
                 .addProperty(model.createProperty(prefixes.get(prePossibleX)+"uid"), "231802-bb34-11ec-8422-0242ac120002")
                 .addProperty(model.createProperty(prefixes.get(prePossibleX)+"hasPermissions"), model.createResource()
                    .addProperty(RDF.type, model.createProperty(prefixes.get(prePossibleX)+"Permission"))
                    .addProperty(model.createProperty(prefixes.get(prePossibleX)+"target"), assetId)
                    .addProperty(model.createProperty(prefixes.get(prePossibleX)+"action"), model.createProperty(prefixes.get(prePossibleX)+"Use"))
                    .addProperty(model.createProperty(prefixes.get(prePossibleX)+"edcType"), "dataspaceconnector:permission")
                 );

         subjectData.addProperty(RDF.type, DCAT.Dataset)
                 .addProperty(RDF.type, model.createResource(prefixes.get(preGaxTrust)+dataResource))
                 .addProperty(DCTerms.description, "This is an example for a Gaia-X Data Resource", "en")
                 .addProperty(DCTerms.title, "Example Gaia-X Data Resource", "en")
                 .addProperty(model.createProperty(prefixes.get(preGaxTrust)+producedBy), model.createResource("https://piveau.io/set/resource/legal-person/some-legal-person-2"))
                 .addProperty(model.createProperty(prefixes.get(preGaxTrust)+exposedThrough), model.createResource("http://85.215.202.146:8282/"))
                 // not exaclty as expected
                 .addProperty(model.createProperty(prefixes.get(preGaxTrust)+containsPII), model.createTypedLiteral("false", XSDDatatype.XSDboolean))
                 .addProperty(model.createProperty(prefixes.get(prePossibleX)+edcApiVersion), "1")
                 .addProperty(model.createProperty(prefixes.get(prePossibleX)+contractOfferId), "1:50f75a7a-5f81-4764-b2f9-ac258c3628e2")
                 .addProperty(model.createProperty(prefixes.get(prePossibleX)+assetId), assetId)
                 .addProperty(model.createProperty(prefixes.get(prePossibleX)+protocol), model.createResource(prefixes.get(prePossibleX)+idsMultipart))
                 .addProperty(model.createProperty(prefixes.get(prePossibleX)+hasPolicy), objectPolicy)
                 .addProperty(DCAT.distribution, model.createResource("https://possible.fokus.fraunhofer.de/set/distribution/1"));
        subjectDistribution.addProperty(RDF.type, DCAT.distribution)
                .addProperty(DCTerms.license, model.createResource("http://dcat-ap.de/def/licenses/gfdl"))
                .addProperty(DCAT.accessURL, model.createResource("http://85.215.193.145:9192/api/v1/data/assets/test-document_company2"));

        // Add all subjects to the model
        model.add(subjectData.getModel())
                .add(subjectDistribution.getModel());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(byteArrayOutputStream, model, Lang.TURTLE) ;

        String outputMessageText = new String(byteArrayOutputStream.toByteArray());

        try {
                TestSendRequest(outputMessageText); 
        } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
        }

        
        
        return outputMessageText;
    }


    public void TestSendRequest(String payload) throws IOException {
        URL urlForGetRequest = new URL("https://possible.fokus.fraunhofer.de/api/hub/repo/catalogues/test-provider/datasets/origin?originalId=h_test_friday_1");
        String readLine = null;
        /* String reqBody = """
                @prefix dcat:   <http://www.w3.org/ns/dcat#> .
                @prefix dct:    <http://purl.org/dc/terms/> .
                @prefix gax-core: <http://w3id.org/gaia-x/core#> .
                @prefix gax-trust-framework: <http://w3id.org/gaia-x/gax-trust-framework#> .
                @prefix possible-x: <https://possible-gaia-x.de/ns/#> .
                @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
                                
                <https://possible.fokus.fraunhofer.de/set/data/test-dataset>
                    a                                   dcat:Dataset ;
                    a                                   gax-trust-framework:DataResource ;
                    dct:description                     "This is an example for a Gaia-X Data Resource"@en ;
                    dct:title                           "Example Gaia-X Data Resource"@en ;
                    gax-trust-framework:producedBy      <https://piveau.io/set/resource/some-legal-person/some-legal-person-2> ;
                    gax-trust-framework:exposedThrough  <http://85.215.202.146:8282/> ;
                    gax-trust-framework:containsPII     "false"^^xsd:boolean ;
                    possible-x:edcApiVersion            1;
                    possible-x:contractOfferId          "1:50f75a7a-5f81-4764-b2f9-ac258c3628e2" ;
                    possible-x:assetId                  "assetId" ;
                    possible-x:protocol                 possible-x:IdsMultipart ;
                    possible-x:hasPolicy                [
                                                            a possible-x:Policy ;
                                                            possible-x:policyType possible-x:Set ;
                                                            possible-x:uid "231802-bb34-11ec-8422-0242ac120002" ;
                                                            possible-x:hasPermissions [
                                                                a possible-x:Permission ;
                                                                possible-x:target "assetId" ;
                                                                possible-x:action possible-x:Use ;
                                                                possible-x:edcType "dataspaceconnector:permission" ;
                                                            ] ;
                                                        ] ;
                    dcat:distribution                   <https://possible.fokus.fraunhofer.de/set/distribution/1> .
                                
                <https://possible.fokus.fraunhofer.de/set/distribution/1>
                    a                               dcat:Distribution ;
                    dct:license                     <http://dcat-ap.de/def/licenses/gfdl> ;
                    dcat:accessURL                  <http://85.215.193.145:9192/api/v1/data/assets/test-document_company2> .
                """; */

        String reqBody = payload;
        HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
        conection.setRequestProperty("Content-Type","text/turtle");
        conection.setRequestProperty("Authorization","Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib3BlcmF0b3IiXX19.lMkYKTViVNVPFH49ntdkruLe5EaWRUYt1YL-1Y7b0gc");
        conection.setRequestMethod("PUT");
        conection.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(
                conection.getOutputStream());
        out.write(reqBody);
  
        out.close();


        int responseCode = conection.getResponseCode();


        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in .readLine()) != null) {
                response.append(readLine);
            } in .close();
            // print result
            //monitor.info("Response"+ response.toString());
            System.out.println("JSON String Result " + response.toString());

            System.out.println("JSON String Result " + response.toString());
            //GetAndPost.POSTRequest(response.toString());
        } else {
            //monitor.info("Not WORKED"+responseCode);
                System.out.println("Not WORKED "+responseCode);
        }
    }

}
