package TwitterVideoCompress;


/**
 * @author Nicholas Klaebe
 */

import java.io.*;

public class BitInputStream 
{
	InputStream fis;
	int count;
	long totalCount;
	byte value;
	byte[] buffer;
	int buffCount;
	int buffSize;
	boolean returnVal;	
	private static final int EOF_INT=-1;
	
	/**
	 * Constructor
	 * @param fis1		The base inputStream
	 * @param size		size of the input buffer
	 */
	public BitInputStream(InputStream fis1,int size)  
	{
		count=0;
		buffer=new byte[size];
		buffCount=0;
		totalCount=0;
		fis=fis1;
	}
		
	/**
	 * read in a single bit
	 * @return bit read
	 * @throws IOException
	 */
	public boolean read() throws IOException
	{
		if (count==0)
		{
			if (buffCount==0)
			{
				buffSize=0;
				while (buffSize==0)
				{
					buffSize=fis.read(buffer,0,buffer.length);
				}
				if (buffSize==EOF_INT) 
				{
					throw new EOFException("END OF FILE");
				} 
						 
			}
			value=buffer[buffCount];
			buffCount++;
			if (buffCount==buffSize) buffCount=0;
		}
		
		if ((value>>(7-count) &0x01)>0) returnVal=true; else returnVal=false;
			
		count++;
		totalCount++;
		if (count==8) count=0;
		
		return returnVal;
	}
	/**
	 * reads in the next char, if not on a 8bit boundary then the stream is aligned to the next byte
	 * @return byte read
	 * @throws IOException
	 */
	public char readChar() throws IOException
	{
		byte[] b= new byte[1];
		alignToNextByte();
		fis.read(b);
		totalCount+=8;
		return (char) b[0];
	}
	
	/**
	 * aligns the bit stream to the next byte boundary
	 * @throws IOException
	 */
	public void alignToNextByte() throws IOException
	{
		while (count!=0)
		{
			read();
		}
	}
	
	/**
	 * read in a specified number of bits
	 * @param bits	the number of bits to be read (between 1 and 64)
	 * @return a long integer containg the bits read
	 * @throws IOException
	 */

	public long read(int bits) throws IOException
	{
		long val=0;
		int mask=0x01;
		boolean[] bitsRead=new boolean[bits];
			
		for (int i=0;i<bits;i++)
		{
			bitsRead[i]=read();
		}
		for (int j=bits-1;j>-1;j--)
		{
				
			val=val<<1;
			if (bitsRead[j]) 
			{
				val=val|mask;
			}
		}
		return val;
	}
	/**
	 * attempts to read bytes into the array given
	 * @param temp		the array being populated
	 * @param start		the starting index
	 * @param end		the ending index
	 * @return			the bytes read in
	 * @throws IOException
	 */
	public int read(byte[] temp,int start,int end) throws IOException
	{
		totalCount+=(end-start)*8;
		return fis.read(temp,start,end);
	}
	
	public int read(byte[] temp) throws IOException
	{
		totalCount+=temp.length*8;
		return fis.read(temp);
	}
	
	public int readByte() throws IOException
	{
		totalCount+=8;
		return fis.read();
	}
	
	public void close() throws IOException
	{
		fis.close();
	}
}

