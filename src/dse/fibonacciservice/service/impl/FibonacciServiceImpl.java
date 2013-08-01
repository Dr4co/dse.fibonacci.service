package dse.fibonacciservice.service.impl;

import api.FibonacciService;

public class FibonacciServiceImpl implements FibonacciService {
		@Override
		public long getNextFib() {

			if(!FibonacciServiceActivator.fibonacciSequence.isEmpty()){
				long tempFib = FibonacciServiceActivator.fibonacciSequence.get(0);
				FibonacciServiceActivator.fibonacciSequence.remove(0);
				
				if(FibonacciServiceActivator.fibonacciSequence.isEmpty()){
					FibonacciServiceActivator.calculateNextFibs();	
				}
				return tempFib;
			}
			else{
				return Long.MIN_VALUE;
			}		
		}
}
