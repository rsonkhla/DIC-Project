package myWordCooccurence;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class WordCooccurence {

  public static class TokenizerMapper 
       extends Mapper<Object, Text, TextPair, DoubleWritable>{
    
    private final static DoubleWritable one 	= new DoubleWritable(1);
    private TextPair wordPair			= new TextPair();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      List<String> tokens 	= MyTokenizer.GetValidTokens(value.toString());
      int noOftokens 		= tokens.size();
      int contextWinSz 		= noOftokens;; 
		
		for(int i=0 ; i<noOftokens ; i++) {
			int minWinCord	= (i-contextWinSz) >= 0 		 ?  (i-contextWinSz) : 0;
			int maxWinCord	= (i+contextWinSz) <  noOftokens ?  (i+contextWinSz) : noOftokens;
			
			for(int j=minWinCord ; j<maxWinCord ; j++) {
				if(!tokens.get(i).equals(tokens.get(j))) {
					wordPair.setFirstText(tokens.get(i));
					wordPair.setSecondText("*");
					context.write(wordPair, one);
					wordPair.setFirstText(tokens.get(i));
					wordPair.setSecondText(tokens.get(j));
					context.write(wordPair, one);
				}
			}
		}
      }
    }
  
  public static class IntSumCombiner 
  		extends Reducer<TextPair,DoubleWritable,TextPair,DoubleWritable> {
	private DoubleWritable result = new DoubleWritable();
	
	public void reduce(TextPair key, Iterable<DoubleWritable> values, 
	                  Context context
	                  ) throws IOException, InterruptedException {
	 int sum = 0;
	 for (DoubleWritable val : values) {
	   sum += val.get();
	 }
	 result.set(sum);
	 context.write(key, result);
	}
  }
  
  public static class IntSumReducer 
       extends Reducer<TextPair,DoubleWritable,Text,DoubleWritable> {
    private DoubleWritable result = new DoubleWritable();
    static private String curKey	   = new String();
    static private int curKeyCount	   = 0;	

    public void reduce(TextPair key, Iterable<DoubleWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
    	
      System.out.print(key.GetPairText());	
    	
      if(!curKey.equals(key.first.toString())) {
    	  curKey	  = key.first.toString();
    	  curKeyCount = 0;
          for (DoubleWritable val : values) {
          curKeyCount += val.get();
          }   
      }
      else {
    	  double sum = 0;
    	  for (DoubleWritable val : values) {
    	  sum += val.get();
          }
        
    	  if(curKeyCount != 0)
    		  result.set(sum / curKeyCount);
    	  else
    		  result.set(0);
          context.write(key.GetPairText(), result);
      }
    }
  }
  
  public static class FirstPartitioner 
  extends Partitioner<TextPair, DoubleWritable> {
	  @Override
	  public int getPartition(TextPair key, DoubleWritable value, int numPartitions) {
	  return Math.abs(key.first.hashCode() * 127) % numPartitions;
	  }
  }
  
  public static class KeyComparator extends WritableComparator {
	  protected KeyComparator() {
		  super(TextPair.class, true);
	  }
	  @SuppressWarnings("rawtypes")
	  @Override
	  public int compare(WritableComparable w1, WritableComparable w2) {
		  TextPair ip1 = (TextPair) w1;
		  TextPair ip2 = (TextPair) w2;
		  return ip1.compareTo(ip2);
	  }
  }
  
  public static class GroupComparator extends WritableComparator {
	  protected GroupComparator() {
		  super(TextPair.class, true);
	  }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 2) {
      System.err.println("Usage: wordcooccurence <in> <out>");
      System.exit(2);
    }
    Job job = new Job(conf, "word cooccurence");
    job.setJarByClass(WordCooccurence.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumCombiner.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(TextPair.class);
    job.setOutputValueClass(DoubleWritable.class);
    
    job.setPartitionerClass(FirstPartitioner.class);
    job.setSortComparatorClass(KeyComparator.class);
    
    job.setNumReduceTasks(2);
    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
