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
 *       Microsoft Corporation - Initial implementation
 *       1&1 IONOS Cloud GmbH
 *
 */

package org.eclipse.edc.extension.possible;

import org.eclipse.edc.connector.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.extension.possible.register.RegistrationSender;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.event.Event;
import org.eclipse.edc.spi.event.EventSubscriber;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.connector.contract.spi.event.contractdefinition.ContractDefinitionCreated;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.Criterion;

public class PossibleEventDetected implements  EventSubscriber{
    private final ContractDefinitionStore contractDefinitionStore;
    private final AssetIndex assetIndex;
    private final RegistrationSender registrationSender;
    private final Monitor monitor;

    PossibleEventDetected(ContractDefinitionStore contractDefinitionStore,
                          AssetIndex assetIndex,
                          RegistrationSender registrationSender,
                          Monitor monitor)
    {
        this.contractDefinitionStore = contractDefinitionStore;
        this.assetIndex = assetIndex;
        this.registrationSender = registrationSender;
        this.monitor = monitor;
    }

    @Override
    public <E extends Event> void  on(EventEnvelope<E> event) {
        if (event.getPayload() instanceof ContractDefinitionCreated cdc) {
            try {
                monitor.debug("Contract Definition ID:  " + cdc.getContractDefinitionId());

                var contractDefinitionId = cdc.getContractDefinitionId();
                var contractDefinition = contractDefinitionStore.findById(contractDefinitionId);
                if (contractDefinition == null) {
                    monitor.severe("Invalid Invalid Contract Definition ID: " + contractDefinitionId);
                    return;
                }

                monitor.debug("Contract Definition Found: " + contractDefinition);
                var policyId = contractDefinition.getContractPolicyId();

                for (Criterion assetCriterion: contractDefinition.getAssetsSelector()) {

                    var assetId = assetCriterion.getOperandRight().toString();
                    var asset = assetIndex.findById(assetId);
                    if (asset == null) {
                        monitor.severe("Invalid Asset ID: " + assetId);
                        continue;
                    }

                    monitor.debug("Asset Found: " + asset);
                    var description = asset.getDescription();
                    var title = asset.getName();

                    monitor.debug("Sending registration for Contract Definition ID " + contractDefinitionId + " and Asset ID " + assetId);
                    registrationSender.doCommunication(contractDefinitionId, assetId, policyId, assetId, description, title);
                }
            } catch (Exception e) {
                throw new EdcException("Could not process contract definition event", e);
            }
        }
    }
}
