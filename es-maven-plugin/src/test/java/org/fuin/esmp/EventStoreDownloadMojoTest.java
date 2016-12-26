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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.fuin.utils4j.Utils4J;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link EventStoreDownloadMojo}.
 */
public class EventStoreDownloadMojoTest {

    private static final Logger LOG = LoggerFactory.getLogger(EventStoreDownloadMojoTest.class);

    // CHECKSTYLE:OFF Test

    @Test
    public void testUnTarGz() throws MojoExecutionException, IOException {

        // PREPARE
        final String name = this.getClass().getSimpleName() + "-testUnTarGz";
        final File archive = File.createTempFile(name + "-", ".tar.gz");
        final File destDir = new File(Utils4J.getTempDir(), name);
        init("example.tar.gz", archive, destDir);

        // TEST
        EventStoreDownloadMojo.unTarGz(archive, destDir);

        // VERIFY
        assertAllExists(destDir);

    }

    @Test
    public void testUnTarGzFilesOnly() throws MojoExecutionException, IOException {

        // PREPARE
        final String name = this.getClass().getSimpleName() + "-testUnTarGzFilesOnly";
        final File archive = File.createTempFile(name + "-", ".tar.gz");
        final File destDir = new File(Utils4J.getTempDir(), name);
        init("files-only.tar.gz", archive, destDir);

        // TEST
        EventStoreDownloadMojo.unTarGz(archive, destDir);

        // VERIFY
        assertAllExists(destDir);

    }
    
    @Test
    public void testUnzip() throws MojoExecutionException, IOException {

        // PREPARE
        final String name = this.getClass().getSimpleName() + "-testUnzip";
        final File archive = File.createTempFile(name + "-", ".zip");
        final File destDir = new File(Utils4J.getTempDir(), name);
        init("example.zip", archive, destDir);

        // TEST
        EventStoreDownloadMojo.unzip(archive, destDir);

        // VERIFY
        assertAllExists(destDir);

    }

    private void assertAllExists(final File destDir) {
        assertThat(destDir).exists();
        final File binDir = new File(destDir, "bin");
        assertThat(binDir).exists();
        assertThat(new File(binDir, "some.sh")).exists();
        final File confDir = new File(destDir, "conf");
        assertThat(new File(confDir, "test.xml")).exists();
        assertThat(confDir).exists();
        final File libDir = new File(destDir, "lib");
        assertThat(libDir).exists();
        assertThat(new File(libDir, "commons-lang-2.6.jar")).exists();
        assertThat(new File(libDir, "commons-io-2.5.jar")).exists();
    }

    private void init(final String srcFilename, final File archive, final File destDir) throws IOException {
        if (destDir.exists()) {
            FileUtils.deleteDirectory(destDir);
        }
        destDir.mkdir();
        final URL srcUrl = Utils4J.url("classpath:" + srcFilename);
        FileUtils.copyURLToFile(srcUrl, archive);
        LOG.info("archive: {}", archive);
        LOG.info("destDir: {}", destDir);
    }

    // CHECKSTYLE:ON

}
