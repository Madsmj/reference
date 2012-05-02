/*
 * #%L
 * Bitrepository Modifying Client
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.modify.deletefile.selector;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;

public class AllPillarsSelectorForDeleteFile extends DeleteFileSelector {
    
    public AllPillarsSelectorForDeleteFile(Collection<String> pillarsWhichShouldRespond) {
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
    }
    
    protected boolean checkPillarResponseForSelection(IdentifyPillarsForDeleteFileResponse response) {
        return response.getResponseInfo().getResponseCode().equals(ResponseCode.IDENTIFICATION_POSITIVE);
    }


    

}
