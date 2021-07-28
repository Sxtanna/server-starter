package com.sxtanna.mc;

import org.jetbrains.annotations.NotNull;

import com.sxtanna.mc.conf.ServerStarterConf;
import com.sxtanna.mc.data.Size;
import com.sxtanna.mc.data.Type;
import com.sxtanna.mc.data.Vers;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class ServerStarterMain
{

    public static void main(@NotNull final String... args)
    {
        final var root = Paths.get(".").toAbsolutePath();
        final var conf = root.resolve("server-starter.conf").toAbsolutePath();


        ServerStarterConf config = ServerStarterConf.builder().build();

        if (Files.notExists(conf))
        {
            try
            {
                saveConfig(conf, ServerStarterConf.builder()
                                                  .defaults()
                                                  .build());
            }
            catch (final IOException ex)
            {
                System.out.println("Failed to save default config");
                ex.printStackTrace();
                return;
            }
        }
        else
        {
            try
            {
                config = loadConfig(conf);
            }
            catch (final IOException ex)
            {
                System.out.println("Failed to load config");
            }
        }


        final var option = new Options();
        final var parser = new DefaultParser();

        final var optionPath = Option.builder("p")
                                     .desc("The path of the server")
                                     .argName("Path")
                                     .longOpt("path")
                                     .hasArg(true)
                                     .build();

        final var optionSize = Option.builder("s")
                                     .desc("Small or Large Instance")
                                     .argName("Size")
                                     .longOpt("size")
                                     .hasArg(true)
                                     .build();

        final var optionType = Option.builder("t")
                                     .desc("Bungee or Spigot Server")
                                     .argName("Type")
                                     .longOpt("type")
                                     .hasArg(true)
                                     .build();

        final var optionVers = Option.builder("v")
                                     .desc("The version to attempt to run")
                                     .argName("Version")
                                     .longOpt("version")
                                     .hasArg(true)
                                     .build();

        final var optionTest = Option.builder("d")
                                     .desc("Debug testing mode")
                                     .argName("Debug")
                                     .longOpt("debug")
                                     .build();

        option.addOption(optionPath);
        option.addOption(optionSize);
        option.addOption(optionType);
        option.addOption(optionVers);
        option.addOption(optionTest);

        final CommandLine commands;

        try
        {
            commands = parser.parse(option, args);
        }
        catch (final ParseException ex)
        {
            System.out.println("Failed to parse starter options!");
            System.out.println(ex.getMessage());

            final var format = new HelpFormatter();
            format.printHelp("server-starter", option);

            return;
        }


        final var valueTest = commands.hasOption("d");


        final var type = Optional
                .ofNullable(commands.getOptionValue("t"))
                .map(Type::resolveType)
                .or(config::type)
                .orElse(Type.SPIGOT);

        final var path = Optional
                .ofNullable(commands.getOptionValue("p"))
                .map(Paths::get).map(Path::toAbsolutePath)
                .or(config::path)
                .orElse(Paths.get("").toAbsolutePath());

        final var size = Optional
                .ofNullable(commands.getOptionValue("s"))
                .map(Size::resolveSize)
                .or(config::size)
                .orElse(Size.SMALL);

        final var vers = Optional
                .ofNullable(commands.getOptionValue("v"))
                .map(Vers::resolveVers)
                .or(config::vers)
                .orElse(Vers.latest());


        if (valueTest)
        {
            System.out.printf("This would run: %n%s%n%s%nVers[%s]%nPath[%s]%n", size, type, vers, path);
            return;
        }


        try
        {
            exec(type, path, size, vers);
        }
        catch (IOException | InterruptedException e)
        {
            System.out.println("Failed to run server: ");
            e.printStackTrace();
        }
    }

    private static void exec(@NotNull final Type type, @NotNull final Path path, @NotNull final Size size, @NotNull final Vers vers) throws IOException, InterruptedException
    {
        final var starter = new ServerStarter(type, path, size, vers);
        starter.saveServerJar();
        starter.execServerJar();
    }


    private static @NotNull ServerStarterConf loadConfig(@NotNull final Path path) throws IOException
    {
        return ServerStarterConf.IOCodec.pullFromConfig(ConfigFactory.parseFile(path.toFile()));
    }

    private static void saveConfig(@NotNull final Path path, @NotNull final ServerStarterConf conf) throws IOException
    {
        Files.writeString(path, ServerStarterConf.IOCodec.pushIntoConfig(conf)
                                                         .root()
                                                         .render(ConfigRenderOptions.defaults()
                                                                                    .setOriginComments(false)
                                                                                    .setFormatted(true)
                                                                                    .setJson(false)));
    }

}