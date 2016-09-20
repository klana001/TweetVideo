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

import javax.imageio.ImageIO;
 
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
public class TwitterVideoCompressor
{
//1 2 3 4 6 8 12 16 24 32 48 96 
		static private final Random rand = new Random(2);

  static private final int IMAGE_DIMENSION_BITS=13;
  static private final int LUMA_BLOCK_SIZE=4; //4 //4 //4 //4 //3
  static private final int LUMA_SAMPLED_DIMENSION_BLOCKS=24;//18 //30 //16 //16 //8
  static private final int LUMA_SAMPLED_DIMENSION=LUMA_BLOCK_SIZE*LUMA_SAMPLED_DIMENSION_BLOCKS;
  static private final int SAMPLED_DIMENSION = LUMA_SAMPLED_DIMENSION;
 
  static private final int LUMA_FILTER_BIT_DEPTH=13;//16  //16 //12 //12
  static private final int LUMA_FILTER_COUNT=(int) Math.pow( 2, LUMA_FILTER_BIT_DEPTH );
  static private final int LUMA_BLOCK_SAMPLE_SIZE=LUMA_FILTER_COUNT*10; //10000
  static private final int LUMA_FILTERS_IMAGE_DIMENSION = (int) (Math.ceil(Math.sqrt(LUMA_FILTER_COUNT)));
 
  static private final int CHROMA_BLOCK_SIZE=8; //8 //8 //8 //6
  static private final int CHROMA_SAMPLED_DIMENSION_BLOCKS=12; //15 //8 //8 //4
  static private final int CHROMA_SAMPLED_DIMENSION=CHROMA_BLOCK_SIZE*CHROMA_SAMPLED_DIMENSION_BLOCKS;
  static private final int CHROMA_FILTER_BIT_DEPTH=7;//10 //10 //7 //7
  static private final int CHROMA_FILTER_COUNT=(int) Math.pow( 2, CHROMA_FILTER_BIT_DEPTH );
  static private final int CHROMA_BLOCK_SAMPLE_SIZE=CHROMA_FILTER_COUNT*10; //10000
  static private final int CHROMA_FILTERS_IMAGE_DIMENSION = (int) (Math.ceil(Math.sqrt(CHROMA_FILTER_COUNT)));


  private static final int SIMILARITY_BIT_DEPTH = 2;//5;
  private static final int SIMILARITY_COUNT = (int) Math.pow( 2, SIMILARITY_BIT_DEPTH );
  
//  static private final int IMAGE_DIMENSION_BITS=13;
//  static private final int LUMA_BLOCK_SIZE=4; //4 //4 //4 //4 //3
//  static private final int LUMA_SAMPLED_DIMENSION_BLOCKS=18;//18 //30 //16 //16 //8
//  static private final int LUMA_SAMPLED_DIMENSION=LUMA_BLOCK_SIZE*LUMA_SAMPLED_DIMENSION_BLOCKS;
//  static private final int SAMPLED_DIMENSION = LUMA_SAMPLED_DIMENSION;
// 
//  static private final int LUMA_FILTER_BIT_DEPTH=13;//16  //16 //12 //12
//  static private final int LUMA_FILTER_COUNT=(int) Math.pow( 2, LUMA_FILTER_BIT_DEPTH );
//  static private final int LUMA_BLOCK_SAMPLE_SIZE=LUMA_FILTER_COUNT*10; //10000
//  static private final int LUMA_FILTERS_IMAGE_DIMENSION = (int) (Math.ceil(Math.sqrt(LUMA_FILTER_COUNT)));
// 
//  static private final int CHROMA_BLOCK_SIZE=8; //8 //8 //8 //6
//  static private final int CHROMA_SAMPLED_DIMENSION_BLOCKS=9; //15 //8 //8 //4
//  static private final int CHROMA_SAMPLED_DIMENSION=CHROMA_BLOCK_SIZE*CHROMA_SAMPLED_DIMENSION_BLOCKS;
//  static private final int CHROMA_FILTER_BIT_DEPTH=7;//10 //10 //7 //7
//  static private final int CHROMA_FILTER_COUNT=(int) Math.pow( 2, CHROMA_FILTER_BIT_DEPTH );
//  static private final int CHROMA_BLOCK_SAMPLE_SIZE=CHROMA_FILTER_COUNT*10; //10000
//  static private final int CHROMA_FILTERS_IMAGE_DIMENSION = (int) (Math.ceil(Math.sqrt(CHROMA_FILTER_COUNT)));
//
//
//  private static final int SIMILARITY_BIT_DEPTH = 2;//5;
//  private static final int SIMILARITY_COUNT = (int) Math.pow( 2, SIMILARITY_BIT_DEPTH );
  
  static private final int COMPRESSED_IMAGE_BYTE_STREAM_SIZE=(int) Math.ceil((1D/8)*(IMAGE_DIMENSION_BITS*2+LUMA_FILTER_BIT_DEPTH*LUMA_SAMPLED_DIMENSION_BLOCKS*LUMA_SAMPLED_DIMENSION_BLOCKS+CHROMA_FILTER_BIT_DEPTH*CHROMA_SAMPLED_DIMENSION_BLOCKS*CHROMA_SAMPLED_DIMENSION_BLOCKS));
 
  static private final int LUMA=0;
  static private final int CHROMA_U=1;
  static private final int CHROMA_V=2;
 
  static private final float EIGHT_BIT_DIVISOR=1.0F/256;

  private static final boolean CLUSTER = false;

 
//  public static class ForkCluster extends RecursiveAction {
//   
//    public static class Results
//    {
//      public static class Result
//      {
//        int blockSwapClusterCount;
//        float bestDifference;
//        int bestFilterIndex;
//      }
//      List<Result> results = Collections.synchronizedList( new ArrayList<Result>() );
//    }
//   
//   
//    final private Block[] samples;
//    final private Block[] filters;
//    final private int blockSampleSize;
//    final private Results results;
// 
//    public ForkCluster(Block[] samples, Block[] filters, int blockSampleSize, Results results) {
//        this.samples=samples;
//        this.filters=filters;
//        this.blockSampleSize=blockSampleSize;
//        this.results=results;
//    }
//
//    protected void computeDirectly() {
//   
//    // identify closest cluster mid point for each sample block
//    for (int i=0;i<blockSampleSize;i++)
//    {
//      Block block = samples[i];
//      float bestDifference=float.MAX_VALUE;
//      int bestFilterIndex=-1;
//     
//      for (int j=0;j<filterCount;j++)
//      {
//        float difference = comparator.calculateDifference( block, filters[j] );
//       
//        if (difference<bestDifference)
//        {
//          bestDifference=difference;
//          bestFilterIndex=j;
//        }
//      }
//     
//      // sample block has changed to a different cluster or the assigned cluster's collection has been updated
//      if (bestDifference!=block.closestDifference || bestFilterIndex!=block.closestMatchingFilterId)
//      {
//        blockSwapClusterCount++;
//        converged=false;
//        block.closestMatchingFilterId=bestFilterIndex;
//        block.closestDifference=bestDifference;
//      }
//    }
//      Results.Result result = new Results.Result();
//      {
//        result.bestDifference=b
//      }
//    }
//   
//    protected void compute() {
//      if (mLength < sThreshold) {
//          computeDirectly();
//          return;
//      }
//     
//      int split = mLength / 2;
//     
//      invokeAll(new ForkBlur(mSource, mStart, split, mDestination),
//                new ForkBlur(mSource, mStart + split, mLength - split,
//                             mDestination));
//  }
//  }
 
  static final class FilterManager
  {
	Block[] lumaFilters;
	  Block[] chromaFilters;
	  int[][] lumaFilterSimilarityOrdering;
	  int[][] chromaFilterSimilarityOrdering;
	  
	  private void loadFilters() throws IOException
	  {
		  lumaFilters= new Block[LUMA_FILTER_COUNT];
		  chromaFilters= new Block[CHROMA_FILTER_COUNT];
	    BufferedImage filtersImage=ImageIO.read( new File("data/images/working/lumaFilters.png") );
	    for (int i=0;i<LUMA_FILTER_COUNT;i++)
	    {
	      float[][][] pixels = convertToYUVImage(filtersImage.getSubimage( (i%LUMA_FILTERS_IMAGE_DIMENSION)*(LUMA_BLOCK_SIZE+2), (i/LUMA_FILTERS_IMAGE_DIMENSION)*(LUMA_BLOCK_SIZE+2), LUMA_BLOCK_SIZE, LUMA_BLOCK_SIZE ));
	      lumaFilters[i]=new Block( pixels, 0, 0 ,LUMA_BLOCK_SIZE);
	      lumaFilters[i].closestMatchingFilterId=i;
	    }
	 
	    filtersImage=ImageIO.read( new File("data/images/working/chromaFilters.png") );
	    for (int i=0;i<CHROMA_FILTER_COUNT;i++)
	    {
	      float[][][] pixels = convertToYUVImage(filtersImage.getSubimage( (i%CHROMA_FILTERS_IMAGE_DIMENSION)*(CHROMA_BLOCK_SIZE+2), (i/CHROMA_FILTERS_IMAGE_DIMENSION)*(CHROMA_BLOCK_SIZE+2), CHROMA_BLOCK_SIZE, CHROMA_BLOCK_SIZE ));
	      chromaFilters[i]=new Block( pixels, 0, 0 ,CHROMA_BLOCK_SIZE);
	      chromaFilters[i].closestMatchingFilterId=i;
	    }
	 
	 
	  }
	  

		private void loadSimilarityOrdering() throws IOException
		{
			  DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File("data/images/working/ordering.dat") )));
			  
			  lumaFilterSimilarityOrdering = new int[LUMA_FILTER_COUNT][];
			  for (int i=0;i<lumaFilterSimilarityOrdering.length;i++)
			  {
				  lumaFilterSimilarityOrdering[i]=new int[SIMILARITY_COUNT];
				  for (int j=0;j<SIMILARITY_COUNT;j++)
				  {
					  lumaFilterSimilarityOrdering[i][j]= dis.readInt();
				  }
			  }
			  
			  chromaFilterSimilarityOrdering = new int[CHROMA_FILTER_COUNT][];
			  for (int i=0;i<chromaFilterSimilarityOrdering.length;i++)
			  {
				  chromaFilterSimilarityOrdering[i]=new int[SIMILARITY_COUNT];
				  for (int j=0;j<SIMILARITY_COUNT;j++)
				  {
					  chromaFilterSimilarityOrdering[i][j]= dis.readInt();
				  }
			  }
			  dis.close();
		}
	  
	  private void performSimilarityOrdering() throws IOException
	  {
		  System.out.println("Creating similarity ordered Lists");
		  lumaFilterSimilarityOrdering = new int[LUMA_FILTER_COUNT][];
		  
		  for (int i=0;i<lumaFilterSimilarityOrdering.length;i++)
		  {
			  lumaFilterSimilarityOrdering[i]=order(lumaFilters,lumaFilters[i],LumaComparator.comparator);
		  }
		  
		  chromaFilterSimilarityOrdering = new int[CHROMA_FILTER_COUNT][];
		  
		  for (int i=0;i<chromaFilterSimilarityOrdering.length;i++)
		  {
			  chromaFilterSimilarityOrdering[i]=order(chromaFilters,chromaFilters[i],ChromaComparator.comparator);
		  }
		  
		  DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File("data/images/working/ordering.dat") )));
		  
		  for (int i=0;i<lumaFilterSimilarityOrdering.length;i++)
		  {
			  for (int j=0;j<SIMILARITY_COUNT;j++)
			  {
				  dos.writeInt(lumaFilterSimilarityOrdering[i][j]);
			  }
		  }
		  
		  for (int i=0;i<chromaFilterSimilarityOrdering.length;i++)
		  {
			  for (int j=0;j<SIMILARITY_COUNT;j++)
			  {
				  dos.writeInt(chromaFilterSimilarityOrdering[i][j]);
			  }
		  }
		  dos.close();
		  System.out.println("Creating similarity ordered Lists - Done");
	  }
	  
	  /**
	   * Performs similarity ordering of the given filter type as compared with the given filter.
	   * @return array of similarity ordered indices into the filter list.
	   */
	  static private int[] order(Block[] filters,final Block comparisionFilter,final BlockComparator comparator)
	  {
		  final class Tuple implements Comparable<Tuple>
		  {
			  public Tuple(float difference, int id) {
				this.difference = difference;
				this.id = id;
			}

			float difference;
			  int id;
		    
			@Override
			public int compareTo(Tuple other) {
				// TODO Auto-generated method stub
				float diff = difference-other.difference;
				return diff<0?-1:(diff>0?1:0);
			}
			  
		  }
		  
		  ArrayList<Tuple> tuples = new ArrayList<>();
		  for (int i=0;i<filters.length;i++)
		  {
			  tuples.add(new Tuple(comparator.calculateDifference(filters[i], comparisionFilter),i));
		  }
		  Collections.sort(tuples);
		  
		  int[] orderedIndecies = new int[SIMILARITY_COUNT];
		  
		  for (int i=0;i<SIMILARITY_COUNT;i++)
		  {
			  orderedIndecies[i]=tuples.get(i).id;
		  }

		  return orderedIndecies;
	  }
	 
	  private void generateFilters() throws IOException
	  {
	    File dir = new File("data/images");
	 
	    List<float[][][]> sampleImages = new ArrayList<float[][][]>();
	    BufferedImage sampleImage = new BufferedImage( LUMA_SAMPLED_DIMENSION, LUMA_SAMPLED_DIMENSION, BufferedImage.TYPE_INT_RGB );
	 
	    // iterate over sample images and convert and down sample them into YUV images
	    for (File file : dir.listFiles())
	    {
	      if (file.isFile())
	      {
	        BufferedImage rawImage = ImageIO.read( file);
	        if (rawImage!=null)
	        {
	        	sampleImage.getGraphics().drawImage(rawImage.getScaledInstance( LUMA_SAMPLED_DIMENSION, LUMA_SAMPLED_DIMENSION, BufferedImage.SCALE_AREA_AVERAGING ), 0,0,null);
	        }
	        float[][][] pixels = convertToYUVImage(sampleImage);
	        sampleImages.add(pixels);
	      }
	    }
	 
	    // perform k-clustering for lumenosity and 'chromanosity'
	    lumaFilters = clusterLuma(sampleImages);
	    chromaFilters =new Block[0];
	    
	    if (!(CHROMA_FILTER_COUNT==0 || CHROMA_SAMPLED_DIMENSION == 0))
	    {
	    	chromaFilters =clusterChroma(sampleImages);
	    }

	 
	    // construct and save clustered lumenosity filters image
	    BufferedImage lumaFiltersImage = new BufferedImage( LUMA_FILTERS_IMAGE_DIMENSION*(LUMA_BLOCK_SIZE+2), LUMA_FILTERS_IMAGE_DIMENSION*(LUMA_BLOCK_SIZE+2), BufferedImage.TYPE_INT_RGB );
	    BufferedImage lumaFilterImage = new BufferedImage( LUMA_BLOCK_SIZE, LUMA_BLOCK_SIZE, BufferedImage.TYPE_INT_RGB );
	 
	    for (int i=0;i<lumaFilters.length;i++)
	    {
	      renderYUV(lumaFilterImage,lumaFilters[i].pixels);
	      lumaFiltersImage.getGraphics().drawImage(lumaFilterImage,(i%LUMA_FILTERS_IMAGE_DIMENSION)*(LUMA_BLOCK_SIZE+2), (i/LUMA_FILTERS_IMAGE_DIMENSION)*(LUMA_BLOCK_SIZE+2),null);
	    }
	    new File("data/images/working").mkdirs();
	    ImageIO.write( lumaFiltersImage, "png", new File("data/images/working/lumaFilters.png") );
	 
	    // construct and save clustered 'chromanosity' filters image
	    BufferedImage chromafiltersImage = new BufferedImage( CHROMA_FILTERS_IMAGE_DIMENSION*(CHROMA_BLOCK_SIZE+2), CHROMA_FILTERS_IMAGE_DIMENSION*(CHROMA_BLOCK_SIZE+2), BufferedImage.TYPE_INT_RGB );
	    BufferedImage chromafilterImage = new BufferedImage( CHROMA_BLOCK_SIZE, CHROMA_BLOCK_SIZE, BufferedImage.TYPE_INT_RGB );
	 
	    for (int i=0;i<chromaFilters.length;i++)
	    {
	         renderYUV(chromafilterImage,chromaFilters[i].pixels);
	      chromafiltersImage.getGraphics().drawImage(chromafilterImage,(i%CHROMA_FILTERS_IMAGE_DIMENSION)*(CHROMA_BLOCK_SIZE+2), (i/CHROMA_FILTERS_IMAGE_DIMENSION)*(CHROMA_BLOCK_SIZE+2),null);
	    }
	    ImageIO.write( chromafiltersImage, "png", new File("data/images/working/chromaFilters.png") );
	 
	    
	    // generate similarity filter ordering lists 
	    

	  }
	 
	  /**
	   * Performs K-clustering of samples blocks to produce an array of filter blocks.
	   * @param sampleImages the collection of YUV down sampled sample images to obtain random blocks from
	   * @param sampledDimension the dimension of the down sampled image  (width == height)
	   * @param blockSize the dimension of the filter block (width == height)
	   * @param filterCount the number of filters to cluster for
	   * @param blockSampleSize the number of blocks to cluster
	   * @param comparator the comparator to use when determining magnitude of difference between two blocks
	   * @return array of filters
	   */
	  static private Block[] cluster(List<float[][][]> sampleImages,final int sampledDimension,final int blockSize, final int filterCount,final int blockSampleSize,BlockComparator comparator)
	  {
	    Block[] filters = new Block[filterCount];
	 
	    Block[] samples = new Block[blockSampleSize];
	    for (int i=0;i<blockSampleSize;i++)
	    {
	      samples[i]= new Block(sampleImages.get( rand.nextInt(sampleImages.size())),rand.nextInt(sampledDimension-blockSize),rand.nextInt(sampledDimension-blockSize),blockSize);
	    }
	 
	    // select initial filters
	    for (int i=0;i<filterCount;i++)
	    {
	      filters[i] = new Block(samples[i]);
	      filters[i].closestMatchingFilterId=i;
	    }
	    
	    if (CLUSTER)
	    {
		 
		    int iteration=0;
		    boolean converged=false;
		    long stopTime=System.currentTimeMillis()+1000*60*450;
		 
		    // iterate until all blocks have converged into clusters or timeout occurs
		    while (!converged && System.currentTimeMillis()<stopTime)
		    {
		      iteration++;
		      System.out.print("iteration: "+iteration);
		 
		      converged = true;
		      int blockSwapClusterCount=0;
		 
		//      ForkJoinPool pool = new ForkJoinPool();
		//     
		//      pool.invoke( RecursiveAction )
		 
		      // identify closest cluster mid point for each sample block
		      for (int i=0;i<blockSampleSize;i++)
		      {
		        Block block = samples[i];
		        float bestDifference=Float.MAX_VALUE;
		        int bestFilterIndex=-1;
		 
		        for (int j=0;j<filterCount;j++)
		        {
		          float difference = comparator.calculateDifference( block, filters[j] );
		 
		          if (difference<bestDifference)
		          {
		            bestDifference=difference;
		            bestFilterIndex=j;
		          }
		        }
		 
		        // sample block has changed to a different cluster or the assigned cluster's collection has been updated
		        if (bestDifference!=block.closestDifference || bestFilterIndex!=block.closestMatchingFilterId)
		        {
		          blockSwapClusterCount++;
		          converged=false;
		          block.closestMatchingFilterId=bestFilterIndex;
		          block.closestDifference=bestDifference;
		        }
		      }
		 
		      if (!converged)
		      {
		        int orphanedClusterCount=0;
		        // determine new cluster centre (i.e. filter)
		        for (int j=0;j<filterCount;j++)
		        {
		          int clusterSize=0;
		          Block filter = filters[j];
		          filter.zeroise();
		            for (int i=0;i<blockSampleSize;i++)
		            {
		              if (samples[i].closestMatchingFilterId==j)
		              {
		                clusterSize++;
		                filter.add(samples[i]);
		              }
		            }
		 
		            // the cluster no longer has any members... lets choose another random sample to be our this cluster centre
		            if (clusterSize==0)
		            {
		              orphanedClusterCount++;
		              filter = new Block( samples[rand.nextInt( blockSampleSize )] );
		              filter.closestMatchingFilterId=j;
		              filters[j]=filter;
		            }
		            else
		            {
		              filter.divide(clusterSize);
		            }
		 
		        }
		 
		        if (orphanedClusterCount>0)
		        {
		          System.out.print(" orphanedClusterCount: "+orphanedClusterCount);
		        }       
		      }
		 
		      if (blockSwapClusterCount>0)
		      {
		        System.out.print(" blockSwapClusterCount: "+blockSwapClusterCount);
		      }
		      System.out.println();
		    }
		    System.out.println("Clustering complete");
	    }
	    return filters;
	  }
	 
	  /**
	   * Perform k-clustering based on pixel lumenosity to derive lumenosity filters
	   * @param sampleImages the collection of YUV down sampled sample images to obtain random blocks from
	   * @return array of filters
	   */
	  static private Block[] clusterLuma(List<float[][][]> sampleImages)
	  {
	    return cluster(sampleImages,LUMA_SAMPLED_DIMENSION,LUMA_BLOCK_SIZE,LUMA_FILTER_COUNT,LUMA_BLOCK_SAMPLE_SIZE,LumaComparator.comparator);
	  }
	 
	  /**
	   * Perform k-clustering based on pixel 'chromanosity' to derive 'chromanosity' filters
	   * @param sampleImages the collection of YUV down sampled sample images to obtain random blocks from
	   * @return array of filters
	   */
	  static private Block[] clusterChroma(List<float[][][]> sampleImages)
	  {
	    return cluster(sampleImages,CHROMA_SAMPLED_DIMENSION,CHROMA_BLOCK_SIZE,CHROMA_FILTER_COUNT,CHROMA_BLOCK_SAMPLE_SIZE,ChromaComparator.comparator);
	  }

	public static FilterManager create() throws IOException 
	{
		FilterManager filterManager = new FilterManager();
		filterManager.generateFilters();
		filterManager.performSimilarityOrdering();
		return filterManager;
	}
	
	public static FilterManager load() throws IOException 
	{
		FilterManager filterManager = new FilterManager();
		filterManager.loadFilters();
		filterManager.loadSimilarityOrdering();
		return filterManager;
	}

  }
  
  /**
   *  A square block holding YUV pixels
   */
  public static final class Block
  {
    float[][][] pixels ;
 
    int closestMatchingFilterId=-1;
    float closestDifference=-1;
    final int blockSize;
 
    public Block(float[][][] sourceImage, int x, int y,final int blockSize)
    {
      this.blockSize=blockSize;
      pixels = new float[blockSize][blockSize][3];
      for (int i=0;i<blockSize;i++)
      {
         for (int j=0;j<blockSize;j++)
         {
           float[] pixel = sourceImage[x+j][y+i];
           pixels[j][i][LUMA]=pixel[LUMA];
           pixels[j][i][CHROMA_U]=pixel[CHROMA_U];
           pixels[j][i][CHROMA_V]=pixel[CHROMA_V];
         }
      }
    }
 
    /**
     * Copy Contstructor
     * @param other
     */
    public Block (Block other)
    {
      this.blockSize=other.blockSize;
      pixels = new float[blockSize][blockSize][3];
      for (int i=0;i<blockSize;i++)
      {
         for (int j=0;j<blockSize;j++)
         {
           pixels[j][i][LUMA]=other.pixels[j][i][LUMA];
           pixels[j][i][CHROMA_U]=other.pixels[j][i][CHROMA_U];
           pixels[j][i][CHROMA_V]=other.pixels[j][i][CHROMA_V];
         }
      }
    }
 
    /**
     * sets all pixel colour components to 0
     */
    public void zeroise()
    {
      pixels = new float[blockSize][blockSize][3];
    }
 
    /**
     * adds each pixel's colour components to this block's corresponding pixel colour components
     * @param other the other block to add
     */
    public void add(Block other)
    {
        for (int i=0;i<blockSize;i++)
        {
           for (int j=0;j<blockSize;j++)
           {
             pixels[j][i][LUMA]+=other.pixels[j][i][LUMA];
             pixels[j][i][CHROMA_U]+=other.pixels[j][i][CHROMA_U];
             pixels[j][i][CHROMA_V]+=other.pixels[j][i][CHROMA_V];
           }
        }
    }
 
    /**
     * divides all pixels colour components by the provided divisor
     * @param divisor
     */
    public void divide(float divisor)
    {
        for (int i=0;i<blockSize;i++)
        {
           for (int j=0;j<blockSize;j++)
           {
             pixels[j][i][LUMA]/=divisor;
             pixels[j][i][CHROMA_U]/=divisor;
             pixels[j][i][CHROMA_V]/=divisor;
           }
        }
    }
 
  }
 
  /**
   * Abstract Class providing the method signature to calculate the difference between blocks
   */
  public static abstract class BlockComparator
  {
    /**
     * Calculates and returns the magnitude of difference between the two provided blocks. The result has only has meaning when comparing against other results obtained by calling this method.
     * @param block1
     * @param block2
     * @return the magnitude of difference between the two provided blocks
     */
    abstract public float calculateDifference( Block block1,Block block2 );
  }
 
  /**
   * Concrete implementation of BlockComparator with the difference calculated based on lumenosity
   */
  public static abstract class LumaComparator extends BlockComparator
  {
    public static final LumaComparator comparator = new LumaComparator(){
      public  float calculateDifference( Block block1,Block block2 )
      {
        float diffMetric = 0;
        for ( int i = 0; i < LUMA_BLOCK_SIZE; i++ )
        {
          for ( int j = 0; j < LUMA_BLOCK_SIZE; j++ )
          {
            diffMetric+=(block1.pixels[j][i][LUMA]-block2.pixels[j][i][LUMA])*(block1.pixels[j][i][LUMA]-block2.pixels[j][i][LUMA]);
          }
        }
        return diffMetric;
      }
    };
  }
 
  /**
   * Concrete implementation of BlockComparator with the difference calculated based on 'chromanosity'
   */
  public static abstract class ChromaComparator extends BlockComparator
  {
    public static final ChromaComparator comparator = new ChromaComparator(){
      public  float calculateDifference( Block block1,Block block2 )
      {
        float diff;
        float diffMetric = 0;
        for ( int i = 0; i < CHROMA_BLOCK_SIZE; i++ )
        {
          for ( int j = 0; j < CHROMA_BLOCK_SIZE; j++ )
          {
            diff=(block1.pixels[j][i][CHROMA_U]-block2.pixels[j][i][CHROMA_U]);
            diffMetric+=diff*diff*diff*diff;
            diff=(block1.pixels[j][i][CHROMA_V]-block2.pixels[j][i][CHROMA_V]);
            diffMetric+=diff*diff*diff*diff;
          }
        }
        return diffMetric;
      }
    };
  }
 
 
  public static void main(String[] args) throws Exception
  {
	  FilterManager filters = FilterManager.create();
//    Block[][] filters = loadFilters();    
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
    
    int[][] previousFrameChromaFilters = new int[SAMPLED_DIMENSION][SAMPLED_DIMENSION];
    int[][] previousFrameLumaFilters = new int[SAMPLED_DIMENSION][SAMPLED_DIMENSION];
    boolean firstFrame=true;
    
          for (String inputFileString : args)
          {
        	  File inputFile = new File(inputFileString);
            BufferedImage firstImage = ImageIO.read( inputFile);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BitOutputStream bos = new BitOutputStream(baos);
       
            // Down sample input image
            BufferedImage scaledImage = new BufferedImage( SAMPLED_DIMENSION, SAMPLED_DIMENSION, BufferedImage.TYPE_INT_RGB );
            scaledImage.getGraphics().drawImage(firstImage.getScaledInstance( SAMPLED_DIMENSION, SAMPLED_DIMENSION, BufferedImage.SCALE_AREA_AVERAGING ), 0,0,null);
            float[][][] pixels = convertToYUVImage(scaledImage);
           
            // save image dimensions
            
            if (firstFrame)
            {
            	bos.write(firstImage.getWidth(),IMAGE_DIMENSION_BITS);
            	bos.write(firstImage.getHeight(),IMAGE_DIMENSION_BITS);
            }
           
       
            // match sampled image blocks' lumenosity to filters...
            float[][][] categorisedPixels = new float[LUMA_SAMPLED_DIMENSION][LUMA_SAMPLED_DIMENSION][3];
           
            for (int i=0;i<LUMA_SAMPLED_DIMENSION;i+=LUMA_BLOCK_SIZE)
            {
              for (int j=0;j<LUMA_SAMPLED_DIMENSION;j+=LUMA_BLOCK_SIZE)
              {
                Block block = new Block( pixels, j, i,LUMA_BLOCK_SIZE);
                Block leastDifferenceLumaFilter= null;
                float leastDifference=Float.MAX_VALUE;
                float difference;
                for (Block filter : filters.lumaFilters)
                {
                  difference = LumaComparator.comparator.calculateDifference( filter, block );
                 
                  if (difference<leastDifference)
                  {
                    leastDifference=difference;
                    leastDifferenceLumaFilter=filter;
                  }
                }
                
                // save luma block
                if (firstFrame)
                {
                	bos.write(leastDifferenceLumaFilter.closestMatchingFilterId,LUMA_FILTER_BIT_DEPTH);
                }
                else
                out:
                {
                	int[] lumaFilterSimilarityOrdering = filters.lumaFilterSimilarityOrdering[previousFrameLumaFilters[i][j]];
                	for (int k=0;k<SIMILARITY_COUNT;k++)
                	if (lumaFilterSimilarityOrdering[k]==leastDifferenceLumaFilter.closestMatchingFilterId)
                	{
                		bos.write(true);
                		bos.write(k,SIMILARITY_BIT_DEPTH);
                		break out;
                	}
                	bos.write(false);
                	bos.write(leastDifferenceLumaFilter.closestMatchingFilterId,LUMA_FILTER_BIT_DEPTH);
                }
                previousFrameLumaFilters[i][j]=leastDifferenceLumaFilter.closestMatchingFilterId;
               
               
                // copy matched filter's lumenosity into corresponding output image's block
                for (int y=0;y<LUMA_BLOCK_SIZE;y++)
                {
                  for (int x=0;x<LUMA_BLOCK_SIZE;x++)
                  {
                    categorisedPixels[j+x][i+y][LUMA]=leastDifferenceLumaFilter.pixels[x][y][LUMA];
       
                  }
                }
              }
            }
           
            // match sampled image blocks' 'chromanosity' to filters...
            for (int i=0;i<CHROMA_SAMPLED_DIMENSION;i+=CHROMA_BLOCK_SIZE)
            {
              for (int j=0;j<CHROMA_SAMPLED_DIMENSION;j+=CHROMA_BLOCK_SIZE)
              {
                Block block = new Block( pixels, j, i ,CHROMA_BLOCK_SIZE);
                
                Block leastDifferenceChromaFilter= null;
                float leastDifference=Float.MAX_VALUE;
                float difference;
                for (Block filter : filters.chromaFilters)
                {
                  difference = ChromaComparator.comparator.calculateDifference( filter, block );
                 
                  if (difference<leastDifference)
                  {
                    leastDifference=difference;
                    leastDifferenceChromaFilter=filter;
                  }
                }
                
                // save chroma block
                if (firstFrame)
                {
                	bos.write(leastDifferenceChromaFilter.closestMatchingFilterId,CHROMA_FILTER_BIT_DEPTH);
                }
                else
                out:
                {
                	int[] chromaFilterSimilarityOrdering = filters.chromaFilterSimilarityOrdering[previousFrameChromaFilters[i][j]];
                	for (int k=0;k<SIMILARITY_COUNT;k++)
                	if (chromaFilterSimilarityOrdering[k]==leastDifferenceChromaFilter.closestMatchingFilterId)
                	{
                		bos.write(true);
                		bos.write(k,SIMILARITY_BIT_DEPTH);
                		break out;
                	}
                	bos.write(false);
                	bos.write(leastDifferenceChromaFilter.closestMatchingFilterId,CHROMA_FILTER_BIT_DEPTH);
                }
                previousFrameChromaFilters[i][j]=leastDifferenceChromaFilter.closestMatchingFilterId;
       
                // copy matched filter's 'chromanosity' into corresponding output image's block
                for (int y=0;y<CHROMA_BLOCK_SIZE;y++)
                {
                  for (int x=0;x<CHROMA_BLOCK_SIZE;x++)
                  {
                    categorisedPixels[j+x][i+y][CHROMA_U]=leastDifferenceChromaFilter.pixels[x][y][CHROMA_U];
                    categorisedPixels[j+x][i+y][CHROMA_V]=leastDifferenceChromaFilter.pixels[x][y][CHROMA_V];
                  }
                }
              }
            }

            
            bos.close();
            gos.write(baos.toByteArray());
            
            ByteArrayOutputStream defBaos = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(baos.toByteArray());
            deflater.finish(); 
            byte[] buffer=new byte[1024];
            while (!deflater.finished())
            {
                int written=deflater.deflate(buffer);
            	defBaos.write(buffer,0,written);
            }
            System.out.println(defBaos.size());
            
            
            
            byte[] data = baos.toByteArray();
            fos.write(data);
//            int pairCount=0;
//            for (int i=0;i<data.length-1;i++)
//            {
//            	if (data[i]==data[i+1])
//            	{
//            		pairCount++;
//            	}
//            }
//            System.out.println(pairCount);
            	
//            //String str = "0VW*`Gnyq;c1JBY}tj#rOcKm)v_Ac\\S.r[>,Xd_(qT6 >]!xOfU9~0jmIMG{hcg-'*a.s<X]6*%U5>/FOze?cPv@hI)PjpK9\\iA7P ]a-7eC&ttS[]K>NwN-^$T1E.1OH^c0^\"J 4";
//            String str = "@4YZIS=1bIGvfc;/]n.)C6s9[v}[WOCo'g@~=+5Lj^?[(x+@[KNBA!4:w$dD7Yd[r%3%\"kT+\"iB>VM,@DS|u)4h9B$32*C'slrTpvZ1#e|2\\:}~O)mYL4UA`A@|3BsZc*Sy\\fOqrl+j";
            
            String encodedString = RadixEncoderDecoder.toString(baos.toByteArray());
            System.out.println(encodedString);
//           
            // render the scaled output image
            BufferedImage yuvImage = new BufferedImage( SAMPLED_DIMENSION, SAMPLED_DIMENSION, BufferedImage.TYPE_INT_RGB );
            renderYUV(yuvImage,categorisedPixels);
           
            // save the output image, resized to the input image's dimensons
//            ImageIO.write( blurResize(yuvImage,firstImage.getWidth(), firstImage.getHeight()), "png",new File(inputFileName+".out.png") );
//            ImageIO.write( yuvImage, "png",new File(outputDir+inputFile.getName() );
//           
            filters = FilterManager.load();  
            decodeFrame(new File(outputDir+File.separator+inputFile.getName()),filters,baos.toByteArray(),previousFrameLumaFilters,previousFrameChromaFilters,firstFrame,firstImage.getWidth(),firstImage.getHeight());
//            System.out.println("Encoded String Length: "+encodedString.getBytes().length);
            
            firstFrame=false;
          }
          fos.close();
          gos.close();
  }
  
  static void decodeFrame(File outputFile, FilterManager filters, byte[] decodedByteArray,int[][] previousFrameLumaFilters, int[][] previousFrameChromaFilters,boolean firstFrame,int width,int height) throws IOException
  {
//	  byte[] decodedByteArray = new byte[COMPRESSED_IMAGE_BYTE_STREAM_SIZE];
//	  RadixEncoderDecoder.fromString(encodedString,decodedByteArray);
	  
	  ByteArrayInputStream bais = new ByteArrayInputStream(decodedByteArray);
	  BitInputStream bis = new BitInputStream(bais, 8);
	  
	  if (firstFrame)
	  {
		  width = (int) bis.read(IMAGE_DIMENSION_BITS);
		  height = (int) bis.read(IMAGE_DIMENSION_BITS);
	  }
      float[][][] pixels = new float[LUMA_SAMPLED_DIMENSION][LUMA_SAMPLED_DIMENSION][3];
     
      for (int i=0;i<LUMA_SAMPLED_DIMENSION;i+=LUMA_BLOCK_SIZE)
      {
        for (int j=0;j<LUMA_SAMPLED_DIMENSION;j+=LUMA_BLOCK_SIZE)
        {
        	int filterId;
        	if (firstFrame || !bis.read())
        	{
        		filterId = (int) bis.read(LUMA_FILTER_BIT_DEPTH);
        	}
        	else
        	{
        		filterId = filters.lumaFilterSimilarityOrdering[previousFrameLumaFilters[i][j]][(int) bis.read(SIMILARITY_BIT_DEPTH)];
        	}
        	previousFrameLumaFilters[i][j]=filterId;
        	Block filter = filters.lumaFilters[filterId];
         
          // copy matched filter's lumenosity into corresponding output image's block
          for (int y=0;y<LUMA_BLOCK_SIZE;y++)
          {
            for (int x=0;x<LUMA_BLOCK_SIZE;x++)
            {
              pixels[j+x][i+y][LUMA]=filter.pixels[x][y][LUMA];
            }
          }
        }
      }
     
      // match sampled image blocks' 'chromanosity' to filters...
      for (int i=0;i<CHROMA_SAMPLED_DIMENSION;i+=CHROMA_BLOCK_SIZE)
      {
        for (int j=0;j<CHROMA_SAMPLED_DIMENSION;j+=CHROMA_BLOCK_SIZE)
        {
        	int filterId;
        	if (firstFrame || !bis.read())
        	{
        		filterId = (int) bis.read(CHROMA_FILTER_BIT_DEPTH);
        	}
        	else
        	{
        		filterId = filters.chromaFilterSimilarityOrdering[previousFrameChromaFilters[i][j]][(int) bis.read(SIMILARITY_BIT_DEPTH)];
        	}
        	previousFrameChromaFilters[i][j]=filterId;
        	Block filter = filters.chromaFilters[filterId];
 
          // copy matched filter's 'chromanosity' into corresponding output image's block
          for (int y=0;y<CHROMA_BLOCK_SIZE;y++)
          {
            for (int x=0;x<CHROMA_BLOCK_SIZE;x++)
            {
              pixels[j+x][i+y][CHROMA_U]=filter.pixels[x][y][CHROMA_U];
              pixels[j+x][i+y][CHROMA_V]=filter.pixels[x][y][CHROMA_V];
            }
          }
        }
      }
     
      // render the scaled output image
      BufferedImage yuvImage = new BufferedImage( SAMPLED_DIMENSION, SAMPLED_DIMENSION, BufferedImage.TYPE_INT_RGB );
      renderYUV(yuvImage,pixels);
     
      // save the output image, resized to the input image's dimensons
      ImageIO.write( blurResize(yuvImage,width, height), "png",outputFile);
      BufferedImage scaledImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
      scaledImage.getGraphics().drawImage(yuvImage.getScaledInstance(width, height, Image.SCALE_FAST),0,0,null);
      ImageIO.write( scaledImage, "png",new File(outputFile.getAbsolutePath()+".nonBlurred.png"));
      System.out.println(outputFile.getAbsolutePath());
  }
 
 
 
  /**
   * Converts the given buffered image into a 3-dimensional array of YUV pixels
   * @param image the buffered image to convert
   * @return 3-dimensional array of YUV pixels
   */
  static private float[][][] convertToYUVImage(BufferedImage image)
  {
    final int width = image.getWidth();
    final int height = image.getHeight();
    float[][][] yuv = new float[width][height][3];
    for (int y=0;y<height;y++)
    {
      for (int x=0;x<width;x++)
      {
        int rgb = image.getRGB( x, y );
        yuv[x][y]=rgb2yuv(rgb);
      }
    }
    return yuv;
  }
 
  /**
   * Renders the given YUV image into the given buffered image.
   * @param image the buffered image to render to
   * @param pixels the YUV image to render.
   * @param dimension the
   */
  static private void renderYUV(BufferedImage image, float[][][] pixels)
  {
    final int height = pixels.length;
    final int width = pixels[0].length;
    int rgb;
 
    for (int y=0;y<height;y++)
    {
      for (int x=0;x<width;x++)
      {
 
        rgb = yuv2rgb( pixels[x][y] );
        image.setRGB( x, y,rgb );
      }
    }
  }
 
  /**
   * Converts a RGB pixel into a YUV pixel
   * @param rgb a pixel encoded as 24 bit RGB
   * @return array representing a pixel. Consisting of Y,U and V components
   */
  static float[] rgb2yuv(int rgb)
  {
    float red = EIGHT_BIT_DIVISOR*((rgb>>16)&0xFF);
    float green = EIGHT_BIT_DIVISOR*((rgb>>8)&0xFF);
    float blue = EIGHT_BIT_DIVISOR*(rgb&0xFF);
 
    float Y = 0.299F*red + 0.587F * green + 0.114F * blue;
    float U = (blue-Y)*0.565F;
    float V = (red-Y)*0.713F;
 
    return new float[]{Y,U,V};
  }
 
  /**
   * Converts a YUV pixel into a RGB pixel
   * @param yuv array representing a pixel. Consisting of Y,U and V components
   * @return a pixel encoded as 24 bit RGB
   */
  static int yuv2rgb(float[] yuv)
  {
    int red = (int) ((yuv[0]+1.403*yuv[2])*256);
    int green = (int) ((yuv[0]-0.344 *yuv[1]- 0.714*yuv[2])*256);
    int blue = (int) ((yuv[0]+1.77*yuv[1])*256);
 
    // clamp to 0-255 range
    red=red<0?0:red>255?255:red;
    green=green<0?0:green>255?255:green;
    blue=blue<0?0:blue>255?255:blue;
 
    return (red<<16) | (green<<8) | blue;
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