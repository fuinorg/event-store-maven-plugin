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

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all mojos.
 */
public abstract class AbstractEventStoreMojo extends AbstractMojo {

    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractEventStoreMojo.class);

    private static final String PID_FILE_NAME = "event-store-pid";

    /**
     * Full URL where the event store file to download is located. If set, this
     * always overrides the <code>base-url</code>, <code>archive-name</code>,
     * <code>archive-version</code> and <code>archive-extension</code>
     * parameters.
     * 
     * @parameter expression="${download-url}"
     */
    private String downloadUrl;

    /**
     * Base URl where the event store archives are located. This is used to
     * construct an archive URl for the OS where the build script is executed.
     * 
     * @parameter expression="${base-url}"
     *            default-value="http://download.geteventstore.com/binaries/"
     */
    private String baseUrl = "http://download.geteventstore.com/binaries/";

    /**
     * Name of the archive (Like "EventStore-OSS-Win" or "EventStore-OSS-Linux")
     * without version and file extension. This is used to construct an archive
     * URl for the OS where the build script is executed.
     * 
     * @parameter expression="${archive-name}"
     */
    private String archiveName;

    /**
     * Version of the archive (Like "3.0.5"). This is used to construct an
     * archive URl for the OS where the build script is executed.
     * 
     * @parameter expression="${archive-version}" default-value="3.0.5"
     */
    private String archiveVersion = "3.0.5";

    /**
     * File extension of the archive (Like "zip" or "tar.gz"). This is used to
     * construct an archive URl for the OS where the build script is executed.
     * 
     * @parameter expression="${archive-extension}"
     */
    private String archiveExtension;

    /**
     * The target build directory.
     * 
     * @parameter expression="${project.build.directory}"
     */
    private File targetDir = new File("./target");

    /**
     * Directory where the event store should be installed. The downloaded
     * archive will be uncompressed into this directory.
     * 
     * @parameter expression="${event-store-dir}"
     */
    private File eventStoreDir;

    /**
     * Checks if a variable is not <code>null</code> and throws an
     * <code>IllegalNullArgumentException</code> if this rule is violated.
     * 
     * @param name
     *            Name of the variable to be displayed in an error message.
     * @param value
     *            Value to check for <code>null</code>.
     * 
     * @throws MojoExecutionException
     *             Checked value was NULL.
     */
    protected final void checkNotNull(final String name, final Object value)
            throws MojoExecutionException {
        if (value == null) {
            throw new MojoExecutionException(name + " cannot be null!");
        }
    }

    @Override
    public final void execute() throws MojoExecutionException {
        StaticLoggerBinder.getSingleton().setMavenLog(getLog());
        init();
        LOG.info("download-url={}", downloadUrl);
        LOG.info("base-url={}", baseUrl);
        LOG.info("archive-name={}", archiveName);
        LOG.info("archive-version={}", archiveVersion);
        LOG.info("archive-extension={}", archiveExtension);
        LOG.info("target-dir={}", targetDir);
        LOG.info("event-store-dir={}", eventStoreDir);
        executeGoal();
    }

    // CHECKSTYLE:OFF Cyclomatic complexity - Not nice, but OK for now
    private void init() throws MojoExecutionException {

        // Only initialize other stuff if no full URL is provided
        if (downloadUrl == null) {

            // Make sure base URL always ends with a slash
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }

            // Supply variables that are OS dependent
            if (OS.isFamilyWindows()) {
                if (archiveName == null) {
                    archiveName = "EventStore-OSS-Win";
                }
                if (archiveExtension == null) {
                    archiveExtension = "zip";
                }
            } else if (OS.isFamilyMac()) {
                if (archiveName == null) {
                    archiveName = "EventStore-OSS-Mac";
                }
                if (archiveExtension == null) {
                    archiveExtension = "tar.gz";
                }
            } else if (OS.isFamilyUnix()) {
                if (archiveName == null) {
                    archiveName = "EventStore-OSS-Linux";
                }
                if (archiveExtension == null) {
                    archiveExtension = "tar.gz";
                }
            } else {
                if (archiveName == null) {
                    throw new MojoExecutionException(
                            "Unknown OS - You must use the 'archive-name' parameter");
                }
                if (archiveExtension == null) {
                    throw new MojoExecutionException(
                            "Unknown OS - You must use the 'archive-ext' parameter");
                }
            }

            downloadUrl = baseUrl + archiveName + "-v" + archiveVersion + "."
                    + archiveExtension;

        }

        // If it's not explicitly set, create it with target directory
        if (eventStoreDir == null) {

            if (downloadUrl.endsWith(".zip")) {
                eventStoreDir = new File(canonicalFile(targetDir),
                        FilenameUtils.getBaseName(downloadUrl));
            } else if (downloadUrl.endsWith(".tar.gz")) {
                eventStoreDir = new File(canonicalFile(targetDir),
                        FilenameUtils.getBaseName(FilenameUtils
                                .getBaseName(downloadUrl)));
            } else {
                throw new MojoExecutionException(
                        "Cannot handle archive with this extension: "
                                + downloadUrl);
            }

        }

    }

    // CHECKSTYLE:ON

    private File canonicalFile(final File file) throws MojoExecutionException {
        try {
            return file.getCanonicalFile();
        } catch (final IOException ex) {
            throw new MojoExecutionException("Error creating canonical file: "
                    + file, ex);
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
     * Returns the base URl where the event store archives are located.
     * 
     * @return Base URL where archives are located.
     * 
     * @throws MojoExecutionException
     *             Error initializing the variable.
     */
    public final String getBaseUrl() throws MojoExecutionException {
        if (baseUrl == null) {
            init();
        }
        return baseUrl;
    }

    /**
     * Sets the base URl where the event store archives are located.
     * 
     * @param baseUrl
     *            Base URL where archives are located to set
     */
    public final void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the name of the archive (Like "EventStore-OSS-Win" or
     * "EventStore-OSS-Linux") without version and file extension.
     * 
     * @return the archiveName
     * 
     * @throws MojoExecutionException
     *             Error initializing the variable.
     */
    public final String getArchiveName() throws MojoExecutionException {
        if (archiveName == null) {
            init();
        }
        return archiveName;
    }

    /**
     * Sets the name of the archive (Like "EventStore-OSS-Win" or
     * "EventStore-OSS-Linux") without version and file extension.
     * 
     * @param archiveName
     *            Archive name to set.
     */
    public final void setArchiveName(final String archiveName) {
        this.archiveName = archiveName;
    }

    /**
     * Returns the version of the archive (Like "3.0.5").
     * 
     * @return Event store version.
     */
    public final String getArchiveVersion() {
        return archiveVersion;
    }

    /**
     * Sets the version of the archive (Like "3.0.5").
     * 
     * @param archiveVersion
     *            The event store version to set.
     */
    public final void setArchiveVersion(final String archiveVersion) {
        this.archiveVersion = archiveVersion;
    }

    /**
     * Returns the file extension of the archive (Like "zip" or "tar.gz").
     * 
     * @return Archive file extension.
     * 
     * @throws MojoExecutionException
     *             Error initializing the variable.
     */
    public final String getArchiveExtension() throws MojoExecutionException {
        if (archiveExtension == null) {
            init();
        }
        return archiveExtension;
    }

    /**
     * Sets the file extension of the archive (Like "zip" or "tar.gz").
     * 
     * @param archiveExtension
     *            Archive file extension to set.
     */
    public final void setArchiveExtension(final String archiveExtension) {
        this.archiveExtension = archiveExtension;
    }

    /**
     * Returns the directory where the event store should be installed. The
     * downloaded archive will be uncompressed into this directory.
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
     * Sets the directory where the event store should be installed. The
     * downloaded archive will be uncompressed into this directory.
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
     * Writes the process ID of the event store to a file in the target
     * directory.
     * 
     * @param pid
     *            PID to write.
     * 
     * @throws MojoExecutionException
     *             Error writing the PID to file.
     */
    protected final void writePid(final String pid)
            throws MojoExecutionException {
        try {
            FileUtils.write(getPidFile(), pid);
        } catch (final IOException ex) {
            throw new MojoExecutionException("Couldn't write the PID '" + pid
                    + "' to file: " + getPidFile(), ex);
        }
    }

    /**
     * Reads the process ID of the event store from a file in the target
     * directory.
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
            throw new MojoExecutionException(
                    "Couldn't read the PID from file: " + getPidFile(), ex);
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
            throw new MojoExecutionException("Couldn't delete the PID file: "
                    + getPidFile());
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
    protected final List<String> asList(final String str)
            throws MojoExecutionException {
        try {
            final List<String> lines = new ArrayList<String>();
            final LineNumberReader reader = new LineNumberReader(
                    new StringReader(str));
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
     * @throws MojoExecutionException if goal execution failed
     */
    protected abstract void executeGoal() throws MojoExecutionException;

}
