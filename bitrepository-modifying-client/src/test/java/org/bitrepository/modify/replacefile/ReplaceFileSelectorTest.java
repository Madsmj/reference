package org.bitrepository.modify.replacefile;

import java.util.Arrays;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.common.utils.ResponseInfoUtils;
import org.bitrepository.modify.replacefile.pillarselector.AllPillarsSelectorForReplaceFile;
import org.bitrepository.modify.replacefile.pillarselector.SpecificPillarSelectorForReplaceFile;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReplaceFileSelectorTest extends ExtendedTestCase {
    
    String PILLAR_ID_1 = "pillar1";
    
    @Test(groups={"regressiontest"})
    public void allPillarSelectorTester() throws Exception {
        addDescription("Tests the " + AllPillarsSelectorForReplaceFile.class.getName());
        addStep("Setup", "");
        AllPillarsSelectorForReplaceFile selector = new AllPillarsSelectorForReplaceFile(Arrays.asList(PILLAR_ID_1));
        
        addStep("Test the basic funtionallity", "Should return lists.");
        Assert.assertEquals(selector.getOutstandingComponents(), Arrays.asList(PILLAR_ID_1));
        Assert.assertEquals(selector.getContributersAsString(), Arrays.asList(new String[0]).toString());
        Assert.assertEquals(selector.getSelectedComponents(), Arrays.asList(new String[0]));
        Assert.assertFalse(selector.hasSelectedComponent());
        Assert.assertFalse(selector.isFinished());
        
        addStep("Test against another type of message response.", "Should throw an exception.");
        MessageResponse response = new MessageResponse();
        response.setFrom(PILLAR_ID_1);
        response.setResponseInfo(ResponseInfoUtils.getPositiveIdentification());
        
        try {
            selector.processResponse(response);
            Assert.fail("Should throw an " + UnexpectedResponseException.class.getName());
        } catch (UnexpectedResponseException e) {
            // expected.
        }
        
        addStep("Test against a valid but negative response.", 
                "Should be accepted, but the selector should not be able to finish.");
        IdentifyPillarsForReplaceFileResponse response2 = new IdentifyPillarsForReplaceFileResponse();
        response2.setFrom(PILLAR_ID_1);
        response2.setResponseInfo(createNegativeIdentificationResponseInfo());
        selector.processResponse(response2);
        
        Assert.assertEquals(selector.getOutstandingComponents(), Arrays.asList(new String[0]));
        try {
            selector.isFinished();
            Assert.fail("Should throw an " + UnableToFinishException.class.getName());
        } catch (UnableToFinishException e) {
            // expected
        }
        
        addStep("Process the same response twice.", "Should throw an exception");
        try {
            selector.processResponse(response2);
            Assert.fail("Should throw an " + UnexpectedResponseException.class.getName());
        } catch (UnexpectedResponseException e) {
            // expected
        }
        
        addStep("Good case. New selector and a positive response.", "Should finish");
        selector = new AllPillarsSelectorForReplaceFile(Arrays.asList(PILLAR_ID_1));
        IdentifyPillarsForReplaceFileResponse goodResponse = new IdentifyPillarsForReplaceFileResponse();
        goodResponse.setFrom(PILLAR_ID_1);
        goodResponse.setResponseInfo(ResponseInfoUtils.getPositiveIdentification());
        selector.processResponse(goodResponse);
        
        Assert.assertTrue(selector.isFinished());
    }

    @Test(groups={"regressiontest"})
    public void specificPillarSelectorTester() throws Exception {
        addDescription("Tests the " + SpecificPillarSelectorForReplaceFile.class.getName());
        addStep("Setup", "");
        SpecificPillarSelectorForReplaceFile selector = new SpecificPillarSelectorForReplaceFile(
                Arrays.asList(PILLAR_ID_1), PILLAR_ID_1);
        
        addStep("Test the basic funtionallity", "Should return lists.");
        Assert.assertEquals(selector.getOutstandingComponents(), Arrays.asList(PILLAR_ID_1));
        Assert.assertEquals(selector.getContributersAsString(), Arrays.asList(new String[0]).toString());
        Assert.assertEquals(selector.getSelectedComponents(), Arrays.asList(new String[0]));
        Assert.assertFalse(selector.hasSelectedComponent());
        Assert.assertFalse(selector.isFinished());
        
        addStep("Test against another type of message response.", "Should throw an exception.");
        MessageResponse response = new MessageResponse();
        response.setFrom(PILLAR_ID_1);
        response.setResponseInfo(ResponseInfoUtils.getPositiveIdentification());
        
        try {
            selector.processResponse(response);
            Assert.fail("Should throw an " + UnexpectedResponseException.class.getName());
        } catch (UnexpectedResponseException e) {
            // expected.
        }
        
        addStep("Test against a valid but negative response.", 
                "Should be accepted, but the selector should not be able to finish.");
        IdentifyPillarsForReplaceFileResponse response2 = new IdentifyPillarsForReplaceFileResponse();
        response2.setFrom(PILLAR_ID_1);
        response2.setResponseInfo(createNegativeIdentificationResponseInfo());
        selector.processResponse(response2);
        
        Assert.assertEquals(selector.getOutstandingComponents(), Arrays.asList(new String[0]));
        try {
            selector.isFinished();
            Assert.fail("Should throw an " + UnableToFinishException.class.getName());
        } catch (UnableToFinishException e) {
            // expected
        }
        
        addStep("Process the same response twice.", "Should throw an exception");
        try {
            selector.processResponse(response2);
            Assert.fail("Should throw an " + UnexpectedResponseException.class.getName());
        } catch (UnexpectedResponseException e) {
            // expected
        }
        
        addStep("Good case. New selector and a positive response.", "Should finish");
        selector = new SpecificPillarSelectorForReplaceFile(Arrays.asList(PILLAR_ID_1), PILLAR_ID_1);
        IdentifyPillarsForReplaceFileResponse goodResponse = new IdentifyPillarsForReplaceFileResponse();
        goodResponse.setFrom(PILLAR_ID_1);
        goodResponse.setPillarID(PILLAR_ID_1);
        goodResponse.setResponseInfo(ResponseInfoUtils.getPositiveIdentification());
        selector.processResponse(goodResponse);
        
        Assert.assertTrue(selector.isFinished());
    }

    
    private ResponseInfo createNegativeIdentificationResponseInfo() {
        ResponseInfo resInfo = new ResponseInfo();
        resInfo.setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        resInfo.setResponseText("Failure");
        return resInfo;
    }
}
