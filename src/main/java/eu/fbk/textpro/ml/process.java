package eu.fbk.textpro.ml;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

public class process implements Runnable {
	HashMap<String, Integer> keys = new HashMap<>();
	BlockingQueue shared;
	public process(HashMap<String, Integer> keys, BlockingQueue sharedQueue){
		keys.putAll(keys);
		shared = sharedQueue;
		System.out.println("Consumer initated!");
	}
	
	@Override
	public void run()
    {
        while(true&&!learner2.finished&&learner2.sharedQueue.size()>0)
        {
            //get an object off the queue
            sen object = (sen) shared.poll();
            if(object!=null){
            	try {
            		System.out.println("Start process: "+object.iq);
					crun(object.fn,object.para,object.out,object.pr,object.iq);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
    }
	void  crun(String fn, LinkedList<Sentence> para, Writer out, parameters pr, int iq) throws IOException{
	
	//	System.out.println("Process "+iq);
		learner2.process(para,pr);
	//	System.out.println("Finish Process "+iq);
		learner2.writeToFile(para, fn, out);
		System.out.println("Finish write "+iq);
		out.append("\n");
		out.flush();
	}
}
