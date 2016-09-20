package TwitterVideoCompress;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
 
public class ConvertToUncompressedPNG {
 
  private static int chunkName;
  private static int width;
  private static int height;
  private static int bitDepth;
  private static int colourType;
  private static int compressionMethod;
  private static int filterMethod;
  private static int interlaceMethod;
 
  // filter types
  private static final int NONE=0;
  private static final int SUB=1;
  private static final int UP=2;
  private static final int AVG=3;
  private static final int PAETH=4;
 
  private static final int SCANLINE_WINDOW_SIZE=0; // the number of previous scanlines to use when attempting to see which filter output is most compressible.
       
        /**
        * @param args
        */
        public static void main(String[] args) throws Exception
        {
            File outputDir = new File(args[0]).getParentFile();
             
            if (new File(args[0]).isDirectory())
            {
            	outputDir=new File(args[0]+File.separator+"out");

          	  outputDir.mkdirs();

            	for (File file : new File(args[0]).listFiles())
            	{
            		uncompress(file,new File(outputDir+File.separator+file.getName()));
            	}
            }	
            else
            {
            	uncompress(new File(args[0]),new File(args[0]));
            }
        	
        }
        
        private static void uncompress(File file, File outputFile) throws Exception
        {
        	System.out.println("recompressing: "+file.getAbsolutePath());
                CRC32 crc32 = new CRC32();
 

                outputFile = new File(outputFile.getAbsolutePath()+".uncompressed.png");
               
                OutputStream os = new FileOutputStream(outputFile);
                DataOutputStream dos = new DataOutputStream(os);
               
                InputStream is = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(is);
 
                // read the PNG magic number
                long magic = dis.readLong();
                dos.writeLong(magic);
               
                String chunkType="";
                
                ByteArrayOutputStream concatenatedIDATStream = new ByteArrayOutputStream();

               
                while (!chunkType.equals("IEND"))
                {
                        // read current chunk size
                        int chunkSize = dis.readInt()+4;
                       
                        // read chunk (chunk type + data)
                        byte[] chunk = new byte[chunkSize];
                        dis.readFully(chunk);
                       
                        // read in chunk CRC
                        long crcVal = dis.readInt() & 0x00000000ffffffffL;
                       
                        chunkType = new String(chunk,0,4);
                        byte[] data = new byte[chunkSize-4];
                        System.arraycopy(chunk, 4, data, 0, chunkSize-4);
                       
                        ByteArrayOutputStream baos;
						int chunkSize1;
						byte[] chunk1;
						long crcVal1;
						switch (chunkType)
                        {
                                case "IDAT":
                                	concatenatedIDATStream.write(data);
//                                   
                                        break;
                                        
                                case "PLTE":
                                case "tRNS":
                                case "IHDR":
                                  if (chunkType.equals("IHDR"))
                                  {
                                    ByteArrayInputStream bais = new ByteArrayInputStream(chunk);
                                  
            DataInputStream headerDis = new DataInputStream(bais);
            chunkName = headerDis.readInt();
//            dos.writeInt(chunkName);
            width = headerDis.readInt();
//            dos.writeInt(width);
            height = headerDis.readInt();
//            dos.writeInt(height);
            bitDepth = headerDis.read();
//            dos.write(bitDepth);
            colourType = headerDis.read();
//            dos.write(colourType);
            compressionMethod = headerDis.read();
//            dos.write(compressionMethod);
            filterMethod = headerDis.read();
//            dos.write(filterMethod);
            interlaceMethod = headerDis.read();
//            dos.write(interlaceMethod);
 
            dos.writeInt(chunkSize-4);
            dos.write(chunk);
            dos.writeInt( (int) crcVal);
 break;
                                  }
                                case "IEND":
                                	
                                	data = concatenatedIDATStream.toByteArray();
                                 
                               // Decompress the bytes
                               Inflater decompresser = new Inflater();
                               decompresser.setInput(concatenatedIDATStream.toByteArray());
                               byte[] result = new byte[1000000];
                               int resultLength = decompresser.inflate(result);
                               decompresser.end();
                               
                               baos = new ByteArrayOutputStream();
                               baos.write("IDAT".getBytes(),0,4);
                              
//                               if (SCANLINE_WINDOW_SIZE>0)
//                               {
//                              	 result = applyBestFilters(result,resultLength);
//                              	 resultLength=result.length;
//                               }
                              
                               // Compress the bytes
                               byte[] output = new byte[1000000];
                               Deflater compresser = new Deflater(0);
                               compresser.setInput(result,0,resultLength);
                               compresser.finish();
                               int compressedDataLength = compresser.deflate(output);
                              
                               baos.write(output,0,compressedDataLength);

                                  chunkSize1=baos.size();
                                  chunk1=baos.toByteArray();
                                         
                                  crc32 = new CRC32();
                                  crc32.update(baos.toByteArray());
                                 
                                  crcVal1 =  crc32.getValue();
                                 
                                  dos.writeInt(chunkSize1-4);
                                  dos.write(chunk1);
                                  dos.writeInt( (int) crcVal1);
                                	
                                	
                                	
                                	
                                	
                                	
                                	
                                        dos.writeInt(chunkSize-4);
                                        dos.write(chunk);
                                        dos.writeInt( (int) crcVal);
                                        break;
                                default:
                                	
                                    dos.writeInt(chunkSize-4);
                                    dos.write(chunk);
                                    dos.writeInt( (int) crcVal);
                                        // ignore this chunk as it is not necessary
                                        System.out.println("Ignoring chunk of type: "+chunkType);
                        }
                }
                dos.close();
                dis.close();
        }
}