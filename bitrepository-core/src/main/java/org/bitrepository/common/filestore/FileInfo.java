/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.filestore;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for the information attached to a given file id.
 */
public interface FileInfo {
    /**
     * @return The ID of the file.
     */
    String getFileID();

    /**
     * @return The inputstream for the data.
     * @exception IOException If any issues regarding retrieving the inputstream occurs.
     */
    InputStream getInputstream() throws IOException;
    
    /**
     * @return The last modified timestamp.
     */
    Long getLastModifiedDate();
    
    /**
     * @return The size of the file.
     */
    long getSize();
}
