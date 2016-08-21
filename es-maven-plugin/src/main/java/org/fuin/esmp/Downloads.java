/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. 
 * <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fuin.esmp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.FileUtils;

/**
 * Parses all available versions and provides easy access to this information.
 */
public final class Downloads {

    private static final String VERSION_URL = "https://geteventstore.com/downloads/downloads";

    private static final int TIMEOUT_30_SECONDS = 1000 * 30;

    private final File jsonDownloadsFile;

    private final List<DownloadOS> osList;

    /**
     * Constructor that loads the versions from the default URL.
     * 
     * @throws IOException
     *             Error creating a temporary JSON file or reading from the URL.
     */
    public Downloads() throws IOException {
        this(File.createTempFile("event-store-downloads-", ".json"));
        FileUtils.copyURLToFile(new URL(VERSION_URL), jsonDownloadsFile, TIMEOUT_30_SECONDS, TIMEOUT_30_SECONDS);
    }

    /**
     * Constructor with local file.
     * 
     * @param jsonDownloadsFile
     *            Name of the JSON download file.
     */
    public Downloads(final File jsonDownloadsFile) {
        super();
        this.jsonDownloadsFile = jsonDownloadsFile;
        this.osList = new ArrayList<>();
    }

    /**
     * Returns the OS with a given name.
     * 
     * @param name
     *            OS name to find.
     * 
     * @return Found instance or <code>null</code> if no OS with that name was
     *         found.
     */
    public DownloadOS findOS(final String name) {
        if (osList == null) {
            return null;
        }
        final int idx = osList.indexOf(new DownloadOS(name));
        if (idx < 0) {
            return null;
        }
        return osList.get(idx);
    }

    /**
     * Loads the data from the JSON download versions file.
     */
    public final void parse() throws IOException {

        final Reader reader = new FileReader(jsonDownloadsFile);
        try {
            final JsonReader jsonReader = Json.createReader(reader);
            final JsonArray osArray = jsonReader.readArray();
            for (int i = 0; i < osArray.size(); i++) {
                final JsonObject osObj = (JsonObject) osArray.get(i);
                final String os = osObj.getString("os");
                final String currentVersion = osObj.getString("currentVersion");
                final JsonArray downloadsArray = osObj.getJsonArray("downloads");
                final List<DownloadVersion> versions = new ArrayList<>();
                for (int j = 0; j < downloadsArray.size(); j++) {
                    final JsonObject downloadObj = (JsonObject) downloadsArray.get(j);
                    final String version = downloadObj.getString("version");
                    final String url = downloadObj.getString("url");
                    versions.add(new DownloadVersion(version, url));
                }
                Collections.sort(versions);
                osList.add(new DownloadOS(os, currentVersion, versions));
            }
            Collections.sort(osList);
        } finally {
            reader.close();
        }

    }

    /**
     * Returns the parsed file.
     * 
     * @return File.
     */
    public final File getJsonDownloadsFile() {
        return jsonDownloadsFile;
    }

    /**
     * Returns the OS list.
     * 
     * @return Available download versions.
     */
    public List<DownloadOS> getOsList() {
        return Collections.unmodifiableList(osList);
    }

}
