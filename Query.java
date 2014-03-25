/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig KjellstrÃ¶m, 2012
 */  

package ir;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    terms.add( tok.nextToken() );
	    weights.add( new Double(1) );
	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (LinkedList<String>) terms.clone();
	queryCopy.weights = (LinkedList<Double>) weights.clone();
	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
    	PostingsList relevantDocs = new PostingsList();
    	PostingsList nonRelevantDocs = new PostingsList();
    	double numRelevantDocs = 0;
    	for (int i = 0; i < 10; i++) {
			if(docIsRelevant[i]){
				relevantDocs.add(results.get(i));
				numRelevantDocs++;
			}
			else
				nonRelevantDocs.add(results.get(i));
		}
    	numRelevantDocs = 1.0/numRelevantDocs;
    	//numRelevantDocs = Math.sqrt(numRelevantDocs);
    	normalizeEntries(results);
    	normalizeWeights();
    	
    	double alpha = 1.0;
    	double beta = 0.25;
    	//multiply query terms with alpha
    	for (int i = 0; i < weights.size(); i++) {
    		double w = weights.get(i)*alpha;
    		weights.set(i,w);
		}
    	//add terms for each relevant document
    	for (int i = 0; i < 10; i++) {
			if(docIsRelevant[i]){
				addTerms(results.get(i), indexer, beta);
			}
    	}
    }

	private void addTerms(PostingsEntry entry, Indexer indexer, double beta) {
		ArrayList<String> termsInDoc = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(""+indexer.index.docIDs.get("" +entry.docID))));
			SimpleTokenizer tokenizer = new SimpleTokenizer(br);
			
			while ( tokenizer.hasMoreTokens() ) {
				termsInDoc.add(tokenizer.nextToken());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//för varje uthämtat ord från dokumentet, räkna ihop ny vikt
		for (String term : termsInDoc) {
			double weight = 1.0/indexer.index.getPostings(term).getEntryByDocID(entry.docID).offsets.size();
			if(terms.contains(term)){
				//om termen redan finns, öka på vikt
				int index = terms.indexOf(term);
				weights.set(index, weights.get(index)+weight*beta);
			}
			else{
				//annars lägg till term och vikt
				terms.add(term);
				weights.add(weight*beta);
			}
		}
	}

	private void normalizeWeights() {
		double score = 0;
    	for (Double weight : weights) {
			score = weight*weight;
		}
    	score = Math.sqrt(score);
    	for (int i = 0; i < weights.size();i++) {
    		weights.set(i, weights.get(i)/score);
		}
	}

	private void normalizeEntries(PostingsList results) {
		double score = 0;
    	for (PostingsEntry entry : results.toList()) {
			score += entry.score * entry.score;
		}
    	score = Math.sqrt(score);
    	for (PostingsEntry entry : results.toList()) {
			entry.score /= score;
		}
	}
}

    
