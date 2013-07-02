/*
 * #%L
 * Bitrepository Command Line
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
package org.bitrepository.commandline;

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.Option;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.utils.CompleteEventAwaiter;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;

/**
 * Putting a file to the collection.
 */
public class PutFile extends CommandLineClient {
    /** The component id. */
    private final static String COMPONENT_ID = "PutFileClient";

    /** The client for performing the PutFile operation.*/
    private final PutFileClient client;

    /**
     * @param args The arguments for performing the PutFile operation.
     */
    public static void main(String[] args) {
        CommandLineClient.runCommandLineClient(new PutFile(args));
    }

    /**
     * 
     * @param args
     */
    private PutFile(String ... args) {
        super(args);
        client = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, COMPONENT_ID);
    }

    @Override
    protected String getComponentID() {
        return COMPONENT_ID;
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return false;
    }

    /**
     * Perform the PutFile operation.
     */
    public void performOperation() {
        output.startupInfo("Putting .");
        OperationEvent finalEvent = putTheFile();
        output.completeEvent("Results of the PutFile operation for the file '" + getFileIDForMessage(), finalEvent);
        if(finalEvent.getEventType() == OperationEventType.COMPLETE) {
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }

    @Override
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

        Option fileOption = new Option(Constants.FILE_ARG, Constants.HAS_ARGUMENT,
                "The path to the file, which is wanted to be put");
        fileOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
        cmdHandler.addOption(fileOption);

        Option checksumTypeOption = new Option(Constants.REQUEST_CHECKSUM_TYPE_ARG, Constants.HAS_ARGUMENT, 
                "[OPTIONAL] The algorithm of checksum to request in the response from the pillars.");
        checksumTypeOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumTypeOption);

        Option checksumSaltOption = new Option(Constants.REQUEST_CHECKSUM_SALT_ARG, Constants.HAS_ARGUMENT, 
                "[OPTIONAL] The salt of checksum to request in the response. Requires the ChecksumType argument.");
        checksumSaltOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumSaltOption);

        Option deleteOption = new Option(Constants.DELETE_FILE_ARG, Constants.NO_ARGUMENT, 
                "If this argument is present, then the file will be removed from the server, "
                        + "when the operation is complete.");
        deleteOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(deleteOption);
    }

    /**
     * Initiates the operation and waits for the results.
     * @return The final event for the results of the operation. Either 'FAILURE' or 'COMPLETE'.
     */
    private OperationEvent putTheFile() {
        output.debug("Uploading the file to the FileExchange.");
        File f = findTheFile();
        FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
        URL url = fileexchange.uploadToServer(f);
        String fileId = retrieveTheName(f);

        output.debug("Initiating the PutFile conversation.");
        ChecksumDataForFileTYPE validationChecksum = getValidationChecksum(f);
        ChecksumSpecTYPE requestChecksum = getRequestChecksumSpec();

        CompleteEventAwaiter eventHandler = new CompleteEventAwaiter(settings, output);
        client.putFile(getCollectionID(), url, fileId, f.length(), validationChecksum, requestChecksum, eventHandler,
                "Putting the file '" + f + "' with the file id '" + fileId + "' from commandLine.");

        if(cmdHandler.hasOption(Constants.DELETE_FILE_ARG)) {
            try {
                fileexchange.deleteFromServer(url);
            } catch (Exception e) {
                System.err.println("Issue regarding removing file from server: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return eventHandler.getFinish();
    }

    /**
     * Finds the file from the arguments.
     * @return The requested file.
     */
    private File findTheFile() {
        String filePath = cmdHandler.getOptionValue(Constants.FILE_ARG);

        File file = new File(filePath);
        if(!file.isFile()) {
            throw new IllegalArgumentException("The file '" + filePath + "' is invalid. It does not exists or it "
                    + "is a directory.");
        }

        return file;
    }

    /**
     * Extracts the id of the file to be put.
     * @return The either the value of the file id argument, or no such option, then the name of the file.
     */
    private String retrieveTheName(File f) {
        if(cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            return cmdHandler.getOptionValue(Constants.FILE_ID_ARG);
        } else {
            return f.getName();
        }
    }

    /**
     * Creates the data structure for encapsulating the validation checksums for validation of the PutFile operation.
     * @param file The file to have the checksum calculated.
     * @return The ChecksumDataForFileTYPE for the pillars to validate the PutFile operation.
     */
    private ChecksumDataForFileTYPE getValidationChecksum(File file) {
        ChecksumSpecTYPE csSpec = ChecksumUtils.getDefault(settings);
        String checksum = ChecksumUtils.generateChecksum(file, csSpec);

        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumSpec(csSpec);
        res.setChecksumValue(Base16Utils.encodeBase16(checksum));

        return res;
    }

    /**
     * @return The requested checksum spec, or null if the arguments does not exist.
     */
    private ChecksumSpecTYPE getRequestChecksumSpec() {
        if(!cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG)) {
            return null;
        }

        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumType(ChecksumType.fromValue(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));

        if(cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_SALT_ARG)) {
            res.setChecksumSalt(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));
        }
        return res;
    }

    /**
     * @return The filename for the file to upload. 
     */
    private String getFileIDForMessage() {
        return cmdHandler.getOptionValue(Constants.FILE_ARG) + (cmdHandler.hasOption(Constants.FILE_ID_ARG) ? 
                " (with the id '" + cmdHandler.getOptionValue(Constants.FILE_ID_ARG) + "')" : "");
    }
}
