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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

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
                .withPrefabValues(DownloadOSFamily.class, new DownloadOSFamily("Linux"), new DownloadOSFamily("Windows")).verify();
    }

    @Test
    public void testConstruction() {

        // PREPARE
        final String version = "1.2.3";
        final List<DownloadOSFamily> osFamilies = new ArrayList<>();
        final DownloadOSFamily linux = new DownloadOSFamily("Linux");
        osFamilies.add(linux);

        // TEST
        DownloadVersion testee = new DownloadVersion(version, osFamilies);

        // VERIFY
        assertThat(testee.getName()).isEqualTo(version);
        assertThat(testee.getOSFamilies()).containsOnly(linux);

        // TEST
        testee = new DownloadVersion(version);

        // VERIFY
        assertThat(testee.getName()).isEqualTo(version);
        assertThat(testee.getOSFamilies()).isEmpty();

    }

    @Test
    public void testAdd() {

        // PREPARE
        final String version = "1.2.3";
        final List<DownloadOSFamily> osFamilies = new ArrayList<>();
        DownloadVersion testee = new DownloadVersion(version, osFamilies);

        final DownloadOSFamily linux = new DownloadOSFamily("Linux");
        final DownloadOSFamily windows = new DownloadOSFamily("Windows");

        // TEST
        testee.addOSFamily(linux);
        testee.addOSFamily(windows);

        // VERIFY
        assertThat(testee.getName()).isEqualTo(version);
        assertThat(testee.getOSFamilies()).containsExactlyInAnyOrder(linux, windows);

    }

    @Test
    public void testSeal() {

        // PREPARE
        final String version = "1.2.3";
        DownloadVersion testee = new DownloadVersion(version);

        final DownloadOSFamily linux = new DownloadOSFamily("Linux");
        final DownloadOSFamily windows = new DownloadOSFamily("Windows");
        testee.addOSFamily(linux);

        // TEST & VERIFY
        testee.seal();
        try {
            testee.addOSFamily(windows);
            fail();
        } catch (final IllegalStateException ex) {
            assertThat(ex.getMessage()).isEqualTo("The instance is sealed");
        }

    }

    @Test
    public void testFind() {

        // PREPARE
        final String version = "1.2.3";
        DownloadVersion testee = new DownloadVersion(version);

        final DownloadOSFamily linux = new DownloadOSFamily("Linux");
        final DownloadOSFamily windows = new DownloadOSFamily("Windows");
        testee.addOSFamily(linux);
        testee.addOSFamily(windows);
        
        // TEST
        final DownloadOSFamily family = testee.findFamily("Linux");

        // VERIFY
        assertThat(family).isSameAs(linux);
        
    }

    @Test
    public void testIsRelease() {

        assertThat(new DownloadVersion("5.0.0-rc1").isRelease()).isFalse();
        assertThat(new DownloadVersion("4.1.1-hotfix1").isRelease()).isTrue();
        assertThat(new DownloadVersion("4.1.1").isRelease()).isTrue();
        assertThat(new DownloadVersion("3.0.0-rc9").isRelease()).isFalse();
        assertThat(new DownloadVersion("1.0.0").isRelease()).isTrue();
        
    }
    
}
// CHECKSTYLE:ON
