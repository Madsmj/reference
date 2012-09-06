/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.service.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IdentifyContributorExceptionTest extends ExtendedTestCase {
    
    @Test(groups = { "regressiontest" })
    public void testIdentifyContributor() throws Exception {
        addDescription("Test the instantiation of the exception");
        addStep("Setup", "");
        String errMsg = "TEST-ERROR";
        ResponseCode errCode = ResponseCode.FAILURE;
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseText(errMsg);
        ri.setResponseCode(errCode);
        String causeMsg = "CAUSE-EXCEPTION";
        
        addStep("Try to throw such an exception", "Should be able to be caught and validated");
        try {
            throw new IdentifyContributorException(ri);
        } catch(Exception e) {
            Assert.assertTrue(e instanceof IdentifyContributorException);
            Assert.assertEquals(e.getMessage(), errMsg);
            Assert.assertEquals(((IdentifyContributorException) e).getResponseInfo().getResponseCode(), errCode);
            Assert.assertEquals(((IdentifyContributorException) e).getResponseInfo().getResponseText(), errMsg);
            Assert.assertNull(e.getCause());
        }
        
        addStep("Throw the exception with an embedded exception", "The embedded exception should be the same.");
        try {
            throw new IdentifyContributorException(ri, new IllegalArgumentException(causeMsg));
        } catch(Exception e) {
            Assert.assertTrue(e instanceof IdentifyContributorException);
            Assert.assertEquals(e.getMessage(), errMsg);
            Assert.assertEquals(((IdentifyContributorException) e).getResponseInfo().getResponseCode(), errCode);
            Assert.assertEquals(((IdentifyContributorException) e).getResponseInfo().getResponseText(), errMsg);
            Assert.assertNotNull(e.getCause());
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
            Assert.assertEquals(e.getCause().getMessage(), causeMsg);
        }
    }    
}