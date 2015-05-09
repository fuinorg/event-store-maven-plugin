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
import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads the eventstore archive and unpacks it into a defined directory.
 * 
 * @goal download
 * @phase pre-integration-test
 * @requiresProject false
 */
public final class EventStoreDownloadMojo extends AbstractEventStoreMojo {

    private static final int MB = 1024 * 1024;

    private static final Logger LOG = LoggerFactory
            .getLogger(EventStoreDownloadMojo.class);

    private static final int BUFFER = MB;

    @Override
    protected final void executeGoal() throws MojoExecutionException {
        
        // Do nothing if already in place
        if (getEventStoreDir().exists()) {
            LOG.info("Events store directory already exists: "  + getEventStoreDir());
        } else {
            final File archive = downloadEventStoreArchive();
            unpack(archive);
        }
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
            final String name = FilenameUtils.getName(url.getPath());
            final File file = new File(getEventStoreDir().getParentFile(), name);
            if (file.exists()) {
                LOG.info("Archive already exists in target directory: " + file);
            } else {
                LOG.info("Dowloading archive: " + url);
                final InputStream in = new CountingInputStream(url.openStream()) {

                    private int called = 0;

                    @Override
                    protected final void afterRead(final int n) {
                        super.afterRead(n);
                        called++;
                        if ((called % 1000) == 0) {
                            LOG.info("{} - {} bytes", name, getCount());
                        }
                    }
                };
                try {
                    FileUtils.copyInputStreamToFile(in, file);
                } finally {
                    in.close();
                }
                LOG.info("Archive saved to: " + file);
            }
            return file;
        } catch (final IOException ex) {
            throw new MojoExecutionException(
                    "Error downloading event store archive: " + url, ex);
        }
    }

    private void unpack(final File archive)
            throws MojoExecutionException {

        final File destDir = getEventStoreDir().getParentFile();
        LOG.info("Unzip event store to target directory: " + getEventStoreDir());
        
        if (archive.getName().endsWith(".zip")) {
            unzip(archive, destDir);
        } else if (archive.getName().endsWith(".tar.gz")) {
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
                    if (entry.isDirectory()) {
                        final File dir = new File(destDir, entry.getName());
                        createIfNecessary(dir);
                    } else {
                        int count;
                        final byte[] data = new byte[BUFFER];
                        final FileOutputStream fos = new FileOutputStream(
                                new File(destDir, entry.getName()));
                        final BufferedOutputStream dest = new BufferedOutputStream(
                                fos, BUFFER);
                        try {
                            while ((count = tarIn.read(data, 0, BUFFER)) != -1) {
                                dest.write(data, 0, count);
                            }
                        } finally {
                            dest.close();
                        }
                    }
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

}
