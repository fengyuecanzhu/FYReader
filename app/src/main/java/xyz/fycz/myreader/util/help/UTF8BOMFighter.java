/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.util.help;

public class UTF8BOMFighter {
    private static final byte[] UTF8_BOM_BYTES = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private UTF8BOMFighter() {
    }

    static public String removeUTF8BOM(String xmlText) {
        byte[] bytes = xmlText.getBytes();
        boolean containsBOM = bytes.length > 3
                && bytes[0] == UTF8_BOM_BYTES[0]
                && bytes[1] == UTF8_BOM_BYTES[1]
                && bytes[2] == UTF8_BOM_BYTES[2];
        if (containsBOM) {
            xmlText = new String(bytes, 3, bytes.length - 3);
        }
        return xmlText;
    }

    static public byte[] removeUTF8BOM(byte[] bytes) {
        boolean containsBOM = bytes.length > 3
                && bytes[0] == UTF8_BOM_BYTES[0]
                && bytes[1] == UTF8_BOM_BYTES[1]
                && bytes[2] == UTF8_BOM_BYTES[2];
        if (containsBOM) {
            byte[] copy = new byte[bytes.length - 3];
            System.arraycopy(bytes, 3, copy, 0, bytes.length - 3);
            return copy;
        }
        return bytes;
    }
}
