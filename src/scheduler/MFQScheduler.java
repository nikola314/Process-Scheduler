package scheduler;

import java.util.Vector;
import process.*;

public class MFQScheduler extends Scheduler {
	private int numOfQueues;
	private int timeSlices[];
	private Vector<Vector<Pcb>> queues;
	
	MFQScheduler(int numOfQueues, int timeSlices[]){
		this.numOfQueues=numOfQueues;
		this.timeSlices=timeSlices;
		queues=new Vector<Vector<Pcb>>();
		for(int i=0;i<numOfQueues;i++)	{
			queues.add(new Vector<Pcb>());
		}	
	}
	
	@Override
	public synchronized Pcb get(int cpuId) {
		Pcb retval=null;
		boolean found=false;
		boolean foundMatch=false;
		boolean toPerformAging=false;
		for(Vector<Pcb> queue:queues) {		
			for(Pcb pcb:queue) {
				if(pcb.getPcbData().cpuId==cpuId) {
					retval=pcb;
					queue.remove(retval);
					foundMatch=true;
					found=true;
					break;
				}
				else if(!found) {
					retval=pcb;
					found=true;
				}
			}
			if(found) {
				if(!foundMatch) {
					switchCPUs(retval,cpuId);
					queue.remove(retval);
				}
				toPerformAging=true;
				break;
			}
		}	
		if(toPerformAging) {
			performAging();
			return retval;
		}
		return null;
	}
	
	private void performAging() {
		for(int i=1; i<queues.size();i++) {
			Vector<Pcb> queue= queues.get(i);	
			for(int j=0;j<queue.size();j++) {
				Pcb pcb=queue.get(j);
				
				PcbData data=pcb.getPcbData();
				data.age++;
				if(data.age>=agingFactor) {
					data.age=0;
					Pcb temp=pcb;
					queue.remove(pcb);
					queues.get(i-1).add(temp);
					temp.setTimeslice(timeSlices[i-1]);
				}
			}
		}
	}

	@Override
	public synchronized void put(Pcb pcb) {
		if(pcb.getPcbData()==null) {
			initPcbData(pcb);
		}
		else {
			PcbData data = pcb.getPcbData();
			Pcb.ProcessState previousState= pcb.getPreviousState();
			if(previousState==Pcb.ProcessState.BLOCKED) {	
				data.priority= data.priority>0? data.priority-1:data.priority;
			}
			else if(previousState==Pcb.ProcessState.RUNNING) {
				data.priority= data.priority<numOfQueues-2? data.priority+1:data.priority;
			}			
		}
		pcb.getPcbData().age=0;
		pcb.setTimeslice(timeSlices[pcb.getPcbData().priority]);
		queues.get(pcb.getPcbData().priority).add(pcb);
	}

}
