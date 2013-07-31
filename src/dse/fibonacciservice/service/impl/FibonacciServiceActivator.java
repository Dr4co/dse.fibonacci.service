package dse.fibonacciservice.service.impl;

import java.util.ArrayList;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import dse.fibonacciservice.service.FibonacciService;

public class FibonacciServiceActivator implements BundleActivator {

	public static ArrayList<Long> fibonacciSequence = new ArrayList<Long>();
	public static long lastFib = -1;
	
    ServiceRegistration fibonacciServiceRegistration;
    public void start(BundleContext context) throws Exception {
        FibonacciService fibonacciService = new FibonacciServiceImpl();
        fibonacciServiceRegistration =context.registerService(FibonacciService.class.getName(), fibonacciService, null);
        
//        fibonacciSequence = new ArrayList<Long>();
        
        for(long i =0; i < 93; i++){
        	fibonacciSequence.add(fib(i));
        }
    }
   
    public void stop(BundleContext context) throws Exception {
        fibonacciServiceRegistration.unregister();
    }
    
	public static long fib(long n) {
		long prev1=0, prev2=1;
		for(long i=0; i<n; i++) {
			long savePrev1 = prev1;
			prev1 = prev2;
			prev2 = savePrev1 + prev2;
		}
		return prev1;
	}
}
