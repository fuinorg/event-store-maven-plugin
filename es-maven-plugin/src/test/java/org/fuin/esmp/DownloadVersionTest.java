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

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test for {@link DownloadVersion}.
 */
// CHECKSTYLE:OFF Test
public class DownloadVersionTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(DownloadVersion.class).suppress(Warning.NULL_FIELDS, Warning.ALL_FIELDS_SHOULD_BE_USED)
                .verify();
    }

    @Test
    public void testConstruction() throws MalformedURLException {

        // PREPARE
        final String version = "1.2.3";
        final String url = "http://www.fuin.org/dummy.json";

        // TEST
        final DownloadVersion testee = new DownloadVersion(version, url);

        // VERIFY
        assertThat(testee.getVersion()).isEqualTo(version);
        assertThat(testee.getUrl()).isEqualTo(url);
        assertThat(testee.getURL()).isEqualTo(new URL(url));

    }

}
// CHECKSTYLE:ON
