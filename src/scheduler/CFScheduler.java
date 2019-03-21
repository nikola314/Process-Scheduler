package scheduler;

import java.util.PriorityQueue;

import process.*;

public class CFScheduler extends Scheduler {
	private PriorityQueue<Pcb> queue;
	
	CFScheduler(){
		queue=new PriorityQueue<Pcb>(11,(a,b) -> (int)(a.getPcbData().totalExecTime - b.getPcbData().totalExecTime));
	}
	
	@Override
	public Pcb get(int cpuId) {
		Pcb retval= queue.poll();
		if(retval==null) return retval;
		retval.setExecutionTime((long)(System.currentTimeMillis()-retval.getPcbData().timeInScheduler)/Pcb.getProcessCount());
		return retval;
	}

	@Override
	public void put(Pcb pcb) {
		if(pcb.getPcbData()==null) {
			initPcbData(pcb);
		}
		else {
			PcbData data = pcb.getPcbData();
			Pcb.ProcessState previousState= pcb.getPreviousState();
			if(previousState==Pcb.ProcessState.BLOCKED) {	
				data.totalExecTime=0;
			}
			else if(previousState==Pcb.ProcessState.RUNNING) {
				data.totalExecTime+=pcb.getExecutionTime();
			}
		}		
		pcb.getPcbData().timeInScheduler=System.currentTimeMillis();
		queue.add(pcb);
	}
	
}
