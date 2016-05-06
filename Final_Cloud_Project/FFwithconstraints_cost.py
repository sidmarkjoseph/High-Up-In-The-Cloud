import xlrd
import math

workbook = xlrd.open_workbook('90th-hourly-memory.xls');
worksheet = workbook.sheet_by_index(0);
workbookone =xlrd.open_workbook('90th-hourly-cpu.xls');
worksheetone = workbookone.sheet_by_index(0);
workbooktwo = xlrd.open_workbook('90th-hourly-bandwidth.xls');
worksheettwo = workbooktwo.sheet_by_index(0);
file = open('result_Of_Problem1.txt','r');
mapping_list = {};
mapping_listtwo = {};
constraint_list = {};
helper_list =[];
vmseperation_list = []
connum = int(raw_input('Enter the number of Maximum number of services constraint \n'))
i=0;
while i < connum:
     temp=int(raw_input('Enter the sever number'));
     constraint_list[temp] = int(raw_input('Enter the maximum number of services on that server number'));
     i = i+1;
connumtwo = int(raw_input("Enter the number of Combination contraints \n"))
i = 0;
while i < connumtwo:
     temp = int(raw_input('Enter the server number'));
     tempnum = int(raw_input('Enter the number of VM\'s invloved'));
     j=0;
     list_of_vm = []
     while j < tempnum:
        strtemp = raw_input('Enter the VM number')
        list_of_vm.append(strtemp);
        helper_list.append(int(strtemp));
        j=j+1;
     mapping_listtwo[temp] = list_of_vm;
     list_of_vm = [];   
     i=i+1;  
i=0;        
contwo = int(raw_input("Enter the number of VM's to be seperated \n"));
while i < contwo:
    vmseperation_list.append(int(raw_input("Enter the VM number")));
    i=i+1;
#print vmseperation_list;         
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
  for x in mapping_listtwo:
     for y in mapping_listtwo[x]:
        y = int(y);
        temp_mem = worksheet.cell(y-1,i).value;
        temp_cpu = worksheetone.cell(y-1,i).value;
        temp_net = worksheettwo.cell(y-1,i).value;
        list_of_cpus[x-1] = list_of_cpus[x-1] - temp_cpu;
        list_of_memories[x-1] =list_of_memories[x-1] - temp_mem;
        list_of_networks[x-1] = list_of_networks[x-1] - temp_net;
        usage_list[x-1] =1;
        print "VM ",y," is plced on the", x ,"th server";
     mapping_list[x] = '@'.join(mapping_listtwo[x]);
     mapping_list[x] += '@';
  sum=0; 
  for j in range(100):
    if j+1 in helper_list:
       continue;
    temp_mem = worksheet.cell(j,i).value;
    temp_cpu = worksheetone.cell(j,i).value;
    temp_net = worksheettwo.cell(j,i).value;
    flag = 0;
    one =0;
    for x in range(len(list_of_cpus)):
       if constraint_list.has_key(x+1):
         if mapping_list.has_key(x+1):
            temp_len = len(mapping_list[x+1].split('@'))-1;
            if temp_len >= constraint_list[x+1]:
              continue;
            else:
                 templist = mapping_list[x+1].split('@');
                 fmarker = 0;
                 for r in range(j+1):
                    if j+1 in vmseperation_list and r+1 in vmseperation_list :
                      fmarker =1;
                      break;
                 if fmarker == 1:
                   continue;         
                 if temp_mem < list_of_cpus[x] and temp_cpu < list_of_memories[x] and temp_net < list_of_networks[x] and temp_net < list_of_networks[x] and list_of_cpus[x]-temp_cpu>0 and list_of_memories[x]-temp_mem>0 and list_of_networks[x]-temp_net>0:      
                    flag=1;
                    usage_list[x] = 1;  
                    mapping_list[x+1] = mapping_list[x+1] + str(j+1) + '@';
                    print "VM ", j+1 , "is placed on the ", x+1 , "th server \n";
                    list_of_cpus[x] =list_of_cpus[x] - temp_cpu;
                    list_of_memories[x] = list_of_memories[x] - temp_mem;
                    list_of_networks[x] = list_of_networks[x] - temp_net;
                    break;
         else:
             if temp_mem < list_of_cpus[x] and temp_cpu < list_of_memories[x] and temp_net < list_of_networks[x] and temp_net < list_of_networks[x] and list_of_cpus[x]-temp_cpu>0 and list_of_memories[x]-temp_mem>0 and list_of_networks[x]-temp_net>0:      
                    flag=1;
                    usage_list[x] = 1;
                    mapping_list[x+1] = str(j+1) + '@'
                    print "VM ", j+1 , "is placed on the ", x+1 , "th server \n";
                    list_of_cpus[x] =list_of_cpus[x] - temp_cpu;
                    list_of_memories[x] = list_of_memories[x] - temp_mem;
                    list_of_networks[x] = list_of_networks[x] - temp_net;
                    break;
       else:                                    
        if temp_mem < list_of_cpus[x] and temp_cpu < list_of_memories[x] and temp_net < list_of_networks[x] and temp_net < list_of_networks[x] and list_of_cpus[x]-temp_cpu>0 and list_of_memories[x]-temp_mem>0 and list_of_networks[x]-temp_net>0:      
          flag=1;
          usage_list[x] = 1;
          if mapping_list.has_key(x+1):
             templist = mapping_list[x+1].split('@');
             fmarker = 0;
             for r in range(j+1):
                if j+1 in vmseperation_list and r+1 in vmseperation_list :
                  fmarker =1;
                  break;
             if fmarker == 1:
                continue;         
             mapping_list[x+1] += str(j+1) + '@';
          else:
             mapping_list[x+1] = str(j+1) + '@';   
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
       final =final + list_of_cpus[x]/cpu;
       finalone = finalone + list_of_memories[x]/memory;
       finaltwo = finaltwo + list_of_networks[x]/network;
       count_number_servers =count_number_servers + 1;
  #print final, finalone, finaltwo, count_number_servers;     
  finalthree = max(final,finalone,finaltwo);
  print "Power utilization for", i ,"th Hour",finalthree*power,"\n";  
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
  mapping_list={};   
  i=i+1;







