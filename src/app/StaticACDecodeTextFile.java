package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class StaticACDecodeTextFile {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/static-compressed.dat";
		String output_file_name = "data/reuncompressed.txt";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		// Read in symbol counts and set up model
		
		int[] symbol_counts = new int[256];
		Integer[] symbols = new Integer[256];
		
		for (int i=0; i<256; i++) {
			symbol_counts[i] = bit_source.next(32);
			symbols[i] = i;
		}

		FreqCountIntegerSymbolModel model = 
				new FreqCountIntegerSymbolModel(symbols, symbol_counts);
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		for (int i=0; i<num_symbols; i++) {
			int sym = decoder.decode(model, bit_source);
			fos.write(sym);
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
}