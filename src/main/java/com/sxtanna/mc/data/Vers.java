package com.sxtanna.mc.data;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

public enum Vers
{

    V1_8_8,
    V1_9_4,
    V1_10_2,
    V1_11_2,
    V1_12_2,
    V1_13_2,
    V1_14_4,
    V1_15_2,
    V1_16_5,
    V1_17_1;


    public @NotNull final String mcVersion()
    {
        return this.name().substring(1).replace('_', '.');
    }

    public @NotNull final String mcVersionGroup()
    {
        final var version = mcVersion();

        if (version.indexOf('.') == version.lastIndexOf('.'))
        {
            return version;
        }

        return version.substring(0, version.lastIndexOf('.'));
    }


    @Override
    public @NotNull final String toString()
    {
        return mcVersionGroup();
    }


    public static @NotNull Vers latest()
    {
        return values()[values().length - 1];
    }

    public static @NotNull Vers resolveVers(@NotNull final String text)
    {
        final Vers vers;

        if ("latest".equals(text.toLowerCase(Locale.ROOT)))
        {
            vers = Vers.values()[Vers.values().length - 1];
        }
        else
        {
            Vers match = null;

            for (final var value : Vers.values())
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
                throw new IllegalArgumentException(String.format("invalid version: '%s', must be of %s", text, Arrays.toString(Vers.values())));
            }

            vers = match;
        }

        return vers;
    }

}
