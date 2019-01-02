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
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Base class for all mojos.
 */
public abstract class AbstractEventStoreMojo extends AbstractMojo {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventStoreMojo.class);

    private static final String PID_FILE_NAME = "event-store-pid";

    /** URL of the JSON file with available event store versions. */
    public static final String VERSION_URL = "https://eventstore.org/downloads/downloads.json";

    /**
     * URl of the version JSON file.
     */
    @Parameter(name = "version-url", defaultValue = VERSION_URL)
    private String versionUrl = VERSION_URL;

    /**
     * Full URL where the event store file to download is located. If set, this always overrides the automatic download of the latest
     * version.
     */
    @Parameter(name = "download-url")
    private String downloadUrl;

    /**
     * Qualifier that helps selecting the right download.
     * 
     * Examples are: "Ubuntu", "Linux", "macOS" or "Windows". As of January 2019 only for "Linux" and "Ubuntu" it's necessary to provide
     * this value. Defaults to "Ubuntu" in case of a Linux OS.
     */
    @Parameter(name = "download-os-qualifier")
    private String downloadOsQualifier;

    /**
     * Determines if release candidates (versions with "-rc") should be included.
     */
    @Parameter(name = "include-rc", defaultValue = "false")
    private String includeRc;

    /**
     * The target build directory.
     */
    @Parameter(name = "target-dir", property = "project.build.directory")
    private File targetDir = new File("./target");

    /**
     * Directory where the event store should be installed. The downloaded archive will be uncompressed into this directory.
     */
    @Parameter(name = "event-store-dir")
    private File eventStoreDir;

    /**
     * Checks if a variable is not <code>null</code> and throws an <code>IllegalNullArgumentException</code> if this rule is violated.
     * 
     * @param name
     *            Name of the variable to be displayed in an error message.
     * @param value
     *            Value to check for <code>null</code>.
     * 
     * @throws MojoExecutionException
     *             Checked value was NULL.
     */
    protected final void checkNotNull(final String name, final Object value) throws MojoExecutionException {
        if (value == null) {
            throw new MojoExecutionException(name + " cannot be null!");
        }
    }

    @Override
    public final void execute() throws MojoExecutionException {
        StaticLoggerBinder.getSingleton().setMavenLog(getLog());
        init();
        LOG.info("version-url={}", versionUrl);
        LOG.info("download-url={}", downloadUrl);
        LOG.info("\n" + "        LOG={}", downloadOsQualifier);
        LOG.info("includeRc={}", includeRc);
        LOG.info("target-dir={}", targetDir);
        LOG.info("event-store-dir={}", eventStoreDir);
        executeGoal();
    }

    // CHECKSTYLE:OFF Cyclomatic complexity - Not nice, but OK for now
    private void init() throws MojoExecutionException {

        // Only initialize other stuff if no full URL is provided
        if (downloadUrl == null) {
            initUsingLatest();
        }

        // If it's not explicitly set, create it with target directory
        if (eventStoreDir == null) {

            if (downloadUrl.endsWith(".zip")) {
                eventStoreDir = new File(canonicalFile(targetDir), FilenameUtils.getBaseName(downloadUrl));
            } else if (downloadUrl.endsWith(".tar.gz")) {
                eventStoreDir = new File(canonicalFile(targetDir), FilenameUtils.getBaseName(FilenameUtils.getBaseName(downloadUrl)));
            } else {
                throw new MojoExecutionException("Cannot handle archive with this extension: " + downloadUrl);
            }

        }

    }

    // CHECKSTYLE:ON

    private void initDownloadOSQualifier() throws MojoExecutionException {
        if (downloadOsQualifier == null) {
            if (OS.isFamilyWindows()) {
                downloadOsQualifier = "Windows";
            } else if (OS.isFamilyMac()) {
                downloadOsQualifier = "macOS";
            } else if (OS.isFamilyUnix()) {
                downloadOsQualifier = "Ubuntu";
            } else {
                throw new MojoExecutionException("Unknown OS - You must use the 'archive-name' parameter");
            }
        }
    }

    private String getOS() throws MojoExecutionException {
        if (OS.isFamilyWindows()) {
            return "Windows";
        }
        if (OS.isFamilyMac()) {
            return "Mac";
        }
        if (OS.isFamilyUnix()) {
            return "Linux";
        }
        throw new MojoExecutionException("Unknown OS - You must use the 'archive-name' parameter");
    }

    private void initUsingLatest() throws MojoExecutionException {

        initDownloadOSQualifier();

        try {
            final URL versionURL = new URL(versionUrl);
            final File jsonVersionFile = new File(canonicalFile(targetDir), "event-store-versions.json");
            final Downloads downloads = new Downloads(versionURL, jsonVersionFile);
            downloads.parse();

            final String os = getOS();

            final DownloadVersion version = downloads.findLatest(isIncludeRc());

            final DownloadOSFamily family = version.findFamily(os);
            if (family == null) {
                throw new MojoExecutionException("Couldn't find OS family '" + os + "' in '" + downloads.getJsonDownloadsFile() + "'");
            }

            final DownloadOS download = family.findLatestDownload(downloadOsQualifier);
            if (download == null) {
                throw new MojoExecutionException("Couldn't find eyntr with download OS qualifier '" + downloadOsQualifier + "' (version='"
                        + version + "', family='" + family + "') in '" + downloads.getJsonDownloadsFile() + "'");
            }

            downloadUrl = download.getUrl();

        } catch (final IOException ex) {
            throw new MojoExecutionException("Error parsing the event store version file", ex);
        }

    }

    private File canonicalFile(final File file) throws MojoExecutionException {
        try {
            return file.getCanonicalFile();
        } catch (final IOException ex) {
            throw new MojoExecutionException("Error creating canonical file: " + file, ex);
        }
    }

    /**
     * Returns the full URL where the event store file to download is located.
     * 
     * @return Download archive URL.
     * 
     * @throws MojoExecutionException
     *             Error initializing the variable.
     */
    public final String getDownloadUrl() throws MojoExecutionException {
        if (downloadUrl == null) {
            init();
        }
        return downloadUrl;
    }

    /**
     * Sets the full URL where the event store file to download is located.
     * 
     * @param downloadUrl
     *            Download archive URL to set
     */
    public final void setDownloadUrl(final String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * Returns the directory where the event store should be installed. The downloaded archive will be uncompressed into this directory.
     * 
     * @return Target directory.
     * 
     * @throws MojoExecutionException
     *             Error initializing the variable.
     */
    public final File getEventStoreDir() throws MojoExecutionException {
        if (eventStoreDir == null) {
            init();
        }
        return eventStoreDir;
    }

    /**
     * Sets the directory where the event store should be installed. The downloaded archive will be uncompressed into this directory.
     * 
     * @param eventStoreDir
     *            Target directory to set
     */
    public final void setEventStoreDir(final File eventStoreDir) {
        this.eventStoreDir = eventStoreDir;
    }

    /**
     * Returns the target build directory.
     * 
     * @return The build directory.
     */
    public final File getTargetDir() {
        return targetDir;
    }

    /**
     * Sets the target build directory.
     * 
     * @param targetDir
     *            The build directory to set
     */
    public final void setTargetDir(final File targetDir) {
        this.targetDir = targetDir;
    }

    /**
     * Returns the qualifier that helps selecting the right download.
     * 
     * @return Qualifier.
     */
    public String getDownloadOsQualifier() {
        return downloadOsQualifier;
    }

    /**
     * Sets the qualifier that helps selecting the right download.
     * 
     * @param downloadOsQualifier
     *            Qualifier.
     */
    public void setDownloadOsQualifier(String downloadOsQualifier) {
        this.downloadOsQualifier = downloadOsQualifier;
    }

    /**
     * Determines if release candidates (versions with "-rc") should be included.
     * 
     * @return {@code true} if RC candidates should be included.
     */
    public String getIncludeRc() {
        return includeRc;
    }

    /**
     * Determines if release candidates (versions with "-rc") should be included.
     * 
     * @return {@code true} if RC candidates should be included.
     */
    public boolean isIncludeRc() {
        if (includeRc == null) {
            return false;
        }
        return Boolean.valueOf(includeRc);
    }
    
    /**
     * Determines if release candidates (versions with "-rc") should be included.
     * 
     * @param includeRc
     *            {@code true} if RC candidates should be included.
     */
    public void setIncludeRc(String includeRc) {
        this.includeRc = includeRc;
    }

    /**
     * Returns the URl of the version JSON file.
     * 
     * @return URl of the version JSON file.
     */
    public String getVersionUrl() {
        return versionUrl;
    }

    /**
     * Sets the URl of the version JSON file.
     * 
     * @param versionUrl
     *            URl of the version JSON file.
     */
    public void setVersionUrl(String versionUrl) {
        this.versionUrl = versionUrl;
    }

    /**
     * Writes the process ID of the event store to a file in the target directory.
     * 
     * @param pid
     *            PID to write.
     * 
     * @throws MojoExecutionException
     *             Error writing the PID to file.
     */
    protected final void writePid(final String pid) throws MojoExecutionException {
        try {
            FileUtils.write(getPidFile(), pid);
        } catch (final IOException ex) {
            throw new MojoExecutionException("Couldn't write the PID '" + pid + "' to file: " + getPidFile(), ex);
        }
    }

    /**
     * Reads the process ID of the event store from a file in the target directory.
     * 
     * @return PID from file.
     * 
     * @throws MojoExecutionException
     *             Error reading the PID from file.
     */
    protected final String readPid() throws MojoExecutionException {
        try {
            return FileUtils.readFileToString(getPidFile());
        } catch (final IOException ex) {
            throw new MojoExecutionException("Couldn't read the PID from file: " + getPidFile(), ex);
        }
    }

    /**
     * Deletes the process ID file in the target directory.
     * 
     * @throws MojoExecutionException
     *             Error deleting the PID file.
     */
    protected final void deletePid() throws MojoExecutionException {
        final boolean ok = getPidFile().delete();
        if (!ok) {
            throw new MojoExecutionException("Couldn't delete the PID file: " + getPidFile());
        }
    }

    /**
     * Returns the PID file.
     * 
     * @return Process ID file.
     */
    protected final File getPidFile() {
        return new File(getTargetDir(), PID_FILE_NAME);
    }

    /**
     * Returns the string as list.
     * 
     * @param str
     *            String to split into lines.
     * 
     * @return List of lines.
     * 
     * @throws MojoExecutionException
     *             Error splitting the string into lines.
     */
    protected final List<String> asList(final String str) throws MojoExecutionException {
        try {
            final List<String> lines = new ArrayList<String>();
            final LineNumberReader reader = new LineNumberReader(new StringReader(str));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (final IOException ex) {
            throw new MojoExecutionException("Error creating string list", ex);
        }
    }

    /**
     * Logs all lines in debug mode.
     * 
     * @param messages
     *            Lines to log.
     */
    protected final void logDebug(final List<String> messages) {
        if (LOG.isDebugEnabled()) {
            for (final String message : messages) {
                LOG.debug(message);
            }
        }
    }

    /**
     * Logs all lines in error mode.
     * 
     * @param messages
     *            Lines to log.
     */
    protected final void logError(final List<String> messages) {
        if (LOG.isErrorEnabled()) {
            for (final String message : messages) {
                LOG.error(message);
            }
        }
    }

    /**
     * Executes the goal code.
     * 
     * @throws MojoExecutionException
     *             if goal execution failed
     */
    protected abstract void executeGoal() throws MojoExecutionException;

}
