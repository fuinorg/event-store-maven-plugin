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

import java.util.List;

/**
 * Operation system version of the Event Store that is available for download.
 * Equals and hash code are based on the OS name.
 */
public final class DownloadOS implements Comparable<DownloadOS> {

    private final String os;

    private final String currentVersion;

    private final List<DownloadVersion> versions;

    /**
     * Constructor with mandatory data.
     * 
     * @param os
     *            OS (like "ubuntu-14.04", "osx-10.10" or "win").
     */
    public DownloadOS(final String os) {
        this(os, null, null);
    }

    /**
     * Constructor with all data.
     * 
     * @param os
     *            OS (like "ubuntu-14.04", "osx-10.10" or "win").
     * @param currentVersion
     *            Latest version.
     * @param versions
     *            List of all versions.
     */
    public DownloadOS(final String os, final String currentVersion, final List<DownloadVersion> versions) {
        super();
        if (os == null) {
            throw new IllegalArgumentException("os == null");
        }
        this.os = os;
        this.currentVersion = currentVersion;
        this.versions = versions;
    }

    /**
     * Returns the OS name.
     * 
     * @return OS (like "ubuntu-14.04", "osx-10.10" or "win") - Never
     *         <code>null</code>.
     */
    public final String getOS() {
        return os;
    }

    /**
     * Returns the latest version.
     * 
     * @return Version or <code>null</code>.
     */
    public final String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Returns the list of available versions.
     * 
     * @return All downloadable versions - or <code>null</code>.
     */
    public final List<DownloadVersion> getVersions() {
        return versions;
    }

    /**
     * Returns the latest version.
     * 
     * @return Latest version - or <code>null</code> if there are no versions.
     */
    public final DownloadVersion getLatestVersion() {
        return findVersion(currentVersion);
    }
    
    /**
     * Returns the version with a given number.
     * 
     * @param number
     *            Version number to find - Cannot be <code>null</code>.
     * 
     * @return Found instance or <code>null</code> if no OS with that name was
     *         found.
     */
    public DownloadVersion findVersion(final String number) {
        if (number == null) {
            throw new IllegalArgumentException("number == null");
        }
        if (versions == null) {
            return null;
        }
        final int idx = versions.indexOf(new DownloadVersion(number, "-"));
        if (idx < 0) {
            return null;
        }
        return versions.get(idx);
    }

    @Override
    public final int hashCode() {
        return os.hashCode();
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
        final DownloadOS other = (DownloadOS) obj;
        return os.equals(other.os);
    }

    @Override
    public final int compareTo(DownloadOS other) {
        return os.compareTo(other.os);
    }

    @Override
    public final String toString() {
        return os;
    }

}
