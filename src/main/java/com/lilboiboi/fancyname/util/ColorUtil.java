package com.lilboiboi.fancyname.util;

import java.awt.*;

public class ColorUtil {
    public static Color interpolate(Color a, Color b, float t) {
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bVal = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bVal);
    }
}
