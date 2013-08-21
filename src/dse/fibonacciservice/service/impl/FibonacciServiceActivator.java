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

		// Read/write from mydatafile
		/* Downside:
		 * The data is deleted when starting the application with the -clean option.
		 * This often happens during debugging.
		 * The data is not accessible when the plug-in is updated/replaced.
		 * Different versions of the same plug-in get different plug-in IDs,
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

					getSequenceProperty(context);

					secondLastFib = -1;
					lastFib = -1;
					
					// Read the persistence data file and retrieve the last two Fibonacci numbers.					
					retrieveLastTwoFibs(reader, line);

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

						// Update the next Fibonacci number
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

			/**
			 *  Persistence data file (fibsequence.dat) not found.
			 *  
			 */			
			if(firstRun){
				// On first run
				System.out.println("Server: First run");

				// If the configuration property isn't set, choose the default sequence size (4).
				getSequenceProperty(context);

				// Create the first (sequenceSize) Fibonacci sequence numbers and
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

	private static void retrieveLastTwoFibs(BufferedReader reader, String line)
			throws IOException {
		// We need to fetch at least two numbers from the persistence data
		int evenOdd = 0;
		while(line != null){
			
			if(evenOdd % 2 == 0){
				secondLastFib = Long.valueOf(line);							
			}
			else{
				lastFib = Long.valueOf(line);
			}

			++evenOdd;
			line = reader.readLine();
		}

		// Assure that lastFib and secondLastFib is in the correct order...
		// As odd Fibonacci sequenceSize, requires a swap!
		long tempSwap = -1;
		if(secondLastFib > lastFib){
			tempSwap = lastFib;
			lastFib = secondLastFib;
			secondLastFib = tempSwap;
		}
		// Otherwise do nothing!
	}

	/**
	 * Retrieve the OSGi configuration property, if such property isn't already set,
	 * set the sequenceSize to the default sequence size (4).
	 *  
	 * @param context
	 */
	private static void getSequenceProperty(BundleContext context) {
		// If the configuration property isn't set, choose the default sequence size (4).
		if(context.getProperty("dse.fibonacci.service.fibsize") != null){
			sequenceSize = Integer.valueOf(context.getProperty("dse.fibonacci.service.fibsize"));        	
		}
		else{
			sequenceSize = defaultSequenceSize;
		}
	}

	public void stop(BundleContext context) throws Exception {
		System.out.println("Server: Stopped");    
		bc = null;
		thread.stopThread();
		fibonacciServiceRegistration.unregister();
	}

	/**
	 * When a Client have requested (sequenceSize) number of Fibonacci
	 * numbers. This method will be called to calculate the next
	 * sequence of Fibonacci numbers.
	 * 
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
				getSequenceProperty(bc);
				
				secondLastFib = -1;
				lastFib = -1;
				boolean maximumFib = false;
				
				retrieveLastTwoFibs(reader, line);
				
				if((lastFib < -1) || (secondLastFib < -1) ){
					maximumFib = true;
				}
			
//				System.out.println("SecondLastFib: " + secondLastFib);				
//				System.out.println("LastFib: " + lastFib);
				
				
				
				// We have reached the maximum Long value number 93 in the Fibonacci sequence,
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
							// Unnecessary to do a Long.MAX_VALUE check, as these are the first
							// Fibonacci sequence.
							getFibonacciSequence().add(getFibonacciSequence().get(i-2)+getFibonacciSequence().get(i-1));
							writer.write(String.valueOf((long) getFibonacciSequence().get(i-2)+getFibonacciSequence().get(i-1)));
							writer.newLine();						
						}
					}

				}
				// Otherwise continue to calculate Fibonacci numbers.
				else{
					writer = new BufferedWriter(new FileWriter(fibsequence));
					for(int i=0; i < sequenceSize; ++i){
						/* Formula: Calculating Fibonacci numbers
						 * f(0) = 0;
						 * f(1) = 1;
						 * f(n) = f(n-1)+f(n-2)
						 */

						// We have reached the maximum long value.
						// Restart sequence.				

						// Update the next Fibonacci number
						long temp = secondLastFib+lastFib;

						if(secondLastFib+lastFib < 0){
							secondLastFib = lastFib;
							lastFib = 0;
							
							getFibonacciSequence().add(lastFib);
							writer.write(String.valueOf(lastFib));
							writer.newLine();
						}
						else if(lastFib == 0){
							secondLastFib = lastFib;
							lastFib = 1;
							
							getFibonacciSequence().add(lastFib);
							writer.write(String.valueOf(lastFib));
							writer.newLine();							
						}
						else{
							getFibonacciSequence().add(secondLastFib+lastFib);
							writer.write(String.valueOf(secondLastFib+lastFib));
							writer.newLine();
							
							secondLastFib = lastFib;
							lastFib = temp;
						}						
					}
				}
				
				// Flush and Close BufferedWriter  
				writer.flush();
				writer.close();	
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
 * This thread calculates the next series of Fibonacci numbers.
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