/*
 * #%L
 * Bitrepository Webclient
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
package org.bitrepository;

import javax.jms.JMSException;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.modify.replacefile.ReplaceFileClient;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.collectionsettings.CollectionSettings;
import org.bitrepository.utils.HexUtils;
import org.bitrepository.utils.XMLGregorianCalendarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class BasicClient {
    private PutFileClient putClient;
    private GetFileClient getClient;
    private GetChecksumsClient getChecksumClient;
    private GetFileIDsClient getFileIDsClient;
    private DeleteFileClient deleteFileClient;
    private ReplaceFileClient replaceFileClient;
    private EventHandler eventHandler;
    private String logFile;
    private SecurityManager securityManager;
    private Settings settings;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ArrayBlockingQueue<String> shortLog;
    private List<URL> completedFiles;

    public BasicClient(Settings settings, SecurityManager securityManager, String logFile, String clientID) {
        log.debug("---- Basic client instanciating ----");
        this.logFile = logFile;
        changeLogFiles();
        shortLog = new ArrayBlockingQueue<String>(50);
        eventHandler = new BasicEventHandler(logFile, shortLog);
        completedFiles = new CopyOnWriteArrayList<URL>();
        this.settings = settings;
        this.securityManager = securityManager;
        putClient = ModifyComponentFactory.getInstance().retrievePutClient(settings, this.securityManager, clientID);
        getClient = AccessComponentFactory.getInstance().createGetFileClient(settings, this.securityManager, clientID);
        getChecksumClient = AccessComponentFactory.getInstance().createGetChecksumsClient(settings, 
                this.securityManager, clientID);
        getFileIDsClient = AccessComponentFactory.getInstance().createGetFileIDsClient(settings, 
                this.securityManager, clientID);
        deleteFileClient = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(settings, 
                this.securityManager, clientID);
        replaceFileClient = ModifyComponentFactory.getInstance().retrieveReplaceFileClient(settings, 
                this.securityManager, clientID);
        log.debug("---- Basic client instantiated ----");

    }

    public void shutdown() {
        try {
            MessageBusManager.getMessageBus(settings.getCollectionID()).close();
        } catch (JMSException e) {
            log.warn("Failed to shutdown message bus cleanly, " + e.getMessage());
        }
    }

    public String putFile(String fileID, long fileSize, URL url, String putChecksum, String putChecksumType,
            String putSalt, String approveChecksumType, String approveSalt) {
        ChecksumDataForFileTYPE checksumDataForNewFile = null;
        if(putChecksum != null) {
            checksumDataForNewFile = makeChecksumData(putChecksum, putChecksumType, putSalt);
        }

        ChecksumSpecTYPE checksumRequestForNewFile = null;
        if(approveChecksumType != null) {
            checksumRequestForNewFile = makeChecksumSpec(approveChecksumType, approveSalt);
        }

        putClient.putFile(url, fileID, fileSize, checksumDataForNewFile, checksumRequestForNewFile, 
                eventHandler, generateAuditTrailMessage("PutFile"));
        return "Placing '" + fileID + "' in Bitrepository :)";
    }

    public String getFile(String fileID, URL url) {
        GetFileEventHandler handler = new GetFileEventHandler(url, completedFiles, eventHandler);
        getClient.getFileFromFastestPillar(fileID, null, url, handler, null);
        return "Fetching '" + fileID + "' from Bitrepository :)";
    }

    public String getLog() {
        File logfile = new File(logFile);
        try {
            FileReader fr = new FileReader(logfile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line + "\n");
            }
            return result.toString();
        } catch (FileNotFoundException e) {
            return "Unable find log file... '" + logfile.getAbsolutePath() + "'";
        } catch (IOException e) {
            return "Unable to read log... '" + logfile.getAbsolutePath() + "'";
        }
    }

    public String getHtmlLog() {
        File logfile = new File(logFile);
        try {
            FileReader fr = new FileReader(logfile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line + "<br>");
            }
            return result.toString();
        } catch (FileNotFoundException e) {
            return "Unable find log file... '" + logfile.getAbsolutePath() + "'";
        } catch (IOException e) {
            return "Unable to read log... '" + logfile.getAbsolutePath() + "'";
        }
    }

    public String getShortHtmlLog() {
        StringBuilder sb = new StringBuilder();
        List<String> entries = new ArrayList<String>();
        for(String entry : shortLog) {
            entries.add(entry);
        }
        Collections.reverse(entries);
        for(String entry : entries) {
            sb.append(entry + "<br>");
        }

        return sb.toString();
    }

    public String getSettingsSummary() {
        StringBuilder sb = new StringBuilder();
        CollectionSettings collectionSettings = settings.getCollectionSettings();
        sb.append("CollectionID: <i>" + collectionSettings.getCollectionID() + "</i><br>");
        sb.append("Pillar(s) in configuration: <br> <i>");
        List<String> pillarIDs = collectionSettings.getClientSettings().getPillarIDs(); 
        for(String pillarID : pillarIDs) {
            sb.append("&nbsp;&nbsp;&nbsp; " + pillarID + "<br>");
        }
        sb.append("</i>");
        sb.append("Messagebus URL: <br> &nbsp;&nbsp;&nbsp; <i>"); 
        sb.append(collectionSettings.getProtocolSettings().getMessageBusConfiguration().getURL() + "</i><br>");
        return sb.toString();
    }

    public List<String> getPillarList() {
        return settings.getCollectionSettings().getClientSettings().getPillarIDs();
    }

    public Map<String, Map<String, String>> getChecksums(String fileIDsText, String checksumType, String salt) {
        ChecksumSpecTYPE checksumSpecItem = makeChecksumSpec(checksumType, salt);
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(fileIDsText);

        GetChecksumsResults results = new GetChecksumsResults();
        GetChecksumsEventHandler handler = new GetChecksumsEventHandler(results, eventHandler);

        getChecksumClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(),
                fileIDs, checksumSpecItem, null, handler, generateAuditTrailMessage("GetChecksum"));

        try {
            while(!results.isDone() && !results.hasFailed()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            // Uhm, we got aborted, should return error..
        }
        return results.getResults();
    }

    public GetFileIDsResults getFileIDs(String fileIDsText, boolean allFileIDs) {
        GetFileIDsResults results = new GetFileIDsResults(
                settings.getCollectionSettings().getClientSettings().getPillarIDs());
        GetFileIDsEventHandler handler = new GetFileIDsEventHandler(results, eventHandler);
        FileIDs fileIDs = new FileIDs();

        if(allFileIDs) {
            fileIDs.setAllFileIDs(allFileIDs);
        } else {
            fileIDs.setFileID(fileIDsText);
        }
        try {
            getFileIDsClient.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(),
                    fileIDs, null, handler);

            while(!results.isDone() && !results.hasFailed()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            // Uhm, we got aborted, should return error..
        }		
        return results;
    }

    public String deleteFile(String fileID, String pillarID, String deleteChecksum, String deleteChecksumType, 
            String deleteChecksumSalt, String approveChecksumType, String approveChecksumSalt) {
        if(fileID == null) {
            return "Missing fileID!";
        }
        if(pillarID == null || !settings.getCollectionSettings().getClientSettings().getPillarIDs().contains(pillarID)) {
            return "Missing or unknown pillarID!";
        }
        if(deleteChecksum == null || deleteChecksum.equals("")) {
            return "Checksum for pillar check is missing";
        }
        if(deleteChecksumType == null || deleteChecksumType.equals("")) {
            return "Checksum type for pillar check is invalid";
        }
        ChecksumDataForFileTYPE verifyingChecksum = makeChecksumData(deleteChecksum, deleteChecksumType, 
                deleteChecksumSalt);
        ChecksumSpecTYPE requestedChecksumSpec = null;
        log.info("----- Got DeleteFileRequest with approveChecksumtype = " + approveChecksumType);
        if(approveChecksumType != null && !approveChecksumType.equals("disabled")) {
            requestedChecksumSpec = makeChecksumSpec(approveChecksumType, approveChecksumSalt);
        }      

        deleteFileClient.deleteFile(fileID, pillarID, verifyingChecksum, requestedChecksumSpec, 
                eventHandler, generateAuditTrailMessage("DeleteFile"));

        return "Deleting file";
    }

    public String replaceFile(String fileID, String pillarID, String oldFileChecksum, String oldFileChecksumType,
            String oldFileChecksumSalt, String oldFileRequestChecksumType, String oldFileRequestChecksumSalt,
            URL url, long newFileSize, String newFileChecksum, String newFileChecksumType,
            String newFileChecksumSalt, String newFileRequestChecksumType, String newFileRequestChecksumSalt) {
        if(fileID == null) {
            return "Missing fileID!";
        }
        if(pillarID == null || !settings.getCollectionSettings().getClientSettings().getPillarIDs().contains(pillarID)) {
            return "Missing or unknown pillarID!";
        }
        if(oldFileChecksum == null || oldFileChecksum.equals("")) {
            return "Checksum for pillar check of old file is missing";
        }
        if(oldFileChecksumType == null || oldFileChecksumType.equals("")) {
            return "Checksum type for pillar check of old file is invalid";
        }
        if(newFileChecksum == null || newFileChecksum.equals("")) {
            return "Checksum for pillar check of new file is missing";
        }
        if(newFileChecksumType == null || newFileChecksumType.equals("")) {
            return "Checksum type for pillar check of new file is invalid";
        }
        if(url == null) {
            return "Url for the file is missing.";
        }
        ChecksumDataForFileTYPE oldFileChecksumData = makeChecksumData(oldFileChecksum, oldFileChecksumType,
                oldFileChecksumSalt);
        ChecksumDataForFileTYPE newFileChecksumData = makeChecksumData(newFileChecksum, newFileChecksumType,
                newFileChecksumSalt);
        ChecksumSpecTYPE oldFileChecksumRequest = null;
        if(oldFileRequestChecksumType != null && (!oldFileRequestChecksumType.equals("disabled") &&
                !oldFileRequestChecksumType.trim().equals(""))) {
            oldFileChecksumRequest = makeChecksumSpec(oldFileRequestChecksumType, oldFileRequestChecksumSalt);
        }
        ChecksumSpecTYPE newFileChecksumRequest = null;
        if(newFileRequestChecksumType != null && (!newFileRequestChecksumType.equals("disabled") &&
                !newFileRequestChecksumType.trim().equals(""))) {
            newFileChecksumRequest = makeChecksumSpec(newFileRequestChecksumType, newFileRequestChecksumSalt);
        }

        replaceFileClient.replaceFile(fileID, pillarID, oldFileChecksumData, oldFileChecksumRequest, url, 
                newFileSize, newFileChecksumData, newFileChecksumRequest, eventHandler, generateAuditTrailMessage("ReplaceFile"));

        return "Replacing file";
    }

    public String getCompletedFiles() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Completed files:</b><br>");
        for(URL url : completedFiles) {
            sb.append("<a href=\"" + url.toExternalForm() + "\">" + url.getFile() + "</a> <br>");
        }

        return sb.toString();
    }

    private void changeLogFiles() {
        File oldLogFile = new File(logFile);
        String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String newName = logFile + "-" + date;
        System.out.println("Moving old log file to: " + newName);
        oldLogFile.renameTo(new File(newName));
    }

    private ChecksumDataForFileTYPE makeChecksumData(String checksum, String checksumType, String checksumSalt) {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumValue(HexUtils.stringToByteArray(checksum));
        Date now = new Date();
        checksumData.setCalculationTimestamp(XMLGregorianCalendarConverter.asXMLGregorianCalendar(now));
        checksumData.setChecksumSpec(makeChecksumSpec(checksumType, checksumSalt));
        return checksumData;
    }

    private ChecksumSpecTYPE makeChecksumSpec(String checksumType, String checksumSalt) {
        ChecksumSpecTYPE spec = new ChecksumSpecTYPE();

        if(checksumType == null || checksumType.trim().equals("")) {
            checksumType = settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType();
        }

        if(checksumSalt != null && !checksumSalt.trim().equals("")) {
            spec.setChecksumSalt(HexUtils.stringToByteArray(checksumSalt));
            checksumType = "HMAC_" + checksumType;
        }
        spec.setChecksumType(ChecksumType.fromValue(checksumType));

        return spec;
    }
    
    private String generateAuditTrailMessage(String operationType) {
        return "Webservice initiation of " + operationType + " operation";
    }
}
