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

import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegistrationSender {

    private final Monitor monitor;
    private final String endpoint;
    private final String token;
    private final String edcVersion;

    public RegistrationSender(Monitor monitor, String endpoint, String token, String edcVersion) {
        this.monitor = monitor;
        this.endpoint = endpoint;
        this.token = token;
        this.edcVersion = edcVersion;
    }

    public void doCommunication(String contractOfferId, String assetId, String policyId, String target, String description, String title) {

        RegistrationPayload payload = new RegistrationPayload(this.edcVersion, contractOfferId, assetId, policyId, target, description, title);
        String apiUrl = endpoint + "?originalId="+ assetId; 
        try {
                monitor.debug("doCommunication endpoint:" + apiUrl);
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT"); 

                // Add headers
                connection.setRequestProperty("Content-Type", "text/turtle");
                connection.setRequestProperty("Authorization", "Bearer " + token); 
                
                String requestBody = payload.getBody();
                monitor.debug("doCommunication body:" + requestBody);
                // Enable sending request body
                connection.setDoOutput(true);
                connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

                int responseCode = connection.getResponseCode();
                monitor.debug("Response Code: " + responseCode);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();

                if (!response.isEmpty())
                    monitor.debug("Response: " + response);

        } catch (Exception e) {
            throw new EdcException("Could not send clearing house registration", e);
        }
    }
}
