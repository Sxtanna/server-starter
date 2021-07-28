package com.sxtanna.mc.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Size
{

    @NotNull
    private static final Pattern SIZE_PATTERN = Pattern.compile("(?<min>\\d+):(?<max>\\d+)");


    public static @NotNull Size resolveSize(@NotNull final String text)
    {
        final Size size;

        switch (text.toLowerCase(Locale.ROOT))
        {
            case "small":
                size = Size.SMALL;
                break;
            case "large":
                size = Size.LARGE;
                break;
            default:
                size = resolveSizeFromPattern(text);
                break;
        }

        return size;
    }

    @Contract("_ -> new")
    public static @NotNull Size resolveSizeFromPattern(@NotNull final String text)
    {
        try
        {
            final var match = SIZE_PATTERN.matcher(text);
            if (!match.matches())
            {
                throw new IllegalArgumentException("invalid size");
            }

            final var min = Integer.parseInt(match.group("min"));
            final var max = Integer.parseInt(match.group("max"));

            if (min > max)
            {
                throw new IllegalArgumentException("min must be less than max");
            }

            return new Size(min, max);
        }
        catch (final IllegalArgumentException ex)
        {
            throw new IllegalArgumentException(String.format("invalid size: '%s', must be of [small, large, 'min:max']", text), ex);
        }
    }


    @NotNull
    public static final Size SMALL = new Size(2048, 2048);
    @NotNull
    public static final Size LARGE = new Size(8192, 8192);


    private final int minMemory;
    private final int maxMemory;


    @Contract(pure = true)
    public Size(final int minMemory, final int maxMemory)
    {
        this.minMemory = minMemory;
        this.maxMemory = maxMemory;
    }


    @Contract(pure = true)
    public int getMinMemory()
    {
        return this.minMemory;
    }

    @Contract(pure = true)
    public int getMaxMemory()
    {
        return this.maxMemory;
    }


    @Override
    @Contract(pure = true)
    public @NotNull String toString()
    {
        return String.format("Size[%d:%d]", minMemory, maxMemory);
    }

}
