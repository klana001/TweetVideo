package TwitterVideoCompress;

import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
 
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
 */ 
public class JpegVideoCompressor
{
	private static final int SAMPLED_HORIZONTAL_DIMENSION = 136;//18*8;//320/2;
	private static final int SAMPLED_VERTICAL_DIMENSION = 102;//13*8;//240/2;
 
  public static void main(String[] args) throws Exception
  {
    File outputDir = new File(args[0]).getParentFile();
    
    if (new File(args[0]).isDirectory())
    {
    	outputDir=new File(args[0]+File.separator+"out");
    	
    	List<String> list = new ArrayList<String>();
    	for (File file : new File(args[0]).listFiles())
    	{
    		list.add(file.getAbsolutePath());
    	}
    	args = list.toArray(new String[0]);	
    }
    
    outputDir.mkdirs();
    FileOutputStream fos= new FileOutputStream(outputDir.getAbsolutePath()+File.separator+"raw");
    GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(outputDir.getAbsolutePath()+File.separator+"raw.gz"));
    
     boolean firstFrame=true;
    
          for (String inputFileString : args)
          {
        	  File inputFile = new File(inputFileString);
            BufferedImage firstImage = ImageIO.read( inputFile);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            BitOutputStream bos = new BitOutputStream(baos);
       
            // Down sample input image
            BufferedImage scaledImage = new BufferedImage( SAMPLED_HORIZONTAL_DIMENSION, SAMPLED_VERTICAL_DIMENSION, BufferedImage.TYPE_INT_RGB );
            scaledImage.getGraphics().drawImage(firstImage.getScaledInstance( SAMPLED_HORIZONTAL_DIMENSION, SAMPLED_VERTICAL_DIMENSION, BufferedImage.SCALE_AREA_AVERAGING ), 0,0,null);
            
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(0.01f);

            ImageOutputStream outputStream = new MemoryCacheImageOutputStream(baos); // For example implementations see below
            jpgWriter.setOutput(outputStream);
            IIOImage outputImage = new IIOImage(scaledImage, null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
            
            outputStream.close();
            
            BufferedImage decodedImage = ImageIO.read(new MemoryCacheImageInputStream(new ByteArrayInputStream(baos.toByteArray())));
            
            gos.write(baos.toByteArray());
            
//            ByteArrayOutputStream defBaos = new ByteArrayOutputStream();
//            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
//            deflater.setInput(baos.toByteArray());
//            deflater.finish(); 
//            byte[] buffer=new byte[1024];
//            while (!deflater.finished())
//            {
//                int written=deflater.deflate(buffer);
//            	defBaos.write(buffer,0,written);
//            }
//            System.out.println(defBaos.size());
            
            
            
            byte[] data = baos.toByteArray();
            fos.write(data);
      
            String encodedString = RadixEncoderDecoder.toString(baos.toByteArray());
            System.out.println(encodedString);
//           
//            // render the scaled output image
//            BufferedImage yuvImage = new BufferedImage( SAMPLED_DIMENSION, SAMPLED_DIMENSION, BufferedImage.TYPE_INT_RGB );
//            
//           
            // save the output image, resized to the input image's dimensons
            ImageIO.write( blurResize(decodedImage,firstImage.getWidth(), firstImage.getHeight()), "png",new File(outputDir.getAbsolutePath()+File.separator+inputFile.getName()+".out.scaled.png") );
//            ImageIO.write( decodedImage, "png",new File(outputDir.getAbsolutePath()+File.separator+inputFile.getName()+".out.png"));
//           
//            filters = FilterManager.load();  
//            decodeFrame(new File(outputDir+File.separator+inputFile.getName()),filters,baos.toByteArray(),previousFrameLumaFilters,previousFrameChromaFilters,firstFrame,firstImage.getWidth(),firstImage.getHeight());
//            System.out.println("Encoded String Length: "+encodedString.getBytes().length);
            
            firstFrame=false;
          }
          fos.close();
          gos.close();
  }
  
  
  /**
   * Blurs an image using a 3x3 unweighted kernel.
   * @param image the image to blur from
   * @return the blurred image
   */
  public static BufferedImage blurImage(BufferedImage image) {
//	    float[] blurKernel = {
//	    		0.075f, 0.075f, 0.075f,
//	        0.075f, 0.4f, 0.075f,
//	        0.075f, 0.075f, 0.075f
//	    };
	  
	    float[] blurKernel = {
	    		0.0375f, 0.0625f, 0.0375f,
	        0.0625f, 0.6f, 0.0625f,
	        0.0375f, 0.0625f, 0.0375f
	    };
 
    Map<Key, Object> map = new HashMap<Key, Object>();
 
    map.put(RenderingHints.KEY_INTERPOLATION,
    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 
    map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
    RenderingHints hints = new RenderingHints(map);
    BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);
    return op.filter(image, null);
  }
 
  /**
   * Resizes the input image to the given width and height.
   *
   * Algorithm:
   * 1. check to see if doubling image will exceed desired width or height, if so then goto 5
   * 2. resize the image so that it is double the width and height it was before.
   * 3. blur the image
   * 4. goto 1
   * 5. resize image to desired with and height
   *
   * @param image the image to resize
   * @param newWidth the desired width of the resized image
   * @param newHeight the desired height of the resized image
   * @return a new resized image
   */
  public static BufferedImage blurResize(BufferedImage image,int newWidth,int newHeight)
  {
 
    int width = image.getWidth();
    int height = image.getHeight();
 
    while (width*2<=newWidth && height*2<=newHeight)
    {
      BufferedImage newImage = new BufferedImage(width*2,height*2,BufferedImage.TYPE_INT_RGB);
      newImage.getGraphics().drawImage(image.getScaledInstance( width*2, height*2, Image.SCALE_AREA_AVERAGING ),0,0,null);
 
      image = blurImage(newImage);
      width = image.getWidth();
      height = image.getHeight();
    }
 
    BufferedImage newImage = new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_RGB);
    newImage.getGraphics().drawImage(image.getScaledInstance( newWidth, newHeight, Image.SCALE_AREA_AVERAGING ),0,0,null);
    return newImage;
  }
}