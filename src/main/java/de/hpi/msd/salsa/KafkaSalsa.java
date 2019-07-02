package de.hpi.msd.salsa;

import de.hpi.msd.salsa.commands.RangeKeyApp;
import de.hpi.msd.salsa.commands.SamplingApp;
import de.hpi.msd.salsa.commands.SegmentedApp;
import de.hpi.msd.salsa.commands.SimpleApp;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "kafka-salsa",
        mixinStandardHelpOptions = true,
        subcommands = {
                RangeKeyApp.class,
                SamplingApp.class,
                SegmentedApp.class,
                SimpleApp.class
        },
        commandListHeading = "%nCommands:%n%n"
)
public class KafkaSalsa implements Callable<Void> {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        System.exit(new CommandLine(new KafkaSalsa()).execute(args));
    }

    @Override
    public Void call() throws CommandLine.ParameterException {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}
