package dse.fibonacciservice.service.impl;

import java.util.ArrayList;

import dse.fibonacciservice.service.FibonacciService;

public class FibonacciServiceImpl implements FibonacciService {
	    public String getFibonacci() {
	        System.out.println("Inside FibonacciServiceImple.getFibonacci()");
	        
	        System.out.println(FibonacciServiceActivator.fibonacciSequence);
	        return "Say Fibonacci";
	    }

		@Override
		public long getNextFib() {

			if(!FibonacciServiceActivator.fibonacciSequence.isEmpty()){
				FibonacciServiceActivator.lastFib = FibonacciServiceActivator.fibonacciSequence.get(0);
				long temp = FibonacciServiceActivator.fibonacciSequence.get(0); 
				FibonacciServiceActivator.fibonacciSequence.remove(0);
				return temp;
			}
			else{
				return -1;
			}			
		}
}
