package me.lauriichan.minecraft.pluginbase.message.component;

import java.awt.Color;
import java.util.Objects;

public final class SimpleColor {

    public static SimpleColor sRGB(Color awtColor) {
        SimpleColor color = new SimpleColor(ColorType.SRGB);
        color.red = awtColor.getRed() / 255d;
        color.green = awtColor.getGreen() / 255d;
        color.blue = awtColor.getBlue() / 255d;
        return color;
    }

    public static SimpleColor sRGB(double red, double green, double blue) {
        SimpleColor color = new SimpleColor(ColorType.SRGB);
        color.red = red;
        color.green = green;
        color.blue = blue;
        return color;
    }

    public static SimpleColor lRGB(double red, double green, double blue) {
        SimpleColor color = new SimpleColor(ColorType.LRGB);
        color.red = red;
        color.green = green;
        color.blue = blue;
        return color;
    }

    public static SimpleColor okLab(double l, double a, double b) {
        SimpleColor color = new SimpleColor(ColorType.OKLAB);
        color.red = l;
        color.green = a;
        color.blue = b;
        return color;
    }

    public static enum ColorType {
        SRGB,
        LRGB,
        OKLAB;
    }

    private final ColorType type;
    // a = red / l
    // b = green / m
    // c = blue / s
    private volatile double red = 0d, green = 0d, blue = 0d;

    public SimpleColor(final ColorType type) {
        this.type = Objects.requireNonNull(type, "ColorType can't be null");
    }

    public int awtRed() {
        return toIntRGB(red);
    }

    public double red() {
        return red;
    }

    public void red(double red) {
        this.red = red;
    }

    public int awtGreen() {
        return toIntRGB(green);
    }

    public double green() {
        return green;
    }

    public void green(double green) {
        this.green = green;
    }

    public int awtBlue() {
        return toIntRGB(blue);
    }

    public double blue() {
        return blue;
    }

    public void blue(double blue) {
        this.blue = blue;
    }

    public SimpleColor subtract(double value) {
        this.red -= value;
        this.green -= value;
        this.blue -= value;
        return this;
    }

    public SimpleColor subtract(SimpleColor color) {
        if (type != color.type) {
            return subtract(color.as(type));
        }
        this.red -= color.red;
        this.green -= color.green;
        this.blue -= color.blue;
        return this;
    }

    public SimpleColor subtractTo(SimpleColor color) {
        if (type != color.type) {
            return subtractTo(color.as(type));
        }
        color.red -= red;
        color.green -= green;
        color.blue -= blue;
        return this;
    }

    public SimpleColor add(double value) {
        this.red += value;
        this.green += value;
        this.blue += value;
        return this;
    }

    public SimpleColor add(SimpleColor color) {
        if (type != color.type) {
            return add(color.as(type));
        }
        this.red += color.red;
        this.green += color.green;
        this.blue += color.blue;
        return this;
    }

    public SimpleColor addTo(SimpleColor color) {
        if (type != color.type) {
            return addTo(color.as(type));
        }
        color.red += red;
        color.green += green;
        color.blue += blue;
        return this;
    }

    public SimpleColor multiply(double value) {
        this.red *= value;
        this.green *= value;
        this.blue *= value;
        return this;
    }

    public SimpleColor multiply(SimpleColor color) {
        if (type != color.type) {
            return multiply(color.as(type));
        }
        this.red *= color.red;
        this.green *= color.green;
        this.blue *= color.blue;
        return this;
    }

    public SimpleColor multiplyTo(SimpleColor color) {
        if (type != color.type) {
            return multiplyTo(color.as(type));
        }
        color.red *= red;
        color.green *= green;
        color.blue *= blue;
        return this;
    }

    public SimpleColor as(ColorType type) {
        if (type == ColorType.SRGB) {
            return toSRGB();
        }
        if (type == ColorType.LRGB) {
            return toLRGB();
        }
        if (type == ColorType.OKLAB) {
            return toOkLab();
        }
        throw new UnsupportedOperationException("Unsupported color type: " + type);
    }

    public SimpleColor duplicate() {
        SimpleColor color = new SimpleColor(type);
        color.red = red;
        color.green = green;
        color.blue = blue;
        return color;
    }

    public SimpleColor toSRGB() {
        if (type == ColorType.SRGB) {
            return this;
        }
        if (type == ColorType.LRGB) {
            SimpleColor color = new SimpleColor(ColorType.SRGB);
            color.red = lrgb2srgb(red);
            color.green = lrgb2srgb(green);
            color.blue = lrgb2srgb(blue);
            return color;
        }
        if (type == ColorType.OKLAB) {
            SimpleColor color = new SimpleColor(ColorType.SRGB);
            color.red = red;
            color.green = green;
            color.blue = blue;
            oklab2lrgb(color);
            color.red = lrgb2srgb(color.red);
            color.green = lrgb2srgb(color.green);
            color.blue = lrgb2srgb(color.blue);
            return color;
        }
        throw new UnsupportedOperationException("Unsupported color type: " + type);
    }

    public SimpleColor toLRGB() {
        if (type == ColorType.LRGB) {
            return this;
        }
        if (type == ColorType.SRGB) {
            SimpleColor color = new SimpleColor(ColorType.LRGB);
            color.red = srgb2lrgb(red);
            color.green = srgb2lrgb(green);
            color.blue = srgb2lrgb(blue);
            return color;
        }
        if (type == ColorType.OKLAB) {
            SimpleColor color = new SimpleColor(ColorType.LRGB);
            color.red = red;
            color.green = green;
            color.blue = blue;
            oklab2lrgb(color);
            return color;
        }
        throw new UnsupportedOperationException("Unsupported color type: " + type);
    }

    public SimpleColor toOkLab() {
        if (type == ColorType.OKLAB) {
            return this;
        }
        if (type == ColorType.LRGB) {
            SimpleColor color = new SimpleColor(ColorType.OKLAB);
            color.red = red;
            color.green = green;
            color.blue = blue;
            lrgb2oklab(color);
            return color;
        }
        if (type == ColorType.SRGB) {
            SimpleColor color = new SimpleColor(ColorType.OKLAB);
            color.red = srgb2lrgb(red);
            color.green = srgb2lrgb(green);
            color.blue = srgb2lrgb(blue);
            lrgb2oklab(color);
            return color;
        }
        throw new UnsupportedOperationException("Unsupported color type: " + type);
    }

    public Color asAwtColor() {
        SimpleColor color = toSRGB();
        return new Color(color.awtRed(), color.awtGreen(), color.awtBlue(), 255);
    }

    /*
     * Conversion helper
     */

    private static double CONST_1_OVER_2_4 = 1d / 2.4d;

    private static double lrgb2srgb(double value) {
        return value < 0.0031308d ? value * 12.92d : 1.055d * Math.pow(value, CONST_1_OVER_2_4) - 0.055d;
    }

    private static double srgb2lrgb(double value) {
        return value < 0.04045d ? value / 12.92d : Math.pow((value + 0.055d) / 1.055d, 2.4d);
    }

    private static void lrgb2oklab(SimpleColor color) {
        double l = Math.cbrt(color.red * 0.4122214708d + color.green * 0.5363325363d + color.blue * 0.0514459929d);
        double m = Math.cbrt(color.red * 0.2119034982d + color.green * 0.6806995451d + color.blue * 0.1073969566d);
        double s = Math.cbrt(color.red * 0.0883024619d + color.green * 0.2817188376d + color.blue * 0.6299787005d);
        color.red = l * 0.2104542553d + m * 0.7936177850d - s * 0.0040720468d;
        color.green = l * 1.9779984951d - m * 2.4285922050d + s * 0.4505937099d;
        color.blue = l * 0.0259040371d + m * 0.7827717662d - s * 0.8086757660d;
    }

    private static void oklab2lrgb(SimpleColor color) {
        double l = cube(color.red + color.green * 0.3963377774d + color.blue * 0.2158037573d);
        double m = cube(color.red - color.green * 0.1055613458d - color.blue * 0.0638541728d);
        double s = cube(color.red - color.green * 0.0894841775d - color.blue * 1.2914855480d);
        color.red = l * 4.0767416621d - m * 3.3077115913d + s * 0.2309699292d;
        color.green = l * -1.2684380046d + m * 2.6097574011d - s * 0.3413193965d;
        color.blue = l * -0.0041960863d - m * 0.7034186147d + s * 1.7076147010d;
    }

    private static double cube(double value) {
        return value * value * value;
    }

    private static int toIntRGB(double value) {
        return Math.min(Math.max((int) Math.round(value * 255), 0), 255);
    }

}
