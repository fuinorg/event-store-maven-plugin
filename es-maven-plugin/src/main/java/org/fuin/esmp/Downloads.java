/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. 
 * http://www.fuin.org/
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
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.esmp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses all available versions and provides easy access to this information.
 */
public final class Downloads {

    private static final Logger LOG = LoggerFactory.getLogger(Downloads.class);

    private static final int TIMEOUT_30_SECONDS = 1000 * 30;

    private final File jsonDownloadsFile;

    private final List<DownloadVersion> versions;

    /**
     * Constructor with URL and local file. If the file does not exist, a current version of the version file will be loaded.
     * 
     * @param versionURL
     *            URL of the version JSON file.
     * @param jsonDownloadsFile
     *            Name of the JSON download file.
     * 
     * @throws IOException
     *             Copying the event store version file from URL to local disc failed.
     */
    public Downloads(final URL versionURL, final File jsonDownloadsFile) throws IOException {
        super();
        this.jsonDownloadsFile = jsonDownloadsFile;
        this.versions = new ArrayList<>();

        if (!jsonDownloadsFile.exists()) {
            LOG.info("Download version file: " + versionURL);
            FileUtils.copyURLToFile(versionURL, jsonDownloadsFile, TIMEOUT_30_SECONDS, TIMEOUT_30_SECONDS);
        }
        LOG.info("Local version file: " + jsonDownloadsFile);

    }

    /**
     * Returns the version with a given name.
     * 
     * @param name
     *            Version to find.
     * 
     * @return Found instance or <code>null</code> if no version with that name was found.
     */
    public DownloadVersion findVersion(final String name) {
        final int idx = versions.indexOf(new DownloadVersion(name));
        if (idx < 0) {
            return null;
        }
        return versions.get(idx);
    }

    /**
     * Loads the data from the JSON download versions file.
     * 
     * @throws IOException
     *             Parsing the event store version file failed.
     */
    public final void parse() throws IOException {

        final Reader reader = new FileReader(jsonDownloadsFile);
        try {

            final JsonReader jsonReader = Json.createReader(reader);
            final JsonObject versionsObj = jsonReader.readObject();
            final Iterator<String> versionIt = versionsObj.keySet().iterator();
            while (versionIt.hasNext()) {
                final DownloadVersion version = new DownloadVersion(versionIt.next());
                versions.add(version);

                final JsonObject families = versionsObj.getJsonObject(version.getName());
                final Iterator<String> familyIt = families.keySet().iterator();
                while (familyIt.hasNext()) {
                    final DownloadOSFamily family = new DownloadOSFamily(familyIt.next());
                    version.addOSFamily(family);
                    final JsonArray downloads = families.getJsonArray(family.getName());
                    for (int i = 0; i < downloads.size(); i++) {
                        final JsonObject download = downloads.getJsonObject(i);
                        final String name = download.getString("name");
                        final String url = download.getString("url");
                        if (url.endsWith(".tar.gz") || url.endsWith(".zip")) {
                            family.addOS(new DownloadOS(name, url));
                        }
                    }
                }

            }

            for (final DownloadVersion version : versions) {
                version.seal();
            }

        } finally {
            reader.close();
        }

        for (final DownloadVersion version : versions) {
            LOG.info("{}", version);
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
     * Returns the version list.
     * 
     * @return Available download versions.
     */
    public List<DownloadVersion> getVersions() {
        return Collections.unmodifiableList(versions);
    }

    /**
     * Returns the latest available version.
     * 
     * @param includeRC Include release candidates ({@code true}) or only return releases ({@code false}).
     * 
     * @return Latest version.
     */
    public DownloadVersion findLatest(final boolean includeRC) {
        for (final DownloadVersion version : versions) {
            if (includeRC || version.isRelease()) {
                return version;
            }
        }
        throw new IllegalStateException("No version found");
    }

}
