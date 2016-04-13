
/*
 *CompanyJob.java
 *Author : Siddharth Mark Joseph
 * Created on April 13th 2016
 */
package org.sample;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 *
 * @author mac
 */
public class CompanyJob {
    // The Karmasphere Studio Workflow Log displays logging from Apache Commons Logging, for example:
    // private static final Log LOG = LogFactory.getLog("org.sample.CompanyJob");

    public static void main(String[] args) throws Exception {
        Job job = new Job();

        /* Autogenerated initialization. */
        initJob(job);
        
        /* Custom initialization. */
        initCustom(job);

        /* Tell Task Tracker this is the main */
        job.setJarByClass(CompanyJob.class);

        /* This is an example of how to set input and output. */
        FileInputFormat.setInputPaths(job, args[0]);
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        /* You can now do any other job customization. */
        // job.setXxx(...);

        /* And finally, we submit the job. */
        job.submit();

        job.waitForCompletion(true);
    }

    /**
     * This method is executed by the workflow
     */
    public static void initCustom(Job job) {
        // Add custom initialisation here, you may have to rebuild your project before
        // changes are reflected in the workflow.
    }

    /** This method is called from within the constructor to
     * initialize the job.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Job Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initJob
    public static void initJob(Job job) {
org.apache.hadoop.conf.Configuration conf = job.getConfiguration();
// Generating code using Karmasphere Protocol for Hadoop 0.20
// CG_GLOBAL

// CG_INPUT_HIDDEN
job.setInputFormatClass(org.apache.hadoop.mapreduce.lib.input.TextInputFormat.class);

// CG_MAPPER_HIDDEN
job.setMapperClass(org.sample.CompanyMapper.class);
job.getConfiguration().set("mapred.mapper.new-api", "true");

// CG_MAPPER
job.getConfiguration().set("mapred.map.tasks", "3");
job.setMapOutputKeyClass(org.apache.hadoop.io.Text.class);
//job.setMapOutputValueClass(org.apache.hadoop.io.LongWritable.class);
job.setMapOutputValueClass(org.apache.hadoop.io.Text.class);
// CG_PARTITIONER_HIDDEN
job.setPartitionerClass(org.apache.hadoop.mapreduce.lib.partition.HashPartitioner.class);

// CG_PARTITIONER

// CG_COMPARATOR_HIDDEN

// CG_COMPARATOR

// CG_COMBINER_HIDDEN

// CG_REDUCER_HIDDEN
job.setReducerClass(org.sample.CompanyReducer.class);
job.getConfiguration().set("mapred.reducer.new-api", "true");

// CG_REDUCER
job.getConfiguration().set("mapred.reduce.tasks", "2");
job.setOutputKeyClass(org.apache.hadoop.io.Text.class);
job.setOutputValueClass(org.apache.hadoop.io.Text.class);

// CG_OUTPUT_HIDDEN
job.setOutputFormatClass(org.apache.hadoop.mapreduce.lib.output.TextOutputFormat.class);

// CG_OUTPUT

// Others
}

}
