package com.sxtanna.mc.util;

import org.jetbrains.annotations.NotNull;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.util.Optional;
import java.util.function.BiFunction;

public enum Json
{
    ;


    public static Optional<JSONObject> getHash(@NotNull final JSONObject root, @NotNull final String path)
    {
        var some = root;

        try
        {
            for (final String it : path.split(":"))
            {
                if (!some.has(it))
                {
                    return Optional.empty();
                }

                some = some.getJSONObject(it);
            }
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            return Optional.empty();
        }

        return Optional.of(some);
    }


    public static <T> Optional<T> getSomeAtPath(@NotNull final JSONObject root, @NotNull final String path, @NotNull final BiFunction<JSONObject, String, T> function)
    {
        var some = Optional.of(root);

        var node = path.lastIndexOf(':');
        var name = node == -1 ? path : path.substring(node + 1);

        if (node != -1)
        {
            some = getHash(root, path.substring(0, node));
        }

        try
        {
            return some.map(it -> !it.has(name) ? null : function.apply(it, name));
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            return Optional.empty();
        }
    }


    public static <T> Optional<T> getLastInList(@NotNull final JSONArray list, @NotNull final BiFunction<JSONArray, Integer, T> function)
    {
        final var index = list.isEmpty() ? -1 : list.length() - 1;
        if (index == -1)
        {
            return Optional.empty();
        }

        try
        {
            return Optional.ofNullable(list.isNull(index) ? null : function.apply(list, index));
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            return Optional.empty();
        }
    }


    public static Optional<String> getTextAtPath(@NotNull final JSONObject root, @NotNull final String path)
    {
        return getSomeAtPath(root, path, JSONObject::getString);
    }

    public static Optional<JSONArray> getListAtPath(@NotNull final JSONObject root, @NotNull final String path)
    {
        return getSomeAtPath(root, path, JSONObject::getJSONArray);
    }

}
