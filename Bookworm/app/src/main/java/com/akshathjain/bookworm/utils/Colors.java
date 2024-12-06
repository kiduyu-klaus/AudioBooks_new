package com.akshathjain.bookworm.utils;

import android.graphics.Bitmap;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.request.target.BitmapImageViewTarget;

/**
 * Created by Akshath on 10/19/2017.
 */

public class Colors {
    public static void createPaletteAsync(Bitmap b, Palette.PaletteAsyncListener finished){
        Palette.generateAsync(b, finished);
    }
}
