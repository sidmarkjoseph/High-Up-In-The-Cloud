
/*
 * CompanyMapper.java
 * Author : Siddharth Mark Joseph
 * Created on April 13th 2016
 */

package org.sample;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;


/**
 *
 * @author mac
 */
public class CompanyMapper extends Mapper<LongWritable,Text,Text,Text> {
    // The Karmasphere Studio Workflow Log displays logging from Apache Commons Logging, for example:
    // private static final Log LOG = LogFactory.getLog("org.sample.CompanyMapper");

    @Override
    protected void map(LongWritable key, Text value, Context context)
                    throws IOException, InterruptedException {
    	/* Creating two hop paths given neighbors of  particular node (Each record in file) */
    	int i =0;
    	String rem = null;
    	for(String a  : value.toString().split(":"))
    	{
    		if(i==0)
    		{
    			rem =a;
    		}
    		else
    		{
    		   String arr[]=a.split(",");  
    		   for(int j=0; j < arr.length;j++)
    		   {
    			   for(int l=0;l < arr.length;l++)
    			   {
    				   if(l==j)
    					   continue;
    				   else
    				   {
					   context.write(new Text(arr[j]), new Text(rem + "_" + arr[l]));
    				   }
    			   }
    		   }
    		}
    		i++;
    	}
    	
    }
}
