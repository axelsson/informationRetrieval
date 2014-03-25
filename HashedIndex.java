/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig KjellstrÃ¶m, 2012-14
 */  


package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

	/** The index as a hashtable. */
	private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


	/**
	 *  Inserts this token in the index.
	 */
	public void insert( String token, int docID, int offset ) {
		PostingsList list;
		if (index.containsKey(token)){
			list = index.get(token);
		}
		else{
			list = new PostingsList();
			index.put(token, list);
		}
		list.insert(docID, offset, 1);
	}


	/**
	 *  Returns all the words in the index.
	 */
	public Iterator<String> getDictionary() {
		return index.keySet().iterator();
	}


	/**
	 *  Returns the postings for a specific term, or null
	 *  if the term is not in the index.
	 */
	public PostingsList getPostings( String token ) {
		return index.get(token);
	}


	/**
	 *  Searches the index for postings matching the query.
	 */
	public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
		PostingsList result = new PostingsList();
		boolean phrase = false;
		if(queryType == Index.PHRASE_QUERY)
			phrase = true;
		if(queryType == Index.RANKED_QUERY)
			result = searchRanked(query);
		else{
			result = doIntersection(query, phrase);
		}
		return result;
	}


	private PostingsList doIntersection(Query query, boolean phrase) {
		PostingsList result;
		if(query.size() > 1){
			PostingsList prevResult = index.get(query.terms.get(0));
			for (int i = 1; i < query.size(); i++) {
				System.err.println("intersection with term: "+query.terms.get(i)+ " prevres size: "+prevResult.size());
				prevResult = intersection(prevResult, index.get(query.terms.get(i)), phrase);
			}
			result = prevResult;
		}
		else{
			result = index.get(query.terms.get(0));
		}
		return result;
	}
	
	private PostingsList searchRanked(Query query) {
		HashMap<Integer, Double> scores = getCosineScores(query);
		PostingsList results = getResultsFromScore(scores);
		Collections.sort(results.toList());
		return results;
	}
/*
 * tf_idf_dt=ftdt*idft/len d
 * idft = ln(N/dft)
*/
	public HashMap<Integer, Double> getCosineScores(Query q){
		HashMap<Integer, Double> scores = new HashMap<Integer,Double>();
		
		int N = docIDs.size();
		int i = 0;
		for (String term : q.terms) {
			PostingsList list = index.get(term);
			int df_t = list.size();
			double idf_t = Math.log(N/df_t);
			double w_tq = idf_t;
			double weight = q.weights.get(i);
			for (PostingsEntry entry : list.toList()) {
				//weight ska in här nånstans
				int wf_td = entry.offsets.size();	//tf_df
				double score = wf_td * w_tq*weight; 
				if(scores.containsKey(entry.docID)){
					scores.put(entry.docID, scores.get(entry.docID) + score);
				}
				else{
					scores.put(entry.docID, score);
				}
			}
			i++;
		}
		return scores;

	}
	
	public PostingsList getResultsFromScore(HashMap<Integer, Double> scores){
		PostingsList results = new PostingsList();
		for (Entry<Integer, Double> entry : scores.entrySet()) {
		    Integer key = entry.getKey();
		    double value = entry.getValue()/(double)docLengths.get(""+key);
		    results.insert(key, 1, value);
		}
		return results;
	}
	
	/**
	 * Intersection for getting the combined result of files containing a term. Creates a new list for the results.
	 * If you search for a phrase, it finds the documents with that phrase and sets the offset to the second searched term. 
	 */
	public PostingsList intersection(PostingsList a, PostingsList b, boolean phrase){
		PostingsList result = new PostingsList();
		int i = 0;
		int j = 0;
		while (a.get(i) != null && b.get(j) != null){
			if (a.get(i).docID == b.get(j).docID){
				if(phrase){
					LinkedList<Integer> first = a.get(i).offsets;
					LinkedList<Integer> second = b.get(j).offsets;
					int offset = gotPhrase(first,second);
					if(offset != -1){
						result.insert(b.get(j).docID, offset, 1);
					}
				}
				else{
					result.insert(a.get(i).docID, 1, 1);
				}
				i++;
				j++;
			}
			else if (a.get(i).docID < b.get(j).docID){
				i++;
			}
			else{
				j++;
			}
		}
		return result;
	}

	/**
	 *  Finds the offset for the second word in a phrase if it exists. Else, returns -1. 
	 */
	public int gotPhrase(LinkedList<Integer> first, LinkedList<Integer> second){
		for (int k = 0; k < first.size(); k++) {
			for (int i = 0; i < second.size(); i++) {
				if (first.get(k)+1 == second.get(i)){
					return second.get(i);
				}
			}
		}
		return -1;
	}
	/**
	 *  No need for cleanup in a HashedIndex.
	 */
	public void cleanup() {
	}


	@Override
	public boolean filesLeft() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void writeToFile() {
		// TODO Auto-generated method stub
		
	}
}
