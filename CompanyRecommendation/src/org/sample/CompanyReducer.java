
/*
 * CompanyReducer.java
 * Author : Siddharth Mark Joseph
 * Created : April 13th 2016
 */

package org.sample;


import java.io.IOException;
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import java.lang.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
/**
 *
 * @author mac
 */
public class CompanyReducer extends Reducer<Text,Text,Text,Text> {
    // The Karmasphere Studio Workflow Log displays logging from Apache Commons Logging, for example:
    // private static final Log LOG = LogFactory.getLog("org.sample.CompanyReducer");

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
                        throws IOException, InterruptedException {
    	int i=0,j=0;
        String a[] = new String[10];
    	for (Text val : values) {
    		a[i]=val.toString();
    		i++;
    	}
      /*Removes redundant paths and uses HashMap to count the number of two node hops*/
    	for(i=0;i < a.length; i++)
    	{
    	   if(a[i]==null)
    		   continue;
    	   String arr[] = a[i].split("_");
    	   String test = arr[0];
    	   for(j=0;j<a.length;j++ )
    	   {
    		   if(i==j)
    			   continue;
    		   if(a[j]==null)
    			   continue;
    		   String testarr[] = a[j].split("_");
    		   String tester = testarr[1];
    		  if((test.equalsIgnoreCase(tester)))
    		  {
    			  if(arr[1].equalsIgnoreCase(testarr[0]))
    			  {
    	            a[i] = null;
    	            a[j]=null;
    			  }
    			  else
    			  {
    				  a[j]=null;
    			  }
    		  }
    	   }
    		  
    	}
    	HashMap<String,Integer> hm = new HashMap<String,Integer>();
    	for(i=0;i<a.length;i++)
    	{
    		if(a[i]!=null)
    		{
    		    String arr[] = a[i].split("_");
    		    String test = arr[1];
    		    if(hm.containsKey(test))
    		    {
    		    	hm.put(test, hm.get(test)+1);
    		    }
    		    else
    		    hm.put(test,1);
    		}
    	}
    	  Set<Entry<String, Integer>> set = hm.entrySet();
          List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
          Collections.sort( list, new Comparator<Map.Entry<String, Integer>>()
          {
              public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
              {
            	  if((o2.getValue()).compareTo(o1.getValue())==0)
            	  {
            		  return o1.getKey().compareTo(o2.getKey());
            	  }
            	  else
            	  {
                  return (o2.getValue()).compareTo( o1.getValue());
            	  }
              }
          } );
 
    	String str="=> ";
    	for(Map.Entry<String,Integer> entry :list)
    	{
    	   str = str + "[" + entry.getKey() + ":" + entry.getValue() + "]" + " ";
    	}
        
       context.write(key, new Text(str));
    }
}
