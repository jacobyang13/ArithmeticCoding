package app;


//so use differential because the pixels themselves are very rarely going to change

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class StaticACDifferentialEncodeTextFile {

	public static void main(String[] args) throws IOException {
		String input_file_name = "data/out.dat";
		String output_file_name = "data/static-compressed.dat";

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();
		
		// Analyze file for frequency counts
		
		FileInputStream fis = new FileInputStream(input_file_name);


		int next_byte = 0;
		//so edit stuff here with the changing functions
		//so frame is 64 * 64 pixels = 4096 for one frame, then 4096 for consecutive
		//my symbols variable will be from 255 + 255 + 1 because -255 to 255
		//my symbol count will come after i do differential encoding and have the numbers such as 0 and 1
		
		int[] firstFrame = new int[4096];
		int[] prevFrame = new int[4096];
		int[] differSymbols = new int[1224704];
		//values will be from -255 to 255 so size will be 255 + 255 + 1 then
		//differential coding which means i look at first frame and then look at next and put a number in array
		//from array in number then I update prev to current
		//create static probability table based on the potential differences such as 0, -1 and compress based on this
		//numbers
		int[] differCount = new int[511];
		
		//so first frame here
		for(int i = 0; i < 4096; i++) {
			next_byte = fis.read();
			
			firstFrame[i] = next_byte;
			prevFrame[i] = next_byte;
			//System.out.println("encode " + firstFrame[i]);
		}
		//now do differential part
		int j = 0;
	
		for(int i = 0 ; i <1224704;i++) {
			next_byte = fis.read();
			//System.out.println(next_byte+  "at I " + i);
			int difTemp =  next_byte - prevFrame[j % 4096];
			//update first frame to prev now
			prevFrame[j % 4096] += difTemp;
			//System.out.println("this is the first frame value " + firstFrame[j % 4096]);
			
			differSymbols[j] = difTemp;
			//System.out.println("this is the dif value " + difTemp);
			j++;
		}
		fis.close();
		
		//this is the static probability table, but instead of doing a normal regression, i am actually taking the actual proabilities.
		
		for(int i = 0 ; i < 1224704; i ++) {
				int indexT = (differSymbols[i] + 255);
				differCount[indexT]++;

			
	
			
		}
		
		/*while (next_byte != -1) {
			symbol_counts[next_byte]++;
			next_byte = fis.read();
		}*/
	
		
		//so the decoder should have the first 4096 output as normal, then im outputting the symbols as -255 to 255 and their count
		
		
		
		Integer[] symbols = new Integer[511];
		int startT = -255;
		for (int i=0; i<511; i++) {
			symbols[i] = -startT;
			startT++;
		}

		// Create new model with analyzed frequency counts
		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols, differCount);

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(40);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// First 511 * 4 bytes are the frequency counts 
		for (int i=0; i<511; i++) {
			bit_sink.write(differCount[i], 32);
			//System.out.println(differCount[i] + " symbols " + symbols[i]);
		}

		// Next 4 bytes are the number of symbols encoded
		bit_sink.write(num_symbols, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);
		//next 4096 * 8 are the first frame numbers so this is always hardcoded
		for(int i = 0; i < 4096; i++) {
			
			bit_sink.write(firstFrame[i], 8);
			//System.out.println("encode2 " + firstFrame[i]);
		}
		// Now encode the input
		
		
		for (int i=0; i<1224704; i++) {
			
			encoder.encode(differSymbols[i], model, bit_sink);
			
			//System.out.println("Encode" + "difference " + differSymbols[i]+  "at I " + i);
			//System.out.println("i " + i + "difference " + differSymbols[i]);
		}
	

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
	}
}
