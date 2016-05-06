The raw data are included in a big excel file (2011-03-07-04-11.csv).

It records the VM name (first column), timestamp (second column), its CPU (third column, MHz),

network bandwidth (fourth column, KBps) and memory (fifth column, MB) utilizations

of more than 100 VMs on a 5-minute basis for over a month.



The processed data are included in three small excel files (90th-hourly-bandwidth, 90th-hourly-cpu and 90th-hourly-memory).

These files include the HOURLY CPU, Memory and Network utilizations of 100 VMs

for a total of 24 hours (a day), respectively. Each column corresponds to an hour in a day

and each row corresponds to a VM. We generated these files from the raw data by aggreegating samples

belonging to each hour and taking the 90-th percentile for estimation.

