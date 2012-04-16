/*
 * #%L
 * Bitrepository Access
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.access.getfileids.selector;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.exceptions.NegativeResponseException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.ContributorResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

/**
 * Class for selecting pillars for the GetFileIDs operation.
 */
public class PillarSelectorForGetFileIDs {
    /** The status for the responses from the pillars. */
    private final ContributorResponseStatus responseStatus;
    /** The list of information about the pillars selected for the GetFileIDs operation.*/
    private final List<SelectedPillarInfo> selectedPillars = new LinkedList<SelectedPillarInfo>(); 

    /**
     * Constructor.
     * @param pillars The IDs of the pillars to be selected.
     */
    public PillarSelectorForGetFileIDs(Collection<String> pillarsWhichShouldRespond) {
        ArgumentValidator.checkNotNullOrEmpty(pillarsWhichShouldRespond, "pillarsWhichShouldRespond");
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
    }

    /**
     * Method for processing a IdentifyPillarsForGetFileIDsResponse. Checks whether the response is from the wanted
     * expected pillar.
     * @param response The response identifying a pillar for the GetFileIDs operation.
     */
    public void processResponse(IdentifyPillarsForGetFileIDsResponse response) 
            throws UnexpectedResponseException, NegativeResponseException {
        responseStatus.responseReceived(response.getPillarID());
        validateResponse(response.getResponseInfo());
        if (!ResponseCode.IDENTIFICATION_POSITIVE.value().equals(
                response.getResponseInfo().getResponseCode().value())) {
            throw new NegativeResponseException(response.getPillarID() + " sent negative response " + 
                    response.getResponseInfo().getResponseText(), 
                    response.getResponseInfo().getResponseCode());
        }
        selectedPillars.add(new SelectedPillarInfo(response.getPillarID(), response.getReplyTo()));
    }

    /**
     * Method for validating the response.
     * @param irInfo The IdentifyResponseInfo to validate.
     * @throws UnexpectedResponseException If it was not an expected response, e.g. if it was not positive.
     */
    private void validateResponse(ResponseInfo irInfo) throws UnexpectedResponseException {
        if(irInfo == null) {
            throw new UnexpectedResponseException("Response info was null");
        }

        ResponseCode responseCode = irInfo.getResponseCode();
        if(responseCode == null) {
            throw new UnexpectedResponseException("Response code was null, with text: " + irInfo.getResponseText());
        }
        
        if(responseCode != ResponseCode.IDENTIFICATION_POSITIVE) {
            throw new UnexpectedResponseException("Invalid IdentifyResponse. Expected '"
                    + ResponseCode.IDENTIFICATION_POSITIVE + "' but received: '" + responseCode.value() 
                    + "', with text '" + irInfo.getResponseText() + "'");
        }
    }

    /**
     * Tells whether the selection is finished.
     * @return Whether any pillars are outstanding.
     */
    public boolean isFinished() {
        return responseStatus.haveAllPillarResponded();
    }

    /**
     * Method for identifying the pillars, which needs to be identified for this operation to be finished.
     * @return An array of the IDs of the pillars which have not yet responded.
     */
    public List<String> getOutstandingPillars() {
        return Arrays.asList(responseStatus.getOutstandPillars());
    }

    /**
     * @return The selected pillars.
     */
    public List<SelectedPillarInfo> getSelectedPillars() {
        return selectedPillars;
    }
}
