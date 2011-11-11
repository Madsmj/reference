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
package org.bitrepository.access.getchecksums.selector;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.exceptions.NegativeResponseException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for selecting pillars for the GetChecksums operation.
 */
public class PillarSelectorForGetChecksums {
    /** Used for tracking who has answered. */
    private final PillarsResponseStatus responseStatus;
    /** The pillars which have been selected for a checksums request. */
    private final List<SelectedPillarInfo> selectedPillars = new LinkedList<SelectedPillarInfo>(); 

    /**
     * Constructor.
     * @param pillars The IDs of the pillars to be selected.
     */
    public PillarSelectorForGetChecksums(Collection<String> pillarsWhichShouldRespond) {
        ArgumentValidator.checkNotNullOrEmpty(pillarsWhichShouldRespond, "pillarsWhichShouldRespond");
        responseStatus = new PillarsResponseStatus(pillarsWhichShouldRespond);
    }

    /**
     * Method for processing a IdentifyPillarsForGetChecksumsResponse. Checks whether the response is from the wanted
     * expected pillar.
     * @param response The response identifying a pillar for the GetChecksums operation.
     */
    public void processResponse(IdentifyPillarsForGetChecksumsResponse response) 
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
     */
    private void validateResponse(ResponseInfo irInfo) throws UnexpectedResponseException {
        String errorMessage = null;

        if(irInfo == null) {
            errorMessage = "Response code was null";
        }

        ResponseCode responseCode = irInfo.getResponseCode();
        if(responseCode == null) {
            errorMessage = "Response code was null";
        }

        ResponseCode.IDENTIFICATION_POSITIVE.value().equals(
                responseCode.value());
        if (errorMessage != null) throw new UnexpectedResponseException(
                "Invalid IdentifyResponse from response.getPillarID(), " + errorMessage);
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
    public String[] getOutstandingPillars() {
        return responseStatus.getOutstandPillars();
    }

    /**
     * @return The selected pillars.
     */
    public List<SelectedPillarInfo> getSelectedPillars() {
        return selectedPillars;
    }
}
