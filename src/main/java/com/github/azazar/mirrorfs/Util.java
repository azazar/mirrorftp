/*
 * PROPRIETARY/CONFIDENTIAL
 */
package com.github.azazar.mirrorfs;

import java.io.File;

/**
 *
 * @author Mikhail Yevchenko <m.ṥῥẚɱ.ѓѐḿởύḙ@uo1.net>
 */
class Util {

    static File[] arrayAdd(File[] array, File element) {
        if (array == null || array.length == 0) {
            return new File[] { element };
        }

        File[] newArray = new File[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }

}
