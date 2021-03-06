/*
 * #%L
 * Bitrepository Integration
 * 
 * $Id: PillarTestMessageFactory.java 659 2011-12-22 15:56:07Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PillarTestMessageFactory.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.pillar.messagefactories;

import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.common.settings.Settings;

public class GetStatusMessageFactory extends PillarTestMessageFactory {

    public GetStatusMessageFactory(
            String collectionID, Settings clientSettings, String pillarID, String pillarDestination) {
        super(collectionID, clientSettings, pillarID, pillarDestination);
    }
    public  IdentifyContributorsForGetStatusRequest createIdentifyContributorsForGetStatusRequest() {
        IdentifyContributorsForGetStatusRequest res = new IdentifyContributorsForGetStatusRequest();
        initializeIdentifyRequest(res);
        return res;
    }

    public GetStatusRequest createGetStatusRequest() {
        GetStatusRequest request = new GetStatusRequest();
        initializeOperationRequest(request);
        request.setContributor(pillarID);
        return request;
    }
}
