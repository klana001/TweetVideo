package TwitterVideoCompress;


/**
 * @author Nicholas Klaebe
 */

import java.io.*;

public class BitOutputStream
	{
		int value;				// The byte in which the encoded bits are firstly stored.
		int count;				// The number of bits written into value.
		byte[] buffer;			// A byte buffer which is filled with 'value' each time value is full. Used for wirting to file.
		int buffCount;			// The current number of 'values' written into the buffer.
		long masterCount;		// The overall count of bits that have been written
		OutputStream fos;
		
		/**
		 * constructor
		 * @param fos1	The outputstream which this bit stream writes to
		 */
		public BitOutputStream(OutputStream fos1)
		{
			fos=fos1;
			value=0;
			count=0;
			buffer=new byte[4096];
			buffCount=0;
			masterCount=0;
		}
		
		/**
		 * Writes the passed value (temp) to the file using the given number of bits
		 * @param temp		the value to be written
		 * @param bits		the number if bits to write
		 * @throws IOException
		 */
		public void write(long temp,int bits) throws IOException
		{

	
//			System.out.print("code written: ");
				for (int j = 0, mask = 1; j < bits; j++, mask <<= 1)
				{  
					value=value<<1;count++;
					if  ((temp & mask) > 0)
					{
						value=value|0x01;
//						System.out.print("1");
					}
//					System.out.print("0");
					addToBuffer();
				}
//				System.out.println("");
		}
		
		/**
		 * write a single bit to the stream
		 * @param bit		The bit to write
		 * @throws IOException
		 */
		public void write(boolean bit) throws IOException
		{
			value=value<<1;count++;
			if  (bit)
			{
				value=value|0x01;
			}
			addToBuffer();
				
		}
		
		/**
		 * writes a single char (converted to a byte) to the output stream aligning with the next byte boundary
		 * @param c	 	the char to write
		 * @throws IOException
		 */
		
		public void write(char c) throws IOException
		{
				flush();
				byte[] b=new byte[1];
				b[0]=(byte) c;
				fos.write(b);
				masterCount+=8;
				
		}

		/**
		 * adds bits stored in 'value' to a buffer which will be saved to a file
		 * if the current bit count since last storing into the buffer is less than 8 then return without adding it to the buffer
		 * @throws IOException
		 */		
	
		public void addToBuffer() throws IOException
		{
			masterCount++;
		
			if (count<8) return;

			//byte temp=(byte) (value);
			buffer[buffCount]=(byte) (value);;
			buffCount++;
		
			if (buffCount==buffer.length)
			{
				fos.write(buffer,0,buffCount);
				buffCount=0;
			}
			value=0;
			count=0;
		}
		
		/**
		 * writes a single byte to the output stream aligning with the next byte boundary
		 * @param b		the byte to write
		 * @throws IOException
		 */
		public void write(byte[] b) throws IOException
		{
			flush();
			fos.write(b);
		}
		
		/**
		 * writes a single byte to the output stream aligning with the next byte boundary
		 * @param b		the byte to write
		 * @throws IOException
		 */
		public void write(byte  b) throws IOException
		{
			flush();
			fos.write(b);
		}
		
		public void write(byte[] b,int start, int count) throws IOException
		{
			flush();
			
			fos.write(b,start,count);
		}
		
		/**
		 * align the output stream with the next byte boundary
		 * @throws IOException
		 */
		
		public void flush() throws IOException
		{
			// pad out the last byte if necessary
			
			if (count>0)
			{
				masterCount+=(8-count-1);
				value=value<<(8-count);
				count=8;
				addToBuffer();
			}
			if (buffCount>0)
			{
				
				fos.write(buffer,0,buffCount);
				buffCount=0;
			}
			
		}
		
		/**
		 * close the output stream
		 * @throws IOException
		 */
		
		public void close() throws IOException
		{
			flush();
			fos.close();
		}
	}


