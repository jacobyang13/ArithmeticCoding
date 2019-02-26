package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class StaticACDifferentialDecodeTextFile {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/static-compressed.dat";
		String output_file_name = "data/reout.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		// Read in symbol counts and set up model
		int[] firstFrame = new int[4096];
		int[] prevFrame = new int[4096];
		int[] differCount = new int[511];
		
		Integer[] symbols = new Integer[511];
		int startT = -255;
		for (int i=0; i<511; i++) {
			differCount[i] = bit_source.next(32);
		
			symbols[i] = -startT;
			//System.out.println(differCount[i] + " symbols " + symbols[i]);
			startT++;
		}

		
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		
	
		FreqCountIntegerSymbolModel model = 
				new FreqCountIntegerSymbolModel(symbols, differCount);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(40);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);
		
		// Now encode the input
		for(int i = 0; i < 4096; i++) {
		
			int first = bit_source.next(8);
			firstFrame[i] = first;
			prevFrame[i] = first;
			//System.out.println("decode" + firstFrame[i]);
			fos.write(firstFrame[i]);
		}

	
		
		int difTotal = 0;
		int sym = 0;
		for (int i=0; i<1224704; i++) {
			sym = decoder.decode(model, bit_source);
			//System.out.println(prevFrame[i % 4096]);
			//System.out.println("i " + i+ " " + sym);
			difTotal =  sym + prevFrame[i % 4096];
			//update first frame to prev now
			prevFrame[i % 4096] += sym;
			//System.out.println("i " + i + "difference " + difTotal);
			//System.out.println("this is the first frame value " + firstFrame[i % 4096])
			//if(i >=523762 && i <=523762 + 10) {
			//System.out.println("Decode" + "difference " + sym + " total " + difTotal + " i " + i );
			//}
			fos.write(difTotal);
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
}
