package TwitterVideoCompress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class CountPrintableUnicode
{
	static private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	static CharsetDecoder utfDecoder = Charset.forName("UTF-8").newDecoder();
	
	static CharBuffer out = CharBuffer.wrap(new char[3200]);
	
	static
	{
		utfDecoder.onMalformedInput(CodingErrorAction.REPORT);
		utfDecoder.onUnmappableCharacter(CodingErrorAction.REPORT);
	}
	
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		int validCount=0;
		for (char chr = 0; chr<Character.MAX_VALUE;chr++)
		{
			if (isPrintableChar(chr)) validCount++;
		}
		
		System.out.println(validCount);

		
//		System.out.println(generateCodes1("t".getBytes(UTF8_CHARSET)));
		System.out.println(generateCodes1(new byte[0]));
		
		


	}

	static public boolean isPrintableChar( char c ) {
	    Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
	    return (!Character.isISOControl(c)) &&
	            block != null &&
	            block != Character.UnicodeBlock.SPECIALS;
	}
	
//	public static int generateCodes(byte[] string)
//	{
//		if (string.length>6)
//		{
//			return 0;
//		}
//		
//		
////		System.out.println("*");
//		int validCount =0;
//		out:
//		for (int i=Byte.MIN_VALUE;i<=Byte.MAX_VALUE;i++)
//		{
//			if (i!=0)
//			{
//					
//	//			System.out.println(i);
//				out.clear();
//				byte[] newString = new byte[string.length+1];
//				System.arraycopy(string, 0, newString, 0,string.length);
////				newString[string.length]=(byte) i;
//				ByteBuffer bbuf = ByteBuffer.wrap(string);
//				bbuf.flip();
//				CoderResult result = utfDecoder.decode(bbuf, out, false);
//	//			System.out.println(out.toString());
//				if (result.isError() || result.isOverflow() ||
//				    result.isMalformed() ||
//				    result.isUnmappable())
//				{
//	//			    System.out.print("Cannot decode: ");
//	//			    newS
//	
//				}
//				else if (result.isUnderflow())
//				{
//					validCount+= generateCodes(newString);
//				}
//				else
//				{
//					for (int j=0;j<out.length();j++)
//					{
//						if (!isPrintableChar(out.charAt(j))) continue out;
//					}
//					
//	//			    System.out.println("Ebcdic decoded succefully ");
//					validCount+=(1 + generateCodes(newString));
//				}
//			}
//		}
//		return validCount;
//	}
	
	public static int generateCodes1(byte[] string)
	{
		if (string.length>6)
		{
			return 0;
		}
		
		int validCount=0;
		
		for (int i=Byte.MIN_VALUE;i<=Byte.MAX_VALUE;i++)
		{
			if (i!=0)
			{
				byte[] newString = new byte[string.length+1];
				System.arraycopy(string, 0, newString, 0,string.length);
				newString[string.length]=(byte) i;
			    Reader reader = Channels.newReader(Channels.newChannel(new ByteArrayInputStream(newString)), utfDecoder, -1);
			
			    final char[] buffer = new char[10];
			    int read = 0;
		        try
				{
					read = reader.read(buffer);
					
					if (read==1)
					{
		
						for (int j=0;j<read;j++)
						{
							if (!isPrintableChar(buffer[j])) 
								continue;
						}
						validCount+=(1 + generateCodes1(newString));
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}

		}
	    return validCount;
	}
}
