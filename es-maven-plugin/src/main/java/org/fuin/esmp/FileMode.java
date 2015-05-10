// CHECKSTYLE:OFF Copied code
package org.fuin.esmp;

import org.codehaus.plexus.util.StringUtils;

/**
 * Copyright (C) 2011 Martin Bluemel 
 * Original source can be found here:
 * https://raw.githubusercontent.com/bluemel/RapidEnv/master/org.rapidbeans.rapidenv/src/org/rapidbeans/rapidenv/FileMode.java
 */
public class FileMode {

    private boolean dircetory = false;

    private boolean setUid = false;

    private boolean setGid = false;

    private boolean stickyBit = false;

    private boolean userRead = false;

    private boolean userWrite = false;

    private boolean userExecute = false;

    private boolean groupRead = false;

    private boolean groupWrite = false;

    private boolean groupExecute = false;

    private boolean otherRead = false;

    private boolean otherWrite = false;

    private boolean otherExecute = false;

    public FileMode(int mode) {

        // ORIGINAL CODE - Removed dependency to "StringHelper" class
        // final String smodeBin = StringHelper.fillUp(Integer.toBinaryString(mode), 16, '0', FillMode.left);
        final String smodeBin = StringUtils.leftPad(Integer.toBinaryString(mode), 16, "0");
        // System.out.println("@@@ smodeBin = " + smodeBin);
        if (mode >= 65536) {
            throw new AssertionError("Unexpected file mode: "
                    + Integer.toString(mode));
        }
        final String modes = smodeBin.substring(4, 7);
        this.setUid = isR(modes);
        this.setGid = isW(modes);
        this.stickyBit = isX(modes);
        final String modeu = smodeBin.substring(7, 10);
        this.userRead = isR(modeu);
        this.userWrite = isW(modeu);
        this.userExecute = isX(modeu);
        final String modeg = smodeBin.substring(10, 13);
        this.groupRead = isR(modeg);
        this.groupWrite = isW(modeg);
        this.groupExecute = isX(modeg);
        final String modeo = smodeBin.substring(13, 16);
        this.otherRead = isR(modeo);
        this.otherWrite = isW(modeo);
        this.otherExecute = isX(modeo);
    }

    // --- = 0
    // --x = 1
    // -w- = 2
    // -wx = 3
    // r-- = 4
    // r-x = 5
    // rw- = 6
    // rwx = 7

    public String toString() {
        final StringBuilder sb = new StringBuilder(9);
        sb.append(userRead ? 'r' : '-');
        sb.append(userWrite ? 'w' : '-');
        sb.append(userExecute ? 'x' : '-');
        sb.append(groupRead ? 'r' : '-');
        sb.append(groupWrite ? 'w' : '-');
        sb.append(groupExecute ? 'x' : '-');
        sb.append(otherRead ? 'r' : '-');
        sb.append(otherWrite ? 'w' : '-');
        sb.append(otherExecute ? 'x' : '-');
        return sb.toString();
    }

    public String toChmodStringBasic() {
        final StringBuilder sb = new StringBuilder(3);
        sb.append(Integer.toString(toOctal(this.userRead, this.userWrite,
                this.userExecute)));
        sb.append(Integer.toString(toOctal(this.groupRead, this.groupWrite,
                this.groupExecute)));
        sb.append(Integer.toString(toOctal(this.otherRead, this.otherWrite,
                this.otherExecute)));
        return sb.toString();
    }

    public String toChmodStringFull() {
        final StringBuilder sb = new StringBuilder(4);
        sb.append(Integer.toString(toOctal(this.setUid, this.setGid,
                this.stickyBit)));
        sb.append(Integer.toString(toOctal(this.userRead, this.userWrite,
                this.userExecute)));
        sb.append(Integer.toString(toOctal(this.groupRead, this.groupWrite,
                this.groupExecute)));
        sb.append(Integer.toString(toOctal(this.otherRead, this.otherWrite,
                this.otherExecute)));
        return sb.toString();
    }

    private int toOctal(final boolean r, final boolean w, final boolean x) {
        int octal = 0;
        if (r) {
            octal += 4;
        }
        if (w) {
            octal += 2;
        }
        if (x) {
            octal += 1;
        }
        return octal;
    }

    private boolean isR(final String modePart) {
        return (modePart.substring(0, 1).equals("1"));
    }

    private boolean isW(final String modePart) {
        return (modePart.substring(1, 2).equals("1"));
    }

    private boolean isX(final String modePart) {
        return (modePart.substring(2, 3).equals("1"));
    }

    /**
     * @return the dircetory
     */
    public boolean isDircetory() {
        return dircetory;
    }

    /**
     * @return the ur
     */
    public boolean isUr() {
        return userRead;
    }

    /**
     * @return the uw
     */
    public boolean isUw() {
        return userWrite;
    }

    /**
     * @return the ux
     */
    public boolean isUx() {
        return userExecute;
    }

    /**
     * @return the gr
     */
    public boolean isGr() {
        return groupRead;
    }

    /**
     * @return the gw
     */
    public boolean isGw() {
        return groupWrite;
    }

    /**
     * @return the gx
     */
    public boolean isGx() {
        return groupExecute;
    }

    /**
     * @return the or
     */
    public boolean isOr() {
        return otherRead;
    }

    /**
     * @return the ow
     */
    public boolean isOw() {
        return otherWrite;
    }

    /**
     * @return the ox
     */
    public boolean isOx() {
        return otherExecute;
    }

}
// CHECKSTYLE:ON
