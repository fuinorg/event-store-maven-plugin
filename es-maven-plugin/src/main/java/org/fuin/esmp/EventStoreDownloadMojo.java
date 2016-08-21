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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.fuin.utils4j.Utils4J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads the eventstore archive and unpacks it into a defined directory.
 */
@Mojo(name = "download", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresProject = false)
public final class EventStoreDownloadMojo extends AbstractEventStoreMojo {

    private static final Logger LOG = LoggerFactory
            .getLogger(EventStoreDownloadMojo.class);

    private static final int MB = 1024 * 1024;

    @Override
    protected final void executeGoal() throws MojoExecutionException {

        // Do nothing if already in place
        if (getEventStoreDir().exists()) {
            LOG.info("Events store directory already exists: "
                    + getEventStoreDir());
        } else {
            final File archive = downloadEventStoreArchive();
            unpack(archive);
        }
    }

    /**
     * Returns the file where the result of the download is located.
     * 
     * @return File where loaded bytes are stored.
     * 
     * @throws MojoExecutionException
     *             Error initializing the variables necessary to construct the
     *             result.
     */
    public final File getDownloadFile() throws MojoExecutionException {
        final String name = FilenameUtils.getName(getDownloadUrl());
        return new File(getEventStoreDir().getParentFile(), name);
    }

    private URL createDownloadURL() throws MojoExecutionException {
        try {
            return new URL(getDownloadUrl());
        } catch (final MalformedURLException ex) {
            throw new MojoExecutionException(
                    "Failed to construct download URL for the event store", ex);
        }
    }

    private File downloadEventStoreArchive() throws MojoExecutionException {

        final URL url = createDownloadURL();
        try {
            final File file = getDownloadFile();
            if (file.exists()) {
                LOG.info("Archive already exists in target directory: " + file);
            } else {
                LOG.info("Dowloading archive: " + url);
                // Cache the file locally in the temporary directory
                final File tmpFile = new File(Utils4J.getTempDir(),
                        file.getName());
                if (!tmpFile.exists()) {
                    download(url, tmpFile);
                    LOG.info("Archive downloaded to: " + tmpFile);
                }
                FileUtils.copyFile(tmpFile, file);
                LOG.info("Archive copied from '" + tmpFile + "' to:" + file);
            }
            return file;
        } catch (final IOException ex) {
            throw new MojoExecutionException(
                    "Error downloading event store archive: " + url, ex);
        }
    }

    private void download(final URL url, final File file) throws IOException {
        final InputStream in = new CountingInputStream(url.openStream()) {

            private int called = 0;

            @Override
            protected final void afterRead(final int n) {
                super.afterRead(n);
                called++;
                if ((called % 1000) == 0) {
                    LOG.info("{} - {} bytes", file.getName(), getCount());
                }
            }
        };
        try {
            FileUtils.copyInputStreamToFile(in, file);
        } finally {
            in.close();
        }
    }

    private void unpack(final File archive) throws MojoExecutionException {

        LOG.info("Unpack event store to target directory: "
                + getEventStoreDir());

        if (archive.getName().endsWith(".zip")) {
            // All files are in the root of the ZIP file (not in a sub folder as
            // with "tar.gz")
            final File destDir = getEventStoreDir();
            unzip(archive, destDir);
        } else if (archive.getName().endsWith(".tar.gz")) {
            final File destDir = getEventStoreDir().getParentFile();
            unTarGz(archive, destDir);
        } else {
            throw new MojoExecutionException("Cannot unpack file: "
                    + archive.getName());
        }

    }

    private void unzip(final File zipFile, final File destDir)
            throws MojoExecutionException {

        try {
            final ZipFile zip = new ZipFile(zipFile);
            try {
                final Enumeration<? extends ZipEntry> enu = zip.entries();
                while (enu.hasMoreElements()) {
                    final ZipEntry entry = (ZipEntry) enu.nextElement();
                    final File file = new File(entry.getName());
                    if (file.isAbsolute()) {
                        throw new IllegalArgumentException(
                                "Only relative path entries are allowed! ["
                                        + entry.getName() + "]");
                    }
                    if (entry.isDirectory()) {
                        final File dir = new File(destDir, entry.getName());
                        createIfNecessary(dir);
                    } else {
                        final File outFile = new File(destDir, entry.getName());
                        createIfNecessary(outFile.getParentFile());
                        final InputStream in = new BufferedInputStream(
                                zip.getInputStream(entry));
                        try {
                            final OutputStream out = new BufferedOutputStream(
                                    new FileOutputStream(outFile));
                            try {
                                final byte[] buf = new byte[4096];
                                int len;
                                while ((len = in.read(buf)) > 0) {
                                    out.write(buf, 0, len);
                                }
                            } finally {
                                out.close();
                            }
                        } finally {
                            in.close();
                        }
                    }
                }
            } finally {
                zip.close();
            }

        } catch (final IOException ex) {
            throw new MojoExecutionException(
                    "Error unzipping event store archive: " + zipFile, ex);
        }
    }

    private void unTarGz(final File archive, final File destDir)
            throws MojoExecutionException {

        try {
            final TarArchiveInputStream tarIn = new TarArchiveInputStream(
                    new GzipCompressorInputStream(new BufferedInputStream(
                            new FileInputStream(archive))));
            try {
                TarArchiveEntry entry;
                while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                    LOG.info("Extracting: " + entry.getName());
                    final File file = new File(destDir, entry.getName());
                    if (entry.isDirectory()) {
                        createIfNecessary(file);
                    } else {
                        int count;
                        final byte[] data = new byte[MB];
                        final FileOutputStream fos = new FileOutputStream(file);
                        final BufferedOutputStream dest = new BufferedOutputStream(
                                fos, MB);
                        try {
                            while ((count = tarIn.read(data, 0, MB)) != -1) {
                                dest.write(data, 0, count);
                            }
                        } finally {
                            dest.close();
                        }
                        entry.getMode();
                    }
                    applyFileMode(file, new FileMode(entry.getMode()));
                }
            } finally {
                tarIn.close();
            }
        } catch (final IOException ex) {
            throw new MojoExecutionException(
                    "Error uncompressing event store archive: " + archive, ex);
        }
    }

    private static void createIfNecessary(final File dir) throws IOException {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new IOException("Error creating directory '" + dir + "'!");
        }
    }

    // CHECKSTYLE:OFF External code
    // Inspired by:
    // https://raw.githubusercontent.com/bluemel/RapidEnv/master/org.rapidbeans.rapidenv/src/org/rapidbeans/rapidenv/Unpacker.java
    private void applyFileMode(final File file, final FileMode fileMode)
            throws MojoExecutionException {

        if (OS.isFamilyUnix() || OS.isFamilyMac()) {
            final String smode = fileMode.toChmodStringFull();
            final CommandLine cmdLine = new CommandLine("chmod");
            cmdLine.addArgument(smode);
            cmdLine.addArgument(file.getAbsolutePath());
            final Executor executor = new DefaultExecutor();
            try {
                final int result = executor.execute(cmdLine);
                if (result != 0) {
                    throw new MojoExecutionException("Error # " + result
                            + " while trying to set mode \"" + smode
                            + "\" for file: " + file.getAbsolutePath());
                }
            } catch (final IOException ex) {
                throw new MojoExecutionException(
                        "Error while trying to set mode \"" + smode
                                + "\" for file: " + file.getAbsolutePath(), ex);
            }
        } else {
            file.setReadable(fileMode.isUr() || fileMode.isGr()
                    || fileMode.isOr());
            file.setWritable(fileMode.isUw() || fileMode.isGw()
                    || fileMode.isOw());
            file.setExecutable(fileMode.isUx() || fileMode.isGx()
                    || fileMode.isOx());
        }
    }
    // CHECKSTYLE:ON

}
