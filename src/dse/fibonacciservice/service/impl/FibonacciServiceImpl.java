package dse.fibonacciservice.service.impl;

import api.FibonacciService;

public class FibonacciServiceImpl implements FibonacciService {
		@Override
		public long getNextFib() {

			return FibonacciServiceActivator.getFibNumber();
			
//			long tempFib;
//			if(!FibonacciServiceActivator.sequenceEmpty()){
////				tempFib = FibonacciServiceActivator.fibonacciSequence.get(0);
//				FibonacciServiceActivator.fibonacciSequence.remove(0);
//				
//				if(FibonacciServiceActivator.fibonacciSequence.isEmpty()){
//					FibonacciServiceActivator.calculateNextFibs();	
//				}
//				return tempFib;
//			}
//			else{
//				FibonacciServiceActivator.calculateNextFibs();	
//
//				tempFib = FibonacciServiceActivator.fibonacciSequence.get(0);
//				FibonacciServiceActivator.fibonacciSequence.remove(0);
//
//				return tempFib;

//				return Long.MIN_VALUE;
//			}		
		}
}
