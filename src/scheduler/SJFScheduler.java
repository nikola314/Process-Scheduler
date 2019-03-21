package scheduler;

import java.util.PriorityQueue;
import process.*;

public class SJFScheduler extends Scheduler {
	private double coef;
	private boolean preemptive;
	private PriorityQueue<Pcb> queue;
	private final int agingCoef=3;
	
	SJFScheduler(double coef, boolean preemptive) {
		this.coef=coef;
		this.preemptive=preemptive;
		queue=new PriorityQueue<Pcb>(11,(a,b) -> (int)((a.getPcbData().execTimeEvaluation-a.getPcbData().age) - (b.getPcbData().execTimeEvaluation-b.getPcbData().age)));
	}
	
	@Override
	public Pcb get(int cpuId) {
		Pcb retval = queue.poll();
		if(retval!= null) performAging();
		return retval;
	}

	private synchronized void performAging() {
		PriorityQueue<Pcb> q2=new PriorityQueue<Pcb>(11,(a,b) -> (int)((a.getPcbData().execTimeEvaluation-a.getPcbData().age) - (b.getPcbData().execTimeEvaluation-b.getPcbData().age)));
		while(!queue.isEmpty()) {
			Pcb pcb=queue.poll();
			pcb.getPcbData().age+=agingCoef;
			q2.add(pcb);
		}
		queue=q2;
	}
	
	@Override
	public void put(Pcb pcb) {
		if(pcb.getPcbData()==null) {
			initPcbData(pcb);
		}
		pcb.getPcbData().age=0;
		double evaluation = pcb.getPcbData().execTimeEvaluation=nextExecutionEvaluation(pcb);
		System.out.println(pcb.getPcbData().execTimeEvaluation);
		queue.add(pcb);	
		if(preemptive) {
			int ind=-1;
			double max=Double.MIN_VALUE;
			for(int i=0;i<cpuCount;i++) {
				if(Pcb.RUNNING==null) break;
				if(Pcb.RUNNING[i]==null) continue;
				if(Pcb.RUNNING[i].getPcbData()==null) continue;
				double remainingExecution= Pcb.RUNNING[i].getPcbData().execTimeEvaluation - Pcb.RUNNING[i].getExecutionTime();
				if(remainingExecution>evaluation) {
					if(remainingExecution>max) {
						max=remainingExecution;
						ind=i;
					}
				}
			}
			if(ind!=-1) {
				Pcb.RUNNING[ind].preempt();
			}
		}		
	}
	
	private double nextExecutionEvaluation(Pcb pcb) {
		double lastRealExecutionTime=pcb.getExecutionTime();
		double lastEstimatedExecutionTime = pcb.getPcbData().execTimeEvaluation;
		return coef*lastRealExecutionTime + (1-coef)*lastEstimatedExecutionTime;
	}
}
