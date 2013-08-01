package dse.fibonacciservice.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import api.FibonacciService;

public class FibonacciServiceActivator implements BundleActivator {

	public static ArrayList<Long> fibonacciSequence = new ArrayList<Long>();
	public static boolean unused = true;
	public static int sequenceSize;
	public final static int defaultSequenceSize = 4; 
	private static long secondLastFib, lastFib;
	public static BundleContext bc;

	public final static String fileName = "fibsequence.dat";
	
    public ServiceRegistration<?> fibonacciServiceRegistration;
    
    public void start(BundleContext context) throws Exception {
        FibonacciService fibonacciService = new FibonacciServiceImpl();
        fibonacciServiceRegistration =context.registerService(FibonacciService.class.getName(), fibonacciService, null);
        bc = context;
        
        // write/read from mydatafile
        /* Downside:
         * The data is deleted when starting the application with the -clean option.
         * This often happens during debugging.
		 * The data is not accessible when the plugin is updated/replaced.
		 * Different versions of the same plugin get different plugin IDs,
		 * and the data folder is bound to the ID.
         */
        File fibsequence = context.getDataFile(fileName);
        BufferedWriter writer; 
        BufferedReader reader;
        
        if(fibsequence == null){
        	// No file system support!
        	System.out.println("No file system support!");
        }
        else{
			reader = new BufferedReader(new FileReader(fibsequence));   
						
			String line = reader.readLine();
			if(line != null){
				// On resume state				
		    	System.out.println("Server: Resume state");        	

				// If the configuration property isn't set, choose the default sequence size (4).
		        if(context.getProperty("dse.fibonacci.service.fibsize") != null){
		            sequenceSize = Integer.valueOf(context.getProperty("dse.fibonacci.service.fibsize"));        	
		        }
		        else{
		        	sequenceSize = defaultSequenceSize;
		        }

		        secondLastFib = -1;
		        lastFib = -1;
		        for(int i=0; i < sequenceSize; ++i){
		        	// Only the last two numbers are of interest
		        	if(i == sequenceSize-2){
		        		secondLastFib = Long.valueOf(line);
		        	}
		        	else if(i == sequenceSize-1){
		        		lastFib = Long.valueOf(line);
		        	}
		        	line = reader.readLine();
		        }
		        
		        writer = new BufferedWriter(new FileWriter(fibsequence));
				for(int i=0; i < sequenceSize; ++i){
					/* Formula: Calculating Fibonacci numbers
					 * f(0) = 0;
					 * f(1) = 1;
					 * f(n) = f(n-1)+f(n-2)
					 */
					fibonacciSequence.add(secondLastFib+lastFib);
					writer.write(String.valueOf(secondLastFib+lastFib));
		        	writer.newLine();
		        	
		        	// Update the next fibonacci number
		        	long temp = secondLastFib+lastFib;
		        	secondLastFib = lastFib;
		        	lastFib = temp;
				}
	    		
		    	writer.flush();
		    	writer.close();	
		        
			}
			// On first run
			else{
	        	System.out.println("Server: First run");

				// If the configuration property isn't set, choose the default sequence size (4).
		        if(context.getProperty("dse.fibonacci.service.fibsize") != null){
		            sequenceSize = Integer.valueOf(context.getProperty("dse.fibonacci.service.fibsize"));        	
		        }
		        else{
		        	sequenceSize = defaultSequenceSize;
		        }
				
				// Create the first (sequenceSize) fibonacci sequence numbers and
		        // add them to the ArrayList and the persistence storage.
				writer = new BufferedWriter(new FileWriter(fibsequence));
				
				fibonacciSequence = new ArrayList<Long>();	// Initialize ArrayList
				
				for(int i=0; i < sequenceSize; ++i){
					/* Formula:
					 * f(0) = 0;
					 * f(1) = 1;
					 * f(n) = f(n-1)+f(n-2)
					 */
					if(i == 0){
						fibonacciSequence.add((long) 0);	// First
			        	writer.write(String.valueOf((long) 0));
			        	writer.newLine();
					}
					else if(i == 1){
						fibonacciSequence.add((long) 1);	// Second
			        	writer.write(String.valueOf((long) 1));
			        	writer.newLine();
					}
					else{
						fibonacciSequence.add(fibonacciSequence.get(i-2)+fibonacciSequence.get(i-1));
						writer.write(String.valueOf((long) fibonacciSequence.get(i-2)+fibonacciSequence.get(i-1)));
			        	writer.newLine();						
					}
				}
		    	writer.flush();
		    	writer.close();		
			}
        }
    }
   
    public void stop(BundleContext context) throws Exception {
//    	org.osgi.framework.storage.clean = false;
    	System.out.println("Server: Stopped");        	
    	fibonacciServiceRegistration.unregister();
    }

    /**
     * When a Client have requested (sequenceSize) number of fibonacci
     * numbers. This method will be called to calculate the next
     * sequence of numbers.
     * @throws IOException 
     */
    public static void calculateNextFibs() {
	        
	        BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for(int i=0; i < sequenceSize; ++i){
					/* Formula:
					 * f(0) = 0;
					 * f(1) = 1;
					 * f(n) = f(n-1)+f(n-2)
					 */
					fibonacciSequence.add(secondLastFib+lastFib);
					writer.write(String.valueOf(secondLastFib+lastFib));
		        	writer.newLine();
		    		
		        	// Update the next fibonacci number
		        	long temp = secondLastFib+lastFib;
		        	secondLastFib = lastFib;
		        	lastFib = temp;
				}
				
		    	writer.flush();
		    	writer.close();	
		    	
			} catch (IOException e1) {
				e1.printStackTrace();
			}

    }
}
