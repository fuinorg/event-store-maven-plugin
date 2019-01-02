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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Version of the Event Store that is available for download. Equals and hash code are based on the version name.
 */
public final class DownloadVersion implements Comparable<DownloadVersion> {

    private final String name;

    private final List<DownloadOSFamily> osFamilies;

    private boolean sealed;

    /**
     * Constructor with all data.
     * 
     * @param name
     *            Version (like "3.1.0", "4.1.1-hotfix1").
     */
    public DownloadVersion(final String name) {
        this(name, new ArrayList<>());
    }

    /**
     * Constructor with all data.
     * 
     * @param name
     *            Version (like "3.1.0", "4.1.1-hotfix1").
     * @param osFamilies
     *            List of OS families like "Windows", "Mac" or "Linux".
     */
    public DownloadVersion(final String name, final List<DownloadOSFamily> osFamilies) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("name == null");
        }
        if (osFamilies == null) {
            throw new IllegalArgumentException("osFamilies == null");
        }
        this.name = name;
        this.osFamilies = osFamilies;
    }

    /**
     * Returns the version name.
     * 
     * @return Version (like "3.1.0", "4.1.1-hotfix1").
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the list of OS families.
     * 
     * @return Immutable list of OS families like "Windows", "Mac" or "Linux".
     */
    public final List<DownloadOSFamily> getOSFamilies() {
        return Collections.unmodifiableList(osFamilies);
    }

    /**
     * Adds a new family to the list.
     * 
     * @param family
     *            Family to add.
     */
    final void addOSFamily(final DownloadOSFamily family) {
        if (sealed) {
            throw new IllegalStateException("The instance is sealed");
        }
        osFamilies.add(family);
    }

    /**
     * The instance is sealed and no changes are allowed any more.
     */
    final void seal() {
        if (sealed) {
            return;
        }
        sealed = true;
        for (final DownloadOSFamily family : osFamilies) {
            family.seal();
        }
    }

    /**
     * Returns the OS family with a given name.
     * 
     * @param name
     *            Family to find.
     * 
     * @return Found instance or <code>null</code> if no family with that name was found.
     */
    public DownloadOSFamily findFamily(final String name) {
        final int idx = osFamilies.indexOf(new DownloadOSFamily(name));
        if (idx < 0) {
            return null;
        }
        return osFamilies.get(idx);
    }

    /**
     * Determines if this version is a release.
     * 
     * @return {@code true} if the version contains no "-rc" string.
     */
    public boolean isRelease() {
        return !name.contains("-rc");
    }

    
    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DownloadVersion other = (DownloadVersion) obj;
        return name.equals(other.name);
    }

    @Override
    public final String toString() {
        return name;
    }

    @Override
    public final int compareTo(final DownloadVersion other) {
        return name.compareTo(other.name);
    }

}
