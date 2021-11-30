package com.sxtanna.mc.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public final class Vers implements Comparable<Vers>
{

    public static final Vers V1_8_8  = new Vers("V1_8_8");
    public static final Vers V1_9_4  = new Vers("V1_9_4");
    public static final Vers V1_10_2 = new Vers("V1_10_2");
    public static final Vers V1_11_2 = new Vers("V1_11_2");
    public static final Vers V1_12_2 = new Vers("V1_12_2");
    public static final Vers V1_13_2 = new Vers("V1_13_2");
    public static final Vers V1_14_4 = new Vers("V1_14_4");
    public static final Vers V1_15_2 = new Vers("V1_15_2");
    public static final Vers V1_16_5 = new Vers("V1_16_5");
    public static final Vers V1_17_1 = new Vers("V1_17_1");
    public static final Vers V1_18   = new Vers("V1_18");


    private static final Vers[] values = {V1_8_8, V1_9_4, V1_10_2, V1_11_2, V1_12_2, V1_13_2, V1_14_4, V1_15_2, V1_16_5, V1_17_1, V1_18};


    @NotNull
    private final String name;

    @Contract(pure = true)
    public Vers(@NotNull final String name)
    {
        this.name = name;
    }


    @Contract(pure = true)
    public @NotNull String name()
    {
        return this.name;
    }


    public @NotNull String mcVersion()
    {
        return this.name().substring(1).replace('_', '.');
    }

    public @NotNull String mcVersionGroup()
    {
        final var version = mcVersion();

        if (version.indexOf('.') == version.lastIndexOf('.'))
        {
            return version;
        }

        return version.substring(0, version.lastIndexOf('.'));
    }


    @Contract(pure = true)
    public @NotNull String majorVersion()
    {
        return "1"; // optimization, for now
    }

    @Contract(pure = true)
    public @NotNull String minorVersion()
    {
        final var version = mcVersion();

        final var mU = version.indexOf('.') + 1;
        final var pU = version.indexOf('.', mU);

        return pU == -1 ? version.substring(mU) : version.substring(mU, pU);
    }

    @Contract(pure = true)
    public @NotNull String patchVersion()
    {
        final var version = mcVersion();

        final var mU = version.indexOf('.') + 1;
        final var pU = version.indexOf('.', mU);

        return pU == -1 ? "" : version.substring(pU + 1);
    }


    @Contract(pure = true)
    public @NotNull Integer majorVersionNumber()
    {
        final var major = majorVersion();

        return major.isBlank() ? 0 : Integer.parseInt(major);
    }

    @Contract(pure = true)
    public @NotNull Integer minorVersionNumber()
    {
        final var minor = minorVersion();

        return minor.isBlank() ? 0 : Integer.parseInt(minor);
    }

    @Contract(pure = true)
    public @NotNull Integer patchVersionNumber()
    {
        final var patch = patchVersion();

        return patch.isBlank() ? 0 : Integer.parseInt(patch);
    }


    @Override
    public int compareTo(@NotNull final Vers that)
    {
        final var major = this.majorVersionNumber().compareTo(that.majorVersionNumber());
        if (major != 0)
        {
            return major;
        }

        final var minor = this.minorVersionNumber().compareTo(that.minorVersionNumber());
        if (minor != 0)
        {
            return minor;
        }

        return this.patchVersionNumber().compareTo(that.patchVersionNumber());
    }

    @Override
    public @NotNull String toString()
    {
        return mcVersionGroup();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Vers))
        {
            return false;
        }

        final Vers that = (Vers) o;
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.name);
    }


    public static @NotNull Vers latest()
    {
        return Vers.values[Vers.values.length - 1];
    }

    public static @NotNull Vers resolveVers(@NotNull final String text)
    {
        final Vers vers;

        if ("latest".equals(text.toLowerCase(Locale.ROOT)))
        {
            vers = latest();
        }
        else if (text.toLowerCase(Locale.ROOT).startsWith("c"))
        {
            vers = new Vers(text.substring(1));
        }
        else
        {
            Vers match = null;

            for (final var value : Vers.values)
            {
                if (!value.name().equalsIgnoreCase(text) && !value.mcVersion().equalsIgnoreCase(text) && !value.mcVersionGroup().equalsIgnoreCase(text))
                {
                    continue;
                }

                match = value;
                break;
            }

            if (match == null)
            {
                throw new IllegalArgumentException(String.format("invalid version: '%s', must be of %s", text, Arrays.toString(Vers.values)));
            }

            vers = match;
        }

        return vers;
    }

}
