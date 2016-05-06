import csv
import math
from pulp import *

with open('data.csv','rb') as f:
  reader = csv.reader(f);
  sum = 0;
  i=0;
  sumone = 0;
  sumtwo = 0;
  for row in reader:
     if i == 0:
       temp = row[0];
       maxe = int(row[2])
       maxeone = int(row[3]);
       maxetwo = int(row[4]);
     if row[0] == temp:
      if int(row[2]) >  maxe:
             maxe = int(row[2])    
      elif int(row[3]) >  maxeone:
             maxeone = int(row[3])
      elif int(row[4])  >  maxetwo:
             maxetwo = int(row[4])
     else:
         sum = sum + maxe;
         sumone = sumone +  maxeone;
         sumtwo = sumtwo +  maxetwo;
         temp = row[0];
         maxe = int(row[2]);
         maxeone = int(row[3]);
         maxetwo = int(row[4]);    
     i = i + 1;   
  print '------------------------------------------------------\n'
  print '------------------HOMOGENOUS SERVERS------------------\n' 
  print 'ASSUMPTION - CPU, MEMORY, NETWORK CAPACITY OF SERVER EXCEEDS MAXIMUM RESPECTIVE VALUE OF A VM \n'     
  name = raw_input('ENTER NAME OF SERVER \n');
  cpu = float(raw_input('ENTER CPU CAPACITY OF SERVER in MHz \n'));
  memory = float(raw_input('ENTER MEMORY CAPACITY OF SERVER in MB \n '));
  network = float(raw_input('ENTER NETWORK BANDWIDTH OF SERVER in KBps \n'));
  cost = float(raw_input('ENTER COST OF SERVER \n'))
  power = float(raw_input('ENTER POWER UTILIZATION OF EACH SERVER \n'));

  var =  max(sum/cpu,sumone/network,sumtwo/memory)
  
  if isinstance(var,int):
    print "You require", var , "units of ", name
    final = var;
  else:
    final = math.ceil(var); 
    print "You require", final , "units of ", name 
  f = open("result_Of_Problem1.txt","w")
  f.write(str(cpu) + "\n");
  f.write(str(memory)+ "\n");
  f.write(str(network)+ "\n");
  f.write(str(final)+ "\n");
  f.write(str(cost)+ "\n");
  f.write(str(power)+"\n");
  f.close();
  '''print "-------------------------------------------------------\n"
  print '--------------HETEROGENOUS SERVERS(WITH 2 TYPES OF SERVERS)----------------------\n'
  cost1= float(raw_input('ENTER COST OF SERVER 1 \n'));
  cost2 = float(raw_input('ENTER COST OF SERVER 2 \n'))
  mem1 = float(raw_input('ENTER MEMORY CAPACITY OF SERVER 1 \n'));
  mem2 = float(raw_input('ENTER MEMORY CAPACITY OF SERVER 2 \n'))
  net1 = float(raw_input('ENTER NETWORK BANDWIDTH OF SERVER 1 \n'));
  net2 = float(raw_input('ENTER NETWORK BANDWIDTH OF SERVER 2 \n'));
  cpu1 = float(raw_input('ENTER CPU CAPACITY OF SERVER 1 \n'));
  cpu2 = float(raw_input('ENTER CPU CAPACITY OF SERVER 2 \n'));

  #Number of servers of Type 1
  x = LpVariable("x",0)
  #Number of servers of Type 2
  y = LpVariable("y",0)
  prob = LpProblem("heteroProblem",LpMinimize)
  prob += x*mem1 + y*mem2 >= sum
  prob += x*net1 + y*net2 >= sumone
  prob += x*cpu1 + y*cpu2 >= sumtwo
  prob += cost1*x + cost2*y
  status = prob.solve()
  if(LpStatus[status]=='Optimal'):
      f = open("result_Of_Problem1.txt","a")
      f.write(str(math.ceil(value(x)))+ "\n");
      print "Total number of units of Server 1", math.ceil(value(x)),"\n";
      f.write(str(math.ceil(value(y)))+"\n");
      print "Total number of units of Server 2", math.ceil(value(y)),"\n";
      f.write(str(math.ceil(value(x))*cost1 + math.ceil(value(y))*cost2)+"\n")
      print "Total Cost", math.ceil(value(x))*cost1 + math.ceil(value(y))*cost2 , "\n";'''


