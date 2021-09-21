package com.sxtanna.mc.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sxtanna.mc.util.Json;
import com.sxtanna.mc.util.Rest;

import kong.unirest.json.JSONArray;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;

public class Type
{

    private static Optional<String> getPaperAPIGenericLatestBuildVersionJarURL(@NotNull final Vers vers, @NotNull final String name, @NotNull final String base, @NotNull final String link)
    {
        return Rest.json(base, "%s/version_group/%s/builds", name, vers.mcVersionGroup())
                   .flatMap(it -> Json.getListAtPath(it, "builds"))
                   .flatMap(it -> Json.getLastInList(it, JSONArray::getJSONObject))
                   .map(it -> {
                       final var b = Json.getTextAtPath(it, "build");
                       final var v = Json.getTextAtPath(it, "version");
                       final var n = Json.getTextAtPath(it, "downloads:application:name");

                       if (b.isEmpty() || v.isEmpty() || n.isEmpty())
                       {
                           return null;
                       }

                       return String.format(link,
                                            name,
                                            v.get(),
                                            b.get(),
                                            n.get());
                   });
    }


    public static final Type BUNGEE = new Type("waterfall", "end", "https://papermc.io/api/v2/projects", "https://papermc.io/api/v2/projects/%s/versions/%s/builds/%s/downloads/%s")
    {
        @Override
        public Optional<String> getLatestVersion(@NotNull final Vers vers)
        {
            return Optional.of(vers.mcVersionGroup());
        }

        @Override
        public Optional<String> getVersionJarURL(@NotNull final Vers vers, final @Nullable String version)
        {
            return getPaperAPIGenericLatestBuildVersionJarURL(vers, getName(), getBase(), getLink());
        }
    };

    public static final Type SPIGOT = new Type("paper", "stop", "https://papermc.io/api/v2/projects", "https://papermc.io/api/v2/projects/%s/versions/%s/builds/%s/downloads/%s")
    {
        @Override
        public Optional<String> getLatestVersion(@NotNull final Vers vers)
        {
            return Rest.json(getBase(), "%s/version_group/%s", getName(), vers.mcVersionGroup())
                       .flatMap(it -> Json.getListAtPath(it, "versions"))
                       .flatMap(it -> Json.getLastInList(it, JSONArray::getString));
        }

        @Override
        public Optional<String> getVersionJarURL(@NotNull final Vers vers, final @Nullable String version)
        {
            return getPaperAPIGenericLatestBuildVersionJarURL(vers, getName(), getBase(), getLink());
        }
    };

    public static final Type PURPUR = new Type("purpur", "stop", "https://api.pl3x.net/v2/purpur", "https://api.pl3x.net/v2/purpur/%s/%s/download")
    {
        @Override
        public Optional<String> getLatestVersion(@NotNull final Vers vers)
        {
            return Rest.json(getBase(), "%s", vers.mcVersion())
                       .flatMap(it -> Json.getTextAtPath(it, "builds:latest"));
        }

        @Override
        public Optional<String> getVersionJarURL(@NotNull final Vers vers, final @Nullable String version)
        {
            return Optional.ofNullable(version).map(it -> String.format(getLink(), vers.mcVersion(), it));
        }
    };


    @Contract(value = " -> new", pure = true)
    public static @NotNull Type[] values()
    {
        return new Type[]{BUNGEE, SPIGOT, PURPUR};
    }


    public static @NotNull Type resolveType(@NotNull final String text)
    {
        final Type type;

        switch (text.toLowerCase(Locale.ROOT))
        {
            case "spigot":
                type = Type.SPIGOT;
                break;
            case "bungee":
                type = Type.BUNGEE;
                break;
            case "purpur":
                type = Type.PURPUR;
                break;
            default:
                try
                {
                    new URL(text);
                    type = new Type("custom", null, null, text);
                }
                catch (final MalformedURLException ex)
                {
                    throw new IllegalArgumentException(String.format("invalid type: '%s', must be of [bungee, spigot, purpur, URL]", text), ex);
                }
                break;
        }

        return type;
    }


    @NotNull
    private final String name;
    @Nullable
    private final String stop;
    @Nullable
    private final String base;
    @Nullable
    private final String link;


    @Contract(pure = true)
    public Type(@NotNull final String name, @Nullable final String stop)
    {
        this(name, stop, null, null);
    }

    @Contract(pure = true)
    public Type(@NotNull final String name, @Nullable final String stop, @Nullable final String base, @Nullable final String link)
    {
        this.name = name;
        this.stop = stop;
        this.base = base;
        this.link = link;
    }


    @Contract(pure = true)
    public @NotNull final String getName()
    {
        return this.name;
    }

    @Contract(pure = true)
    public @Nullable final String getStop()
    {
        return this.stop;
    }

    @Contract(pure = true)
    public @Nullable final String getBase()
    {
        return this.base;
    }

    @Contract(pure = true)
    public @Nullable final String getLink()
    {
        return this.link;
    }


    @Contract(pure = true)
    public final boolean isJsonRestfulAPI()
    {
        return getBase() != null;
    }

    @Contract(pure = true)
    public final boolean isDirectDownload()
    {
        return getLink() != null;
    }

    @Contract(pure = true)
    public final boolean canGracefulClose()
    {
        return getStop() != null;
    }


    public Optional<String> getLatestVersion(@NotNull final Vers vers)
    {
        return Optional.empty();
    }

    public Optional<String> getVersionJarURL(@NotNull final Vers vers, @Nullable final String version)
    {
        return Optional.empty();
    }


    @Override
    @Contract(pure = true)
    public @NotNull final String toString()
    {
        return String.format("Type[%s]", this.name);
    }

}
