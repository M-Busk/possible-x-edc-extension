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
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.event.Event;

@Extension(value = PossibleExtension.NAME)
public class PossibleExtension implements ServiceExtension {
    public static final String NAME = "POSSIBLE-EXTENSION";

    @Setting
    private static final String POSSIBLE_JWT_TOKEN = "possible.catalog.jwt.token";
    @Setting
    private static final String POSSIBLE_ENDPOINT = "possible.catalog.endpoint";
    @Setting
    private static final String POSSIBLE_EDC_VERSION = "possible.connector.edcVersion";

    @Inject
    private ContractDefinitionStore contractDefinitionStore;

    @Inject
    private AssetIndex assetIndex;

    @Inject
    private EventRouter eventRouter;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {

        var token = context.getSetting(POSSIBLE_JWT_TOKEN, POSSIBLE_JWT_TOKEN);
        var endpoint = context.getSetting(POSSIBLE_ENDPOINT, POSSIBLE_ENDPOINT);
        var edcVersion = context.getSetting(POSSIBLE_EDC_VERSION, POSSIBLE_EDC_VERSION);

        var registrationSender = new RegistrationSender(context.getMonitor(), endpoint, token, edcVersion);
        eventRouter.register(Event.class, new PossibleEventDetected(contractDefinitionStore, assetIndex, registrationSender, context.getMonitor())
        ); // asynchronous dispatch
    }
}
