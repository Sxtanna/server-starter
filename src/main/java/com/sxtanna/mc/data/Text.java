package com.sxtanna.mc.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Text
{

    @NotNull
    String JAR_NAME = "server_{t}.jar";


    @NotNull
    String MIN_MEMORY = "-Xms{s}M";
    @NotNull
    String MAX_MEMORY = "-Xmx{s}M";


    @NotNull
    List<String> ARGS_PROXIES = List.of(
            "-XX:+UseG1GC",
            "-XX:G1HeapRegionSize=4M",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+ParallelRefProcEnabled",
            "-XX:+AlwaysPreTouch",

            "-Dfile.encoding=UTF-8");

    @NotNull
    List<String> ARGS_SERVERS = List.of(
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch",

            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",

            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1",

            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40",
            "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20",
            "-XX:InitiatingHeapOccupancyPercent=15",

            "-Dfile.encoding=UTF-8",

            "-Daikars.new.flags=true",
            "-Dusing.aikars.flags=https://mcflags.emc.gs",

            "-Dcom.mojang.eula.agree=true",
            "-Dpaper.debug-sync-loads=true");

    @NotNull
    List<String> ARGS_GRAALVM = List.of(
            "-server",
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+UnlockDiagnosticVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch",
            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40",
            "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:InitiatingHeapOccupancyPercent=15",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",
            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1",
            "-Dusing.aikars.flags=https://mcflags.emc.gs",
            "-Daikars.new.flags=true",
            "-XX:-UseBiasedLocking",
            "-XX:+EnableJVMCIProduct",
            "-XX:+EnableJVMCI",
            "-XX:+UseJVMCICompiler",
            "-XX:+EagerJVMCI",
            "-XX:UseAVX=2",
            "-XX:+UseStringDeduplication",
            "-XX:+UseFastUnorderedTimeStamps",
            "-XX:+UseAES",
            "-XX:+UseAESIntrinsics",
            "-XX:UseSSE=4",
            "-XX:AllocatePrefetchStyle=1",
            "-XX:+UseLoopPredicate",
            "-XX:+RangeCheckElimination",
            "-XX:+EliminateLocks",
            "-XX:+DoEscapeAnalysis",
            "-XX:+UseCodeCacheFlushing",
            "-XX:+UseFastJNIAccessors",
            "-XX:+OptimizeStringConcat",
            "-XX:+UseCompressedOops",
            "-XX:+UseThreadPriorities",
            "-XX:+OmitStackTraceInFastThrow",
            "-XX:+TrustFinalNonStaticFields",
            "-XX:ThreadPriorityPolicy=1",
            "-XX:+UseInlineCaches",
            "-XX:+RewriteBytecodes",
            "-XX:+RewriteFrequentPairs",
            "-XX:+UseNUMA",
            "-XX:-DontCompileHugeMethods",
            "-XX:+UseCMoveUnconditionally",
            "-XX:+UseFPUForSpilling",
            "-XX:+UseNewLongLShift",
            "-XX:+UseVectorCmov",
            "-XX:+UseXMMForArrayCopy",
            "-XX:+UseXmmI2D",
            "-XX:+UseXmmI2F",
            "-XX:+UseXmmLoadAndClearUpper",
            "-XX:+UseXmmRegToRegMoveAll",
            "-Dfile.encoding=UTF-8",
            "-Djdk.nio.maxCachedBufferSize=262144",
            "-Dgraal.TuneInlinerExploration=1",
            "-Dgraal.CompilerConfiguration=enterprise",
            "-Dgraal.UsePriorityInlining=true",
            "-Dgraal.Vectorization=true",
            "-Dgraal.OptDuplication=true",
            "-Dgraal.DetectInvertedLoopsAsCounted=true",
            "-Dgraal.LoopInversion=true",
            "-Dgraal.VectorizeHashes=true",
            "-Dgraal.EnterprisePartialUnroll=true",
            "-Dgraal.VectorizeSIMD=true",
            "-Dgraal.StripMineNonCountedLoops=true",
            "-Dgraal.SpeculativeGuardMovement=true",
            "-Dgraal.InfeasiblePathCorrelation=true",
            "--add-modules",
            "jdk.incubator.vector");

    @NotNull
    List<String> OPEN_MODULES = List.of(
            "--add-opens",
            "java.base/java.net=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.lang.invoke=ALL-UNNAMED");

    @NotNull
    List<String> ANSI_CONSOLE = List.of(
            "-Dterminal.jline=false",
            "-Dterminal.ansi=true");

}
