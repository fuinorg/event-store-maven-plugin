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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test for {@link DownloadOS}.
 */
// CHECKSTYLE:OFF Test
public class DownloadOSTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(DownloadOS.class).suppress(Warning.NULL_FIELDS, Warning.ALL_FIELDS_SHOULD_BE_USED)
                .verify();
    }

    @Test
    public void testConstruction() throws MalformedURLException {

        // PREPARE
        final String os = "ubuntu-14.04";
        final String version = "3.8.1";
        final List<DownloadVersion> versions = new ArrayList<>();
        final DownloadVersion v381 = new DownloadVersion("3.8.1", "http://www.fuin.org/dummy/v3.8.1.zip");
        final DownloadVersion v380 = new DownloadVersion("3.8.0", "http://www.fuin.org/dummy/v3.8.0.zip");
        versions.add(v381);
        versions.add(v380);

        // TEST
        final DownloadOS testee = new DownloadOS(os, version, versions);

        // VERIFY
        assertThat(testee.getOS()).isEqualTo(os);
        assertThat(testee.getCurrentVersion()).isEqualTo(version);
        assertThat(testee.getVersions()).containsExactlyInAnyOrder(v381, v380);

    }

}
// CHECKSTYLE:ON
