package com.sxtanna.mc.util;

import org.jetbrains.annotations.NotNull;

import kong.unirest.json.JSONObject;

import java.util.Optional;

import static kong.unirest.Unirest.get;

public enum Rest
{
    ;


    public static Optional<JSONObject> json(@NotNull final String link)
    {
        try
        {
            return Optional.ofNullable(get(link).asJson().getBody().getObject());
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<JSONObject> json(@NotNull final String base, @NotNull final String format, @NotNull final Object... args)
    {
        return json(String.format("%s/%s", base, String.format(format, args)));
    }

}
