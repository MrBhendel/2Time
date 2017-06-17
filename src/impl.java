import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import com.aliasi.lm.NGramProcessLM;

public class impl {

	private impl() {

	}

	/**
	 * 
	 * @param inputCorpus
	 *            Name of corpus file to read.
	 * @param inputData
	 *            String containing XORed plaintexts.
	 * @param outputData
	 *            File to write plaintext guesses to.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void peel(String inputCorpus, byte[] inputData,
			String outputData) throws FileNotFoundException, IOException {

		File corpus = new File(inputCorpus);
		ByteArrayInputStream is = new ByteArrayInputStream(
				FileUtils.readFileToByteArray(corpus));
		NGramProcessLM mLM = NGramProcessLM.readFrom(is);

		double[][] fromProbTable = new double[256][256];
		String[][] fromStrTable1 = new String[256][256];
		String[][] fromStrTable2 = new String[256][256];

		double[][] toProbTable = new double[256][256];
		String[][] toStrTable1 = new String[256][256];
		String[][] toStrTable2 = new String[256][256];

		System.out.println("[+] Processing byte 1...");

		// Initialize "from" table.
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				String char1 = "" + (char) i;
				String char2 = "" + (char) j;
				fromStrTable1[i][j] = char1;
				fromStrTable2[i][j] = char2;
				fromProbTable[i][j] = -Math.log(mLM.prob(char1)
						* mLM.prob(char2));
			}
		}

		for (int i = 1; i < inputData.length; i++) {

			System.out.println("[+] Processing byte " + (i + 1) + "...");

			for (int j = 0; j < 256; j++)
				Arrays.fill(toProbTable[j], Double.MAX_VALUE);

			for (int j = 0; j < 256; j++) {
				for (int k = 0; k < 256; k++) {

					if (fromProbTable[j][k] == Double.MAX_VALUE
							|| fromStrTable1[j][k].length() != i)
						continue;

					String one = fromStrTable1[j][k];
					if (one.length() > 6)
						one = one.substring(one.length() - 6);

					String two = fromStrTable2[j][k];
					if (two.length() > 6)
						two = two.substring(two.length() - 6);

					// check every possible byte combination that XORs to
					// message byte
					for (int l = 0; l < 256; l++) {

						double oneProb = mLM.prob(one + (char) l);
						double twoProb = mLM.prob(two
								+ (char) (l ^ inputData[i]));

						double totalWeight = -Math.log(oneProb * twoProb);

						if (toProbTable[l][l ^ inputData[i]] > totalWeight) {
							toProbTable[l][l ^ inputData[i]] = totalWeight;
							toStrTable1[l][l ^ inputData[i]] = fromStrTable1[j][k]
									+ (char) l;
							toStrTable2[l][l ^ inputData[i]] = fromStrTable2[j][k]
									+ (char) (l ^ inputData[i]);
						}
					}
				}
			}

			// combine the probabilities into a running total

			for (int j = 0; j < 256; j++) {
				for (int k = 0; k < 256; k++) {
					if (toProbTable[j][k] != Double.MAX_VALUE
							&& fromProbTable[j][k] != Double.MAX_VALUE)
						toProbTable[j][k] += fromProbTable[j][k];
				}
			}

			// make old "to" new from, create blank "to" data structures

			fromProbTable = toProbTable.clone();
			fromStrTable1 = toStrTable1.clone();
			fromStrTable2 = toStrTable2.clone();

			toProbTable = new double[256][256];
			toStrTable1 = new String[256][256];
			toStrTable2 = new String[256][256];
		}

		// locate shortest path
		double min = Double.MAX_VALUE;
		String oneChoice = "";
		String twoChoice = "";
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				if (fromProbTable[i][j] < min) {
					min = fromProbTable[i][j];
					oneChoice = fromStrTable1[i][j];
					twoChoice = fromStrTable2[i][j];
				}
			}
		}

		System.out.println("[+] Done.\n\nMessage 1:\n\n" + oneChoice
				+ "\n\nMessage 2:\n\n" + twoChoice);

		FileOutputStream fstream1 = new FileOutputStream(outputData + ".1");
		FileOutputStream fstream2 = new FileOutputStream(outputData + ".2");
		DataOutputStream out1 = new DataOutputStream(fstream1);
		DataOutputStream out2 = new DataOutputStream(fstream2);
		for (int i = 0; i < oneChoice.length(); i++) {
			out1.writeByte(oneChoice.charAt(i));
			out2.writeByte(twoChoice.charAt(i));
		}
		out1.close();
		out2.close();
	}

	public static void generateCorpus(String inputDir, String outputCorpus)
			throws IOException {

		NGramProcessLM mLM = new NGramProcessLM(7);

		final File folder = new File(inputDir);
		processDirectory(mLM, folder);

		System.out.println("[+] Writing corpus to " + outputCorpus);
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		mLM.writeTo(bytesOut);
		try {
			OutputStream os = new FileOutputStream(outputCorpus);
			bytesOut.writeTo(os);
			os.close();
		} catch (Exception e) {

		}

		System.out.println("\nDone.\n\n");
	}
	
	private static void processDirectory(NGramProcessLM mLM, File folder) throws IOException {
		for (final File f : folder.listFiles()) {
			if (f.isDirectory()) {
				processDirectory(mLM, f);
				continue;
			}
			System.out.println("[+] Reading file " + f.getCanonicalPath()
					+ " into corpus.");
			Scanner scanner = new Scanner(f).useDelimiter("\\Z");
			if(!scanner.hasNext()) continue;
			String read = scanner.next();
			mLM.train(read);
			scanner.close();
		}
	}

}
