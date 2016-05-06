import xlrd
import math

workbook = xlrd.open_workbook('90th-hourly-memory.xls');
worksheet = workbook.sheet_by_index(0);
workbookone =xlrd.open_workbook('90th-hourly-cpu.xls');
worksheetone = workbookone.sheet_by_index(0);
workbooktwo = xlrd.open_workbook('90th-hourly-bandwidth.xls');
worksheettwo = workbooktwo.sheet_by_index(0);
file = open('result_Of_Problem1.txt','r');
i=0;
for line in file:
    if i==0:
      cpu = float(line);
      i=i+1;
    elif i==1:
      memory = float(line);
      i=i+1;
    elif i==2:
      network = float(line);
      i=i+1;
    elif i==3:
      number = float(line);
      i=i+1;
    elif i==4:
      cost = float(line);
      i=i+1;
    elif i==5:
      power = float(line);
      i=i+1;  
    else:
      i=i+1;
usage_list = [];
list_of_cpus = [];
list_of_memories = [];
list_of_networks = [];
i=0;
while i < number:
    list_of_cpus.append(cpu);
    list_of_memories.append(memory);
    list_of_networks.append(network);
    usage_list.append(0);
    i=i+1;
i=0;
while i<=23:
  print "----------FOR THE ",i," th HOUR-------------";
  sum=0; 
  for j in range(100):
   
    temp_mem = worksheet.cell(j,i).value;
    temp_cpu = worksheetone.cell(j,i).value;
    temp_net = worksheettwo.cell(j,i).value;
    flag = 0;
    for x in range(len(list_of_cpus)):
     if temp_mem < list_of_cpus[x] and temp_cpu < list_of_memories[x] and temp_net < list_of_networks[x] and temp_net < list_of_networks[x] and list_of_cpus[x]-temp_cpu>0 and list_of_memories[x]-temp_mem>0 and list_of_networks[x]-temp_net>0:
       flag=1;
       usage_list[x] = 1;
       print "VM ", j+1 , "is placed on the ", x+1 , "th server \n";
       list_of_cpus[x] =list_of_cpus[x] - temp_cpu;
       list_of_memories[x] = list_of_memories[x] - temp_mem;
       list_of_networks[x] = list_of_networks[x] - temp_net;
       break;
    if flag == 0:
      print "VM ", j+1 , "cannot be placed on any available server \n"    
  for x in usage_list:
   sum = sum + x;
  print "First Fit on the System for", i , " th Hour", sum*cost , "\n";
  k=0;
  final = 0;
  finalone =0;
  finaltwo = 0;
  count_number_servers = 0;
  for x in range(len(list_of_cpus)):
       if cpu-list_of_cpus[x] == 0:
         continue;
       final =final + (cpu-list_of_cpus[x])/cpu;
       finalone = finalone + (memory-list_of_memories[x])/memory;
       finaltwo = finaltwo + (network-list_of_networks[x])/network;
       count_number_servers=count_number_servers+1;         
  finalthree = max(final*power,finalone*power,finaltwo*power);
  print "Power utilization for", i ,"th Hour",finalthree,"\n";  
  list_of_cpus=[];
  list_of_memories=[];
  list_of_networks=[];
  usage_list=[];
  while k < number:
     list_of_cpus.append(cpu);
     list_of_memories.append(memory);
     list_of_networks.append(network);
     usage_list.append(0);
     k=k+1;  
  i=i+1;






