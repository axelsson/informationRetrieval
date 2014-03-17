package ir;
/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class PageRank{

	/**  
	 *   Maximal number of documents. We're assuming here that we
	 *   don't have more docs than we can keep in main memory.
	 */
	final static int MAX_NUMBER_OF_DOCS = 2000000;

	/**
	 *   Mapping from document names to document numbers.
	 */
	Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

	/**
	 *   Mapping from document numbers to document names
	 */
	String[] docName = new String[MAX_NUMBER_OF_DOCS];

	/**  
	 *   A memory-efficient representation of the transition matrix.
	 *   The outlinks are represented as a Hashtable, whose keys are 
	 *   the numbers of the documents linked from.<p>
	 *
	 *   The value corresponding to key i is a Hashtable whose keys are 
	 *   all the numbers of documents j that i links to.<p>
	 *
	 *   If there are no outlinks from i, then the value corresponding 
	 *   key i is null.
	 */
	Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

	/**
	 *   The number of outlinks from each node.
	 */
	int[] out = new int[MAX_NUMBER_OF_DOCS];

	/**
	 *   The number of documents with no outlinks.
	 */
	int numberOfSinks = 0;
	int numberOfDocs = 0;

	/**
	 *   The probability that the surfer will be bored, stop
	 *   following links, and take a random jump somewhere.
	 */
	final static double BORED = 0.15;

	/**
	 *   Convergence criterion: Transition probabilities do not 
	 *   change more that EPSILON from one iteration to another.
	 */
	final static double EPSILON = 0.0001;

	/**
	 *   Never do more than this number of iterations regardless
	 *   of whether the transistion probabilities converge or not.
	 */
	final static int MAX_NUMBER_OF_ITERATIONS = 1000;


	/* --------------------------------------------- */


	public PageRank( String filename ) {
		int noOfDocs = readDocs( filename );
		numberOfDocs = noOfDocs;
		computePagerank( noOfDocs );
	}

	/**
	 *   Reads the documents and creates the docs table. When this method 
	 *   finishes executing then the @code{out} vector of outlinks is 
	 *   initialised for each doc, and the @code{p} matrix is filled with
	 *   zeroes (that indicate direct links) and NO_LINK (if there is no
	 *   direct link. <p>
	 *
	 *   @return the number of documents read.
	 */
	int readDocs( String filename ) {
		int fileIndex = 0;
		try {
			System.err.print( "Reading file... " );
			BufferedReader in = new BufferedReader( new FileReader( filename ));
			String line;
			while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
				int index = line.indexOf( ";" );
				String title = line.substring( 0, index );
				Integer fromdoc = docNumber.get( title );
				//  Have we seen this document before?
				if ( fromdoc == null ) {	
					// This is a previously unseen doc, so add it to the table.
					fromdoc = fileIndex++;
					docNumber.put( title, fromdoc );
					docName[fromdoc] = title;
				}
				// Check all outlinks.
				StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
				while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
					String otherTitle = tok.nextToken();
					Integer otherDoc = docNumber.get( otherTitle );
					if ( otherDoc == null ) {
						// This is a previousy unseen doc, so add it to the table.
						otherDoc = fileIndex++;
						docNumber.put( otherTitle, otherDoc );
						docName[otherDoc] = otherTitle;
					}
					// Set the probability to 0 for now, to indicate that there is
					// a link from fromdoc to otherDoc.
					if ( link.get(fromdoc) == null ) {
						link.put(fromdoc, new Hashtable<Integer,Boolean>());
					}
					if ( link.get(fromdoc).get(otherDoc) == null ) {
						link.get(fromdoc).put( otherDoc, true );
						out[fromdoc]++;
					}
				}
			}
			if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
				System.err.print( "stopped reading since documents table is full. " );
			}
			else {
				System.err.print( "done. " );
			}
			// Compute the number of sinks.
			for ( int i=0; i<fileIndex; i++ ) {
				if ( out[i] == 0 )
					numberOfSinks++;
			}
		}
		catch ( FileNotFoundException e ) {
			System.err.println( "File " + filename + " not found!" );
		}
		catch ( IOException e ) {
			System.err.println( "Error reading file " + filename );
		}
		System.err.println( "Read " + fileIndex + " number of documents" );
		return fileIndex;
	}
	/*
	 *   Computes the pagerank of each document.
	 */
	void computePagerank( int numberOfDocs ) {
		//double[][] P = transitionProbabilityMatrix();
		//double[] pageRanks = powerIteration(P);
		double[] pageRanks = approximationMethod();
		PriorityQueue<Rank> ranks = new PriorityQueue<>();
		for (int i = 0; i < pageRanks.length; i++) {
			ranks.add(new Rank(docName[i], pageRanks[i]));
		}
		printPageRank(ranks);
	}

	private double[] approximationMethod(){
		double[] current = new double[numberOfDocs];
		double[] next  = new double[numberOfDocs];
		next[0] = 1;
		int iterations = 0;
		double diff = 1;
		//Hashtable<Integer, Boolean> links = link.get(i);
		while(diff > EPSILON && iterations < MAX_NUMBER_OF_ITERATIONS){
			iterations++;
			for(Entry<Integer, Hashtable<Integer, Boolean>> entry : link.entrySet()){
				int i = entry.getKey();
				for(Entry<Integer, Boolean> links : entry.getValue().entrySet()){
					int j = links.getKey();
					next[j]+=current[i]*(1-BORED)/out[i];
				}
				next[i]+=BORED/numberOfDocs;
				next[i]+=numberOfSinks/numberOfDocs/numberOfDocs;
			}
			diff = diff(current,next);
			if(diff < EPSILON)
				break;
			current = next;
			next = new double[numberOfDocs];
		}
		System.err.println("Ran "+iterations+" iterations.");
		return next;
	}
	/* for every i:
			for every link i->j:
				x�[j] += x[i]*c/out[i]
			x�[i] += (1-c)/N
			x�[i] += s/N/N
	x = x�
	x�= 0
	 */

	private double[] powerIteration(double[][] P){
		double[] current = new double[numberOfDocs];
		double[] next  = new double[numberOfDocs];
		next[0] = 1;
		int iterations = 0;
		while (diff(current, next) > EPSILON && iterations < MAX_NUMBER_OF_ITERATIONS){
			current = next;
			next = multiply(current, P);
			iterations++;
		}
		System.err.println("Ran "+iterations+" iterations.");
		return next;
	}
	//compute transition probability matrix 
	public double[][] transitionProbabilityMatrix(){
		double[][] P = new double[numberOfDocs][numberOfDocs];
		double teleport = (BORED)/numberOfDocs;
		double noLinksValue = (1-BORED)/numberOfDocs+teleport; 
		for (int i = 0; i < P.length; i++) {
			double outLinks = out[i];
			Hashtable<Integer, Boolean> links = link.get(i);
			double gotLinksValue = (1-BORED)*(1/outLinks)+teleport;
			//System.err.println("values tele:"+teleport+" noLinks:"+noLinksValue+" gotlinks:"+gotLinksValue+ " no links:"+outLinks);
			//bara l�gga till om det �r en etta, dvs l�nk mellan i och j finns
			for (int j = 0; j < P.length; j++) {
				if(outLinks == 0){
					P[i][j] = noLinksValue;
				}
				else if(links!=null && links.get(j)!=null && links.get(j)){
					P[i][j] = gotLinksValue;
				} else{
					P[i][j] = teleport;
				}
				//System.err.println("value:"+P[i][j]);
			}
		}
		System.err.println("Done creating P matrix.");
		return P;
	}

	/* --------------------------------------------- */

	public static void main( String[] args ) {
		if ( args.length != 1 ) {
			System.err.println( "Please give the name of the link file" );
		}
		else {
			new PageRank( args[0] );
		}
	}

	/***********************help methods***********************/
	private double diff(double[] current, double[] next) {
		double diff = 0;
		for (int i = 0; i < numberOfDocs; i++) {
			diff +=Math.abs(current[i]-next[i]);
		}
		System.out.println(diff);
		return diff;
	}
	private void printPageRank(PriorityQueue<Rank> ranks){
		int num = 1;
		NumberFormat f = NumberFormat.getInstance (Locale.US); 
		f.setMaximumFractionDigits (6); 
		while(!ranks.isEmpty() && num < 51){
			Rank r = ranks.poll();
			System.err.println(num+". " +r.name+ " "+ r.rank);//f.format(r.rank));
			num++;
		}
	}
	// vector-matrix multiplication (y = x^T A)
	public static double[] multiply(double[] x, double[][] A) {
		int m = A.length;
		int n = A[0].length;
		if (x.length != m) throw new RuntimeException("Illegal matrix dimensions.");
		double[] y = new double[n];
		for (int j = 0; j < n; j++)
			for (int i = 0; i < m; i++)
				y[j] += (A[i][j] * x[i]);
		return y;
	}
}
