package com.github.azazar.mirrorftp;

import java.io.File;


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
