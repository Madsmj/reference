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

import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.UUID;

public class GetAuditTrailsMessageFactory extends ClientTestMessageFactory {

    final Settings settings;
    
    public GetAuditTrailsMessageFactory(Settings pSettings) {
        super(pSettings.getCollectionID());
        this.settings = pSettings;
    }
    
    public IdentifyContributorsForGetAuditTrailsRequest createIdentifyContributorsForGetAuditTrailsRequest( 
            String auditTrail, String from, String replyTo) {
        IdentifyContributorsForGetAuditTrailsRequest res = new IdentifyContributorsForGetAuditTrailsRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFrom(from);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public IdentifyContributorsForGetAuditTrailsResponse createIdentifyContributorsForGetAuditTrailsResponse(
            String correlationId, String contributorId, String replyTo, ResponseInfo responseInfo, String toTopic) {
        IdentifyContributorsForGetAuditTrailsResponse res = new IdentifyContributorsForGetAuditTrailsResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFrom(contributorId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }
    
    public GetAuditTrailsRequest createGetAuditTrailsRequest(String auditTrail, String contributorId, 
            String correlationId, String fileId, String from, BigInteger maxSequence, XMLGregorianCalendar maxTime, 
            BigInteger minSequence, XMLGregorianCalendar minTime, String replyTo, String url, String toTopic) {
        GetAuditTrailsRequest res = new GetAuditTrailsRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setContributor(contributorId);
        res.setCorrelationID(correlationId);
        res.setFileID(fileId);
        res.setFrom(from);
        res.setMaxSequenceNumber(maxSequence);
        res.setMaxTimestamp(maxTime);
        res.setMinSequenceNumber(minSequence);
        res.setMinTimestamp(minTime);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setResultAddress(url);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public GetAuditTrailsProgressResponse createGetAuditTrailsProgressResponse(String contributorId, String correlationId, 
            String replyTo, ResponseInfo responseInfo, String url, String toTopic) {
        GetAuditTrailsProgressResponse res = new GetAuditTrailsProgressResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setContributor(contributorId);
        res.setCorrelationID(correlationId);
        res.setFrom(contributorId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setResultAddress(url);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public GetAuditTrailsFinalResponse createGetAuditTrailsFinalResponse(String contributorId, String correlationId, 
            String replyTo, ResponseInfo responseInfo, ResultingAuditTrails auditTrails, String toTopic) {
        GetAuditTrailsFinalResponse res = new GetAuditTrailsFinalResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setContributor(contributorId);
        res.setCorrelationID(correlationId);
        res.setFrom(contributorId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setResultingAuditTrails(auditTrails);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }
    
    /**
     * Method for generating new correlation IDs.
     * @return A unique correlation id.
     */
    public String getNewCorrelationID() {
        return UUID.randomUUID().toString();
    }
}