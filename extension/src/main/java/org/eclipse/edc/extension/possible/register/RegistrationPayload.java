/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       1&1 IONOS Cloud GmbH
 *
 */

package org.eclipse.edc.extension.possible.register;

import org.eclipse.edc.extension.possible.rdf.JenaHandler;

public class RegistrationPayload {
    private final String edcApiVersion;
    private final String contractOfferId;
    private final String assetId;
    private final String policyId;
    private final String target;
    private final String description;
    private final String title;

    public RegistrationPayload(String edcApiVersion, String contractOfferId, String assetId, String policyId, String target, String description, String title) {
        this.edcApiVersion = edcApiVersion;
        this.contractOfferId = contractOfferId;
        this.assetId = assetId;
        this.policyId = policyId;
        this.target = target;
        this.description = description;
        this.title = title;
    }

    public String getBody() {
        JenaHandler jenaHandler = new JenaHandler();
        String payload = jenaHandler.writeRDF(edcApiVersion, contractOfferId, assetId, policyId, target, description, title);

        return payload;
/*
        return "@prefix dcat:   <http://www.w3.org/ns/dcat#> .\r\n" + //
                "@prefix dct:    <http://purl.org/dc/terms/> .\r\n" + //
                "@prefix gax-core: <http://w3id.org/gaia-x/core#> .\r\n" + //
                "@prefix gax-trust-framework: <http://w3id.org/gaia-x/gax-trust-framework#> .\r\n" + //
                "@prefix possible-x: <https://possible-gaia-x.de/ns/#> .\r\n" + //
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n" + //
                "\r\n" + //
                "<https://possible.fokus.fraunhofer.de/set/data/test-dataset>\r\n" + //
                "    a                                   dcat:Dataset ;\r\n" + //
                "    a                                   gax-trust-framework:DataResource ;\r\n" + //
                "    dct:description                     \"" + description + "\"@en ;\r\n" + //
                "    dct:title                           \""+ title +"\"@en ;\r\n" + //
                "    gax-trust-framework:producedBy      <https://piveau.io/set/resource/some-legal-person/some-legal-person-2> ;\r\n" + //
                "    gax-trust-framework:exposedThrough  <http://85.215.202.146:8282/> ;\r\n" + //
                "    gax-trust-framework:containsPII     \"false\"^^xsd:boolean ;\r\n" + //
                "    possible-x:edcApiVersion           \""+ edcApiVersion +"\";\r\n" + //
                "    possible-x:contractOfferId          \""+ contractOfferId +"\" ;\r\n" + //
                "    possible-x:assetId                  \""+ assetId +"\" ;\r\n" + //
                "    possible-x:protocol                 possible-x:dataspace-protocol-http ;  \r\n" + //
                "    possible-x:hasPolicy                [\r\n" + //
                "                                            a possible-x:Policy ;\r\n" + //
                "                                            possible-x:policyType possible-x:Set ;\r\n" + //
                "                                            possible-x:uid \""+ policyId +"\" ;\r\n" + //
                "                                            possible-x:hasPermissions [\r\n" + //
                "                                                a possible-x:Permission ;\r\n" + //
                "                                                possible-x:target \""+ target +"\" ;\r\n" + //
                "                                                possible-x:action possible-x:Use ;\r\n" + //
                "                                                possible-x:edcType \"dataspaceconnector:permission\" ;\r\n" + //
                "                                            ] ;\r\n" + //
                "                                        ] ;\r\n" + //
                "    dcat:distribution                   <https://possible.fokus.fraunhofer.de/set/distribution/1> .\r\n" + //
                "\r\n" + //
                "<https://possible.fokus.fraunhofer.de/set/distribution/1>\r\n" + //
                "    a                               dcat:Distribution ;\r\n" + //
                "    dct:license                     <http://dcat-ap.de/def/licenses/gfdl> ;\r\n" + //
                "    dcat:accessURL                  <http://85.215.193.145:9192/api/v1/data/assets/test-document_company2> .\r\n";
        */
    }
}