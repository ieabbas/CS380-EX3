// Luis Cortes
// CS 380 
// Exercise 3

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;

public class Ex3Client {
	private static ArrayList<Integer> bytesReadIn = new ArrayList<>();
	private static ArrayList<Integer> buffer = new ArrayList<>();
	
	public static void main(String[] args) {
		try {
			// Get IP Address
			InetAddress address = InetAddress.getByName(
				new URL("http://codebank.xyz").getHost());
			String ip = address.getHostAddress();

			// Connect to server
			Socket socket = new Socket(ip, 38103);
			System.out.println("Connected to server.");

			InputStream is = socket.getInputStream();

			// Read in first byte to determine how many bytes to read in
			int count = is.read();
			System.out.println("Reading "+count+" bytes.");

			// Read in bytes 
			int columnCount = 0; // Keep track of how many columns are printed
			System.out.print("   ");
			while(true) {
				if (count == 0) { // Stop reading in bytes
					break;
				}

				int bytes = is.read(); // Read in data
				System.out.print(formattedByte(bytes));
				bytesReadIn.add(bytes); 
				// System.out.println(count+": "+Integer.toHexString(bytes & 0xFF));
				columnCount++;

				// Start a new line when 20 characters in a row
				if (columnCount % 10 == 0) 
					System.out.print("\n   ");

				count--;
			}
			System.out.println();

			to16Bits(); // Build 16 bit number out of two 8 bits

			// Separate 16 bit check sum to two bytes
			byte[] checkSum = new byte[2]; 
			short sum = (short) cksum();

			System.out.println("Checksum calculated: 0x"+Integer.toHexString(sum & 0xFFFF));

			checkSum[0] = (byte) ((sum >> 8) & 0xFF);
			checkSum[1] = (byte)(sum & 0xFF);

			PrintStream outStream = new PrintStream(socket.getOutputStream(),  true); 
			outStream.write(checkSum, 0, checkSum.length); // Send to server

			byte response = (byte) is.read(); // Receive response
			if (response == 1) 
				System.out.println("Response good");
			else
				System.out.println("Response bad");


		} catch (Exception e) {e.printStackTrace();} // end try-catch
	} // end main

	// Format byte, so when printed on console, is padded with zeroes if required
	public static String formattedByte(int bits) {
		String part = Integer.toHexString(bits & 0xFF);	// Get last 8 bits
		int size = part.length(); 
		
		if (size == 1) // size can only be 2 or 1
			return "0"+part;
		return part;
	}

	// Create a 16 bit number of two paired bytes
	public static void to16Bits() {
		int length = bytesReadIn.size();
		int i = 0;

		// For every two bytes make a 16 bit number and add the 16 bit number to buffer
		while (length > 1){
			int numOne = bytesReadIn.get(i);
			numOne = numOne << 8;
			// System.out.println("First number: "+Integer.toHexString(numOne & 0xFFFF));
			int numTwo = bytesReadIn.get(i + 1);
			// System.out.println("Second number: "+Integer.toHexString(numTwo & 0xFFFF));

			int together = numOne | numTwo;
			// System.out.println("Both: "+Integer.toHexString(together & 0xFFFF));
			buffer.add(together);
			i +=2;
			length -=2;
		}

		// Put last byte to buffer if number of bytes is odd
		if (length > 0) {
			int numOdd = bytesReadIn.get(bytesReadIn.size()-1);
			// System.out.println("Odd one: "+Integer.toHexString(numOdd & 0xFFFF));
			numOdd = numOdd << 8;
			buffer.add(numOdd);
		}
	}

	public static long cksum() {
		int length = buffer.size();
		int index = 0;
		
		long sum = 0;

		while (length > 0) {
			sum += buffer.get(index);
			if ((sum & 0xFFFF0000) > 0) {
				// Carry occurred
				sum = sum & 0xFFFF;
				sum++;
			} 

			index++;
			length--;
		}
		sum = ~sum;
		sum = sum & 0xFFFF;
		return sum;
	}
}