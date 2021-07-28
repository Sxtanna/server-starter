package com.sxtanna.mc.conf;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sxtanna.mc.data.Size;
import com.sxtanna.mc.data.Type;
import com.sxtanna.mc.data.Vers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

public final class ServerStarterConf
{

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder()
    {
        return new Builder();
    }


    @Nullable
    private final Path path;
    @Nullable
    private final Size size;
    @Nullable
    private final Type type;
    @Nullable
    private final Vers vers;


    @Contract(pure = true)
    public ServerStarterConf(@Nullable final Path path,
                             @Nullable final Size size,
                             @Nullable final Type type,
                             @Nullable final Vers vers)
    {
        this.path = path;
        this.size = size;
        this.type = type;
        this.vers = vers;
    }

    @Contract(pure = true)
    private ServerStarterConf(@NotNull final Builder builder)
    {
        this(builder.path,
             builder.size,
             builder.type,
             builder.vers);
    }


    @Contract(pure = true)
    public @NotNull Optional<Path> path()
    {
        return Optional.ofNullable(this.path);
    }

    @Contract(pure = true)
    public @NotNull Optional<Size> size()
    {
        return Optional.ofNullable(this.size);
    }

    @Contract(pure = true)
    public @NotNull Optional<Type> type()
    {
        return Optional.ofNullable(this.type);
    }

    @Contract(pure = true)
    public @NotNull Optional<Vers> vers()
    {
        return Optional.ofNullable(this.vers);
    }


    @Contract(value = "_ -> new", pure = true)
    public @NotNull ServerStarterConf with(@Nullable final Path path)
    {
        return new ServerStarterConf(path, this.size, this.type, this.vers);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ServerStarterConf with(@Nullable final Size size)
    {
        return new ServerStarterConf(this.path, size, this.type, this.vers);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ServerStarterConf with(@Nullable final Type type)
    {
        return new ServerStarterConf(this.path, this.size, type, this.vers);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ServerStarterConf with(@Nullable final Vers vers)
    {
        return new ServerStarterConf(this.path, this.size, this.type, vers);
    }


    public static final class Builder
    {

        @Nullable
        private Path path = null;
        @Nullable
        private Size size = null;
        @Nullable
        private Type type = null;
        @Nullable
        private Vers vers = null;


        @Contract(pure = true)
        private Builder()
        {}


        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder path(@Nullable final Path path)
        {
            this.path = path;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder size(@Nullable final Size size)
        {
            this.size = size;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder type(@Nullable final Type type)
        {
            this.type = type;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder vers(@Nullable final Vers vers)
        {
            this.vers = vers;
            return this;
        }


        @Contract(value = " -> this", mutates = "this")
        public @NotNull Builder defaults()
        {
            return path(Paths.get("")).size(Size.SMALL).type(Type.SPIGOT).vers(Vers.latest());
        }


        @Contract(value = " -> new", pure = true)
        public @NotNull ServerStarterConf build()
        {
            return new ServerStarterConf(this);
        }

    }


    public static final class IOCodec
    {

        private static final class ConfigWrapped
        {

            private Config config;

            public ConfigWrapped(final Config config)
            {
                this.config = config;
            }


            public void with(@NotNull final String path, @NotNull final Object data)
            {
                this.config = this.config.withValue(path, ConfigValueFactory.fromAnyRef(data));
            }

        }


        public static @NotNull Config pushIntoConfig(@NotNull final ServerStarterConf conf)
        {
            final var config = new ConfigWrapped(ConfigFactory.empty());

            conf.path()
                .ifPresent(path -> config.with("path", path.toAbsolutePath().toString()));

            conf.size()
                .ifPresent(size -> config.with("size", String.format("%d:%d", size.getMinMemory(), size.getMaxMemory())));

            conf.type()
                .ifPresent(type -> {
                    final var name = type.getName().toLowerCase(Locale.ROOT);

                    switch (name)
                    {
                        case "paper":
                            config.with("type", "spigot");
                            break;
                        case "purpur":
                            config.with("type", "purpur");
                            break;
                        case "waterfall":
                            config.with("type", "bungee");
                            break;
                        case "custom":
                            final var link = type.getLink();

                            config.with("type", link == null ? "" : link);
                            break;
                    }
                });

            conf.vers()
                .ifPresent(vers -> config.with("vers", vers.mcVersion()));

            return config.config;
        }

        public static @NotNull ServerStarterConf pullFromConfig(@NotNull final Config conf)
        {
            final var builder = ServerStarterConf.builder();

            if (conf.hasPath("path"))
            {
                builder.path(Paths.get(conf.getString("path")).toAbsolutePath());
            }

            if (conf.hasPath("size"))
            {
                builder.size(Size.resolveSize(conf.getString("size")));
            }

            if (conf.hasPath("type"))
            {
                builder.type(Type.resolveType(conf.getString("type")));
            }

            if (conf.hasPath("vers"))
            {
                builder.vers(Vers.resolveVers(conf.getString("vers")));
            }

            return builder.build();
        }

    }

}
