package me.lauriichan.minecraft.pluginbase.util;

import java.awt.Color;

public record LinearColor(double red, double green, double blue) {

    private static float CONST_1_OVER_2_4 = 1f / 2.4f;

    public static int fromLinear(double value) {
        return (int) Math.floor((value <= 0.0031308d ? value * 12.92d : 1.055d * Math.pow(value, CONST_1_OVER_2_4) - 0.055d) * 255d);
    }

    public static double toLinear(int value) {
        double val = value / 255d;
        return val <= 0.04045d ? val / 12.92d : Math.pow((val + 0.055d) / 1.055d, 2.4d);
    }

    public LinearColor(Color color) {
        this(toLinear(color.getRed()), toLinear(color.getGreen()), toLinear(color.getBlue()));
    }

    public LinearColor calcInterpolationDifference(Color other) {
        return new LinearColor(toLinear(other.getRed()) - red, toLinear(other.getGreen()) - green, toLinear(other.getBlue()) - blue);
    }

    public Color toInterpolatedColor(LinearColor start, double percentage) {
        return new Color(fromLinear(red * percentage + start.red), fromLinear(green * percentage + start.green),
            fromLinear(blue * percentage + start.blue));
    }

}
