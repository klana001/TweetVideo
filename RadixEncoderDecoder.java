package TwitterVideoCompress;

import java.math.BigInteger;

/**
 * Licensed under Revised BSD License:
 * 
 * Copyright (c) 2014, Nicholas Klaebe
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Nicholas Klaebe nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *  @author Nicholas Klaebe 
 *
 *	This utility class can:
 *		Encode byte arrays or Big Integers into strings using character set defined by a radix
 *  or 
 *		Decode strings into byte arrays or Big Integers using character set defined by a radix
 */
public class RadixEncoderDecoder
{
	private static final int ASCII_PRINTABLE_RANGE_OFFSET = 32;
	public final static int ASCII_PRINTABLE_RANGE_RADIX = 126 - ASCII_PRINTABLE_RANGE_OFFSET + 1;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		byte[] bytes = new byte[256];
		for (int i = 0; i < 256; i++)
		{
			bytes[i] = (byte) i;
		}

		BigInteger in = new BigInteger(bytes);
		String out = toString(in, ASCII_PRINTABLE_RANGE_RADIX);
		BigInteger converted = fromString(ASCII_PRINTABLE_RANGE_RADIX,out);
		System.out.println(in);
		System.out.println(out);	
		System.out.println(converted);
		assert(in.equals(converted));
		System.out.println("Unit Test Passed");
	}
	
	/**
	 * Converts the given byte array to a string using ASCII_PRINTABLE_RANGE_RADIX
	 * @param byteArray the byte array to convert
	 * @return string representation of the byte array using the symbol set defined by ASCII_PRINTABLE_RANGE_RADIX
	 */
	public static String toString(byte[] byteArray)
	{
		// ensure that the BigInteger is interpreted as Unsigned
		return toString(new BigInteger(1,byteArray));
	}
	
	/**
	 * Converts the given string into a byte array using ASCII_PRINTABLE_RANGE_RADIX
	 * @param s the string to convert
	 * @return byte array representation of the string using the symbol set defined by ASCII_PRINTABLE_RANGE_RADIX.
	 *         Note that leading zero bytes are NOT padded to the decoded byte array.  
	 */
	public static byte[] fromString(String s)
	{
		return fromStringBigInteger(s).toByteArray();
	}
	
	/**
	 * Converts the given string into a byte array using ASCII_PRINTABLE_RANGE_RADIX
	 * @param s the string to convert
	 * @param byteArray the array to populate the decoded representation of the string using the symbol set defined by ASCII_PRINTABLE_RANGE_RADIX.
	 *                  Note that leading zero bytes are padded to the decoded byte array.  
	 */
	public static void fromString(String s, byte[] byteArray)
	{
		  byte[] decodedByteArray = fromString(s);
		  if (decodedByteArray.length>byteArray.length+1 || (decodedByteArray.length==byteArray.length+1 && decodedByteArray[0]!=0))
		  {
			  throw new RuntimeException("Decoded array size larger than array provided to populate.");
		  }
		  
		  // check for spurious leading zero byte, if found then ignore the first zero byte.
		  if (decodedByteArray.length==byteArray.length+1 && decodedByteArray[0]==0)
		  {
			  System.arraycopy(decodedByteArray, 1, byteArray, 0, byteArray.length);
		  }
		  // else add zero-padding to start array(if any)... i.e. shift the array content to the right
		  else
		  {
			  int padding = byteArray.length-decodedByteArray.length;
			  System.arraycopy(decodedByteArray, 0, byteArray, padding, decodedByteArray.length);
		  }
	}
	
	/**
	 * Converts the given byte array to a string using the provided radix
	 * @param byteArray the byte array to convert
	 * @param radix the radix of the symbol set used in the string. Radix is expected to be Char.MAX_VALUE-32;
	 * @return string representation of the byte array using the symbol set defined by the provided radix 
	 */
	public static String toString(byte[] byteArray, int radix)
	{
		return toString(new BigInteger(1,byteArray),radix);
	}
	
	/**
	 * Converts the given string into a byte array using the provided radix
	 * @param s the string to convert
	 * @param radix the radix of the symbol set used in the string
	 * @return byte array representation of the string using the symbol set defined by the provided radix 
	 */
	public static byte[] fromString(String s, int radix)
	{
		return fromString(radix,s).toByteArray();
	}
	
	/**
	 * Converts the given BigInteger to a string using ASCII_PRINTABLE_RANGE_RADIX
	 * @param bigInt the Big Integer to convert. Must be positive. You can convert a negative Big Int to unsigned by constructing: new BigInteger(1,originalBigInteger.toByteArray())
	 * @return string representation of the Big Integer using the symbol set defined by ASCII_PRINTABLE_RANGE_RADIX
	 */
	public static String toString(BigInteger bigInt)
	{
		if (bigInt.compareTo(BigInteger.ZERO) < 0)
		{
			throw new RuntimeException("Unable to convert negative big integers. You can convert a negative Big Int to unsigned by constructing: new BigInteger(1,originalBigInteger.toByteArray())");
		}
		return toString(bigInt,ASCII_PRINTABLE_RANGE_RADIX);
	}
	
	/**
	 * Converts the given string into a Big Integer using ASCII_PRINTABLE_RANGE_RADIX
	 * @param s the string to convert
	 * @return Big Integer representation of the string using the symbol set defined by ASCII_PRINTABLE_RANGE_RADIX
	 */
	public static BigInteger fromStringBigInteger(String s)
	{
		return fromString(ASCII_PRINTABLE_RANGE_RADIX,s);
	}

	/**
	 * Converts the given BigInteger to a string using the provided radix
	 * @param bigInt the Big Integer to convert
	 * @param radix the radix of the symbol set used in the string. Radix is expected to be Char.MAX_VALUE-32;
	 * @return string representation of the Big Integer using the symbol set defined by the provided radix 
	 */
	public static String toString(BigInteger bigInt, int radix)
	{
		final BigInteger radixBigInt = BigInteger.valueOf(radix);
		StringBuilder sb = new StringBuilder();

		BigInteger i = bigInt;
		BigInteger i_;

		while (i.compareTo(radixBigInt) >= 0)
		{
			i_ = i.mod(radixBigInt);
			sb.append((char) (i_.intValue() + ASCII_PRINTABLE_RANGE_OFFSET));
			i = i.divide(radixBigInt);
		}
		sb.append((char) (i.intValue() + ASCII_PRINTABLE_RANGE_OFFSET));

		// invert character order so that the the most significant symbol is first. 
		StringBuilder sb1 = new StringBuilder();
		for (int k = sb.length() - 1; k >= 0; k--)
		{
			sb1.append(sb.charAt(k));
		}

		return sb1.toString();
	}

	/**
	 * Converts the given string into a Big Integer using the provided radix
	 * @param s the string to convert
	 * @param radix the radix of the symbol set used in the string
	 * @return Big Integer representation of the string using the symbol set defined by the provided radix 
	 */
	public static BigInteger fromString(int radix, String s)
	{
		final BigInteger radixBigInt = BigInteger.valueOf(radix);
		BigInteger result = BigInteger.ZERO;
		int i = 0, len = s.length();
		int digit;

		while (i < len)
		{
			digit = s.charAt(i++) - ASCII_PRINTABLE_RANGE_OFFSET;
			result = result.multiply(radixBigInt);
			result = result.subtract(BigInteger.valueOf(digit));
		}

		// ensure that the big integer is interpreted as unsigned!
		return new BigInteger(1,result.negate().toByteArray());
	}
}
