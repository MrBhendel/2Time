import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.*;

public class main {

	public static void main(String[] args) throws IOException {

		Options opt = new Options();

		Option inputDir = new Option("iDr", "inputDir", true,
				"input corpus data directory");
		opt.addOption(inputDir);

		Option inputCorpus = new Option("iC", "inputCorpus", true,
				"input corpus file");
		opt.addOption(inputCorpus);

		Option outputCorpus = new Option("oC", "outputCorpus", true,
				"output corpus file");
		opt.addOption(outputCorpus);

		Option outputData = new Option("oD", "outputData", true,
				"plaintext output file name");
		opt.addOption(outputData);

		Option inputData = new Option("iD", "inputData", true,
				"file containing XORed plaintexts");
		opt.addOption(inputData);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(opt, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("2Time", opt);
			printUseCases();
			System.exit(1);
			return;
		}

		if (!validParamCombo(cmd)) {
			formatter.printHelp("2Time", opt);
			printUseCases();
			System.exit(1);
			return;
		}

		if (cmd.hasOption("inputDir")) {
			impl.generateCorpus(cmd.getOptionValue("inputDir"),
					cmd.getOptionValue("outputCorpus"));
		} else {
			byte[] inputDataBytes = Files.readAllBytes(Paths.get(cmd
					.getOptionValue("inputData")));
			impl.peel(cmd.getOptionValue("inputCorpus"), inputDataBytes,
					cmd.getOptionValue("outputData"));
		}

	}

	private static boolean validParamCombo(CommandLine p) {

		// use case : generate corpus file from collection of files
		if (p.hasOption("inputDir") && !p.hasOption("inputCorpus")
				&& p.hasOption("outputCorpus") && !p.hasOption("outputData")
				&& !p.hasOption("inputData"))
			return true;

		// use case : use corpus file to peel apart XORed plaintexts
		if (!p.hasOption("inputDir") && p.hasOption("inputCorpus")
				&& !p.hasOption("outputCorpus") && p.hasOption("outputData")
				&& p.hasOption("inputData"))
			return true;

		return false;
	}

	private static void printUseCases() {
		System.out.println("\nuse cases:");
		System.out
				.println(" 2Time --inputDir [dir name] --outputCorpus [file name]");
		System.out
				.println(" 2Time --inputCorpus [file name] --inputData [file name] --outputData \n       [file name]");
	}

}
