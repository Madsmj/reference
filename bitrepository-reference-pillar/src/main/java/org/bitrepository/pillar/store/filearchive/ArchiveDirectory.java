/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.store.filearchive;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.FileUtils;

/**
 * Manager interface for a given archival directory, with the subdirectories 'tempDir', 'fileDir' and 'retainDir'.
 * A new file is ingested into the 'tempDir', where it can be validated before it is moved to the 'fileDir'.
 * If a file is to be deleted, then it is moved from the 'fileDir' to the 'retainDir'.
 */
public class ArchiveDirectory {
    /** Constant for the temporary directory name.*/
    public static final String TEMPORARY_DIR = "tmpDir";
    /** Constant for the file archive name.*/
    public static final String ARCHIVE_DIR = "fileDir";
    /** Constant for the retain directory name.*/
    public static final String RETAIN_DIR = "retainDir";

    /** The directory for the files. Contains three sub directories: tempDir, fileDir and retainDir.*/
    private File baseDepositDir;

    /** The directory where files are being downloaded to before they are put into the filedir. */
    private File tmpDir;
    /** The directory where the files are being stored.*/
    private final File fileDir;
    /** The directory where the files are moved, when they are removed from the archive.*/
    private final File retainDir;

    /** 
     * Constructor. Initialises the file directory. 
     * 
     * @param dirName The directory for this archive.
     */
    public ArchiveDirectory(String dirName) {
        ArgumentValidator.checkNotNullOrEmpty(dirName, "String dirName");

        // Instantiate the directories for this archive.
        baseDepositDir = FileUtils.retrieveDirectory(dirName);
        tmpDir = FileUtils.retrieveSubDirectory(baseDepositDir, TEMPORARY_DIR);
        fileDir = FileUtils.retrieveSubDirectory(baseDepositDir, ARCHIVE_DIR);
        retainDir = FileUtils.retrieveSubDirectory(baseDepositDir, RETAIN_DIR);
    }
    
    /**
     * @param fileID The id of the file to retrieve.
     * @return The requested file, or a null if no such file exists.
     */
    public File getFile(String fileID) {
        File res = new File(fileDir, fileID);
        
        if(res.isFile()) {
            return res;
        } else {
            return null;
        }
    }
    
    /**
     * @param fileID The file id to test whether it exists.
     * @return Whether the given file exists within the archive.
     */
    public boolean hasFile(String fileID) {
        return new File(fileDir, fileID).isFile();
    }
    
    /**
     * @return Retrieves the list of archived files.
     */
    public List<String> getFileIds() {
        return Arrays.asList(fileDir.list());
    }
    
    /**
     * @return The number of bytes left for the base directory.
     */
    public Long getBytesLeft() {
        return baseDepositDir.getFreeSpace();
    }
    
    /**
     * @param fileID The id of the file
     * @return A new file in the temporary directory.
     */
    public File getFileInTempDir(String fileID) {
        File res = new File(tmpDir, fileID);
        if(!res.exists()) {
            throw new IllegalStateException("The file '" + fileID + "' does not exist within the tmpDir.");
        }
        return res;
    }
    
    /**
     * @param fileID The id of the file
     * @return A new file in the temporary directory.
     */
    public File getNewFileInTempDir(String fileID) {
        File res = new File(tmpDir, fileID);
        if(res.exists()) {
            throw new IllegalStateException("Cannot create a new file in the temporary directory.");
        }
        return res;
    }
    
    /**
     * @param fileID The id of the file.
     * @return Whether a given file exist in the temporary directory.
     */
    public boolean hasFileInTempDir(String fileID) {
        return new File(tmpDir, fileID).isFile();
    }
    
    /**
     * Moves a file from the tmpDir to the archive dir.
     * @param fileID The id of the file to
     */
    public void moveFromTmpToArchive(String fileID) {
        File tmpFile = new File(tmpDir, fileID);
        File archiveFile = new File(fileDir, fileID);
        
        if(!tmpFile.isFile()) {
            throw new IllegalStateException("The file '" + fileID + "' does not exist within the tmpDir.");
        }
        if(archiveFile.isFile()) {
            throw new IllegalStateException("The file '" + fileID + "' does already exist within the fileDir.");
        }
        
        // Move the file to the fileDir.
        FileUtils.moveFile(tmpFile, archiveFile);
    }
    
    /**
     * The file to remove from the archive. The file is just moved from the fileDir to the retainDir. 
     * @param fileID The id of the file to remove from archive.
     */
    public void removeFileFromArchive(String fileID) {
        File oldFile = new File(fileDir, fileID);
        if(!oldFile.isFile()) {
            throw new IllegalStateException("Cannot locate the file to delete '" + oldFile.getAbsolutePath() + "'!");
        }
        File retainFile = new File(retainDir, fileID);
        
        // If a version of the file already has been retained, then it should be deprecated.
        if(retainFile.exists()) {
            FileUtils.deprecateFile(retainFile);
        }
        
        FileUtils.moveFile(oldFile, retainFile);
    }

    /**
     * The file to remove from the temporary directory. The file is just moved from the fileDir to the retainDir.
     * This should be used e.g. to clean up after a failed PutFile. 
     * @param fileID The id of the file to remove from tmpdir.
     */
    public void removeFileFromTmp(String fileID) {
        File oldFile = new File(tmpDir, fileID);
        if(!oldFile.isFile()) {
            throw new IllegalStateException("Cannot locate the file to delete '" + oldFile.getAbsolutePath() + "'!");
        }
        File retainFile = new File(retainDir, fileID);
        
        // If a version of the file already has been retained, then it should be deprecated.
        if(retainFile.exists()) {
            FileUtils.deprecateFile(retainFile);
        }
        
        FileUtils.moveFile(oldFile, retainFile);
    }
}
