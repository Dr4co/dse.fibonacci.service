package dse.fibonacciservice.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import api.FibonacciService;

public class FibonacciServiceActivator implements BundleActivator {

	private static ArrayList<Long> fibonacciSequence;
	public static boolean unused = true;
	public static int sequenceSize;
	public final static int defaultSequenceSize = 4; 
	public static long secondLastFib, lastFib;
	public static BundleContext bc;
	
    private FibonacciCalculatorThread thread = null;

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

		boolean firstRun = false;

		setFibonacciSequence(new ArrayList<Long>());	// Initialize ArrayList
		
		if(fibsequence == null){
			// No file system support!
			System.out.println("No file system support!");
		}
		else{
			try{
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
						getFibonacciSequence().add(secondLastFib+lastFib);
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
				else{
					firstRun = true;
				}
			}
			catch(FileNotFoundException fnfe){
				firstRun = true;
				// No previous persistent data found
			}

			if(firstRun){
				// On first run
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
				setFibonacciSequence(new ArrayList<Long>());	// Initialize ArrayList


				for(int i=0; i < sequenceSize; ++i){
					/* Formula:
					 * f(0) = 0;
					 * f(1) = 1;
					 * f(n) = f(n-1)+f(n-2)
					 */
					if(i == 0){
						getFibonacciSequence().add((long) 0);	// First
						writer.write(String.valueOf((long) 0));
						writer.newLine();
					}
					else if(i == 1){
						getFibonacciSequence().add((long) 1);	// Second
						writer.write(String.valueOf((long) 1));
						writer.newLine();
					}
					else{
						getFibonacciSequence().add(getFibonacciSequence().get(i-2)+getFibonacciSequence().get(i-1));
						writer.write(String.valueOf((long) getFibonacciSequence().get(i-2)+getFibonacciSequence().get(i-1)));
						writer.newLine();						
					}
				}
				writer.flush();
				writer.close();		
			}
		}
				
		thread = new FibonacciCalculatorThread();
		thread.start();
		
	}

	public void stop(BundleContext context) throws Exception {
		System.out.println("Server: Stopped");    
		bc = null;
		thread.stopThread();
		fibonacciServiceRegistration.unregister();
	}

	/**
	 * When a Client have requested (sequenceSize) number of fibonacci
	 * numbers. This method will be called to calculate the next
	 * sequence of numbers.
	 * @throws IOException 
	 */
	public static void calculateNextFibs() throws IOException {

		File fibsequence = bc.getDataFile(fileName);
		BufferedWriter writer; 
		BufferedReader reader;
		
		try{
			reader = new BufferedReader(new FileReader(fibsequence));   

			String line = reader.readLine();
			if(line != null){
				// If the configuration property isn't set, choose the default sequence size (4).
				if(bc.getProperty("dse.fibonacci.service.fibsize") != null){
					sequenceSize = Integer.valueOf(bc.getProperty("dse.fibonacci.service.fibsize"));        	
				}
				else{
					sequenceSize = defaultSequenceSize;
				}

				secondLastFib = -1;
				lastFib = -1;
				boolean maximumFib = false;
				for(int i=0; i < sequenceSize; ++i){
					// Only the last two numbers are of interest
					if(i == sequenceSize-2){
						secondLastFib = Long.valueOf(line);
					}
					else if(i == sequenceSize-1){
						lastFib = Long.valueOf(line);
					}
					line = reader.readLine();
					
					if((lastFib < -1) || (secondLastFib < -1) ){
						maximumFib = true;
						break;
					}
				}
//				System.out.println("SecondLastFib: " + secondLastFib);				
//				System.out.println("LastFib: " + lastFib);
				
				// We have reached the maximum Long value number 93 in the fibonacci sequence,
				// restart again...
				if(maximumFib){
					writer = new BufferedWriter(new FileWriter(fibsequence));
					for(int i=0; i < sequenceSize; ++i){
						/* Formula:
						 * f(0) = 0;
						 * f(1) = 1;
						 * f(n) = f(n-1)+f(n-2)
						 */
						if(i == 0){
							getFibonacciSequence().add((long) 0);	// First
							writer.write(String.valueOf((long) 0));
							writer.newLine();
						}
						else if(i == 1){
							getFibonacciSequence().add((long) 1);	// Second
							writer.write(String.valueOf((long) 1));
							writer.newLine();
						}
						else{
							getFibonacciSequence().add(getFibonacciSequence().get(i-2)+getFibonacciSequence().get(i-1));
							writer.write(String.valueOf((long) getFibonacciSequence().get(i-2)+getFibonacciSequence().get(i-1)));
							writer.newLine();						
						}
					}
					writer.flush();
					writer.close();	
				}
				// Otherwise continue to calculate fibonacci numbers.
				else{
					writer = new BufferedWriter(new FileWriter(fibsequence));
					for(int i=0; i < sequenceSize; ++i){
						/* Formula: Calculating Fibonacci numbers
						 * f(0) = 0;
						 * f(1) = 1;
						 * f(n) = f(n-1)+f(n-2)
						 */
						
						getFibonacciSequence().add(secondLastFib+lastFib);
						writer.write(String.valueOf(secondLastFib+lastFib));
						writer.newLine();

						// Update the next fibonacci number
						long temp = secondLastFib+lastFib;
						
						// We have reached the maximum long value.
						// Restart sequence.
						if(temp < -1){
							secondLastFib = 0;
							lastFib = 1;
						}else{
							secondLastFib = lastFib;
							lastFib = temp;	
						}							
					}

					writer.flush();
					writer.close();
				}
			}

		}
		catch(FileNotFoundException fnfe){
			fnfe.printStackTrace();
			// No previous persistent data found
			// This case should not occur... unless the persistence file is manually deleted.
		}
		
	}
	
	
	public static boolean sequenceEmpty(){
		return getFibonacciSequence().isEmpty();
	}
	
	public static long getFibNumber(){
		long temp = getFibonacciSequence().get(0);
		getFibonacciSequence().remove(0);
		return temp;
	}

	public static ArrayList<Long> getFibonacciSequence() {
		return fibonacciSequence;
	}

	public static void setFibonacciSequence(ArrayList<Long> fibonacciSequence) {
		FibonacciServiceActivator.fibonacciSequence = fibonacciSequence;
	}
}

/**
 * This thread calculates the next series of fibonacci numbers.
 * 
 * When the maximum long value is reached a negative number is returned.
 * To fix this problem, future improvements should implement the BigInteger data type.
 * @author Chau
 */
class FibonacciCalculatorThread extends Thread {

	private boolean running = true;
	public FibonacciCalculatorThread() {}
	
	public void run() {
		while (running) {			    
			try {
				try {
					FibonacciServiceActivator.calculateNextFibs();
//					System.out.println("debug: " + FibonacciServiceActivator.getFibonacciSequence());
				} catch (IOException e) {
					e.printStackTrace();
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println("FibonacciServiceCalulatorThread ERROR: " + e);
			}
		}
	}
	
	public void stopThread() {
		this.running = false;
	}
}