package scheduler;

import process.*;

public abstract class Scheduler {
	 private static final String MFQString="MFQS";
	 private static final String SJFString="SJFS";
	 private static final String CFString="CFS";
	 protected static int cpuCount=10;
	 
	 protected static final double firstEstimationTime = 3;
	 protected static final int agingFactor= 15;
	 protected static int procPerCPU[];
	
	 private static final double SJFCoef=1;
	 private static final boolean SJFPreemptive = false;
	 
	 private static int MFQNumOfQueues = 3;
	 private static final int MFQTimeSlices[] = { 12, 10, 8 };
	 
	 public abstract Pcb get(int cpuId);
	 public abstract void put(Pcb pcb);
	 
	 public static void setCpuCount(int num) {
		 cpuCount=num;
	 }
	 
	 public static Scheduler createScheduler(String[] args) {
		 procPerCPU=new int[cpuCount];
		 for(int i=0;i<procPerCPU.length;i++) procPerCPU[i]=0;
		 
		 if (args.length==0) return null;
		 if(args[0].equals(SJFString)) return createSJFScheduler(args);
		 else if(args[0].equals(MFQString)) return createMFQScheduler(args);
		 else if(args[0].equals(CFString)) return createCFScheduler(args);	 
		 return null;
	 }
	 
	 private static Scheduler createSJFScheduler(String[] args) {
		 if(args.length<3) return new SJFScheduler(SJFCoef,SJFPreemptive);
		 else return new SJFScheduler(Double.parseDouble(args[1]),Boolean.parseBoolean(args[2]));
	 }
	 
	 private static Scheduler createMFQScheduler(String[] args) {
		 if(args.length<2 || (Integer.parseInt(args[1])!=args.length-2)) return new MFQScheduler(MFQNumOfQueues,MFQTimeSlices);
		 else {
			 MFQNumOfQueues=args.length-2;
			 int numOfQueues= Integer.parseInt(args[1]);
			 int timeSlices[]=new int[numOfQueues];
			 for(int i=2;i<2+numOfQueues;i++) timeSlices[i-2]=Integer.parseInt(args[i]);
			 return new MFQScheduler(numOfQueues,timeSlices);
		 }
	 }
	 
	 private static Scheduler createCFScheduler(String[] args) {
		 return new CFScheduler();
	 }
	 
	 protected void initPcbData(Pcb pcb) {
		 PcbData data=new PcbData();
		 int min=Integer.MAX_VALUE;
		 int ind=-1;
		 for(int i=0;i<procPerCPU.length;i++) {
			 if(procPerCPU[i]<min) {
				 ind=i;
				 min=procPerCPU[i];
			 }
		 }
		 procPerCPU[ind]++;
		 data.age=0;
		 data.timeInScheduler=0;
		 data.cpuId=ind;
		 data.totalExecTime=0;
		 data.execTimeEvaluation=firstEstimationTime;
		 data.priority=pcb.getPriority()>=MFQNumOfQueues? MFQNumOfQueues-1:pcb.getPriority();
		 pcb.setPcbData(data);
	 }
	 
	 protected void switchCPUs(Pcb pcb, int cpuId) {
		 procPerCPU[pcb.getPcbData().cpuId]--;
		 pcb.getPcbData().cpuId=cpuId;
	 }
	 
}
