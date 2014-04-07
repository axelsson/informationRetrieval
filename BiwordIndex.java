/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig KjellstrÃ¶m, 2012-14
 */  
package ir;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class BiwordIndex implements Index {
	String prevToken = "";
	int prevDocID = -1;
	int prevOffset = -1;
	/** The index as a hashtable. */
	private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
	

	/**
	 *  Inserts this token in the index.
	 */
	public void insert( String token, int docID, int offset ) {
		//första kanske inte ska tas med
		PostingsList list;
		String newToken;
		if(prevDocID != docID){
			prevDocID = docID;
			prevOffset = offset;
			prevToken = "";
			newToken = token;
		}
		else
			newToken = prevToken+" "+token;
		if (index.containsKey(newToken)){
			list = index.get(newToken);
		}
		else{
			list = new PostingsList();
			index.put(newToken, list);
		}
		list.insert(prevDocID, prevOffset, 1);
		prevDocID = docID;
		prevOffset = offset;
		prevToken = token;
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
		double startTime = System.nanoTime();
		if(query.terms.size() > 1)
			makeQueryBiword(query);
		if(queryType == Index.RANKED_QUERY)
			result = searchRanked(query);
		double endTime = System.nanoTime();
		System.out.println(query.terms.toString() + " "+(endTime-startTime)+" ns.");
		return result;
	}

	private void makeQueryBiword(Query query) {
		Query oldQuery = query.copy();
		query.terms.clear();
		query.weights.clear();
		for (int i = 0; i < oldQuery.terms.size()-1; i++) {
			query.terms.add(i, oldQuery.terms.get(i)+ " "+oldQuery.terms.get(i+1));
			query.weights.add(1.0);
		}
	}

	public HashMap<Integer, Double> getCosineScores(Query q){
		HashMap<Integer, Double> scores = new HashMap<Integer,Double>();
		
		int N = docIDs.size();
		int i = 0;
		for (String term : q.terms) {
			PostingsList list = index.get(term);
			if(list == null)
				continue;
			int df_t = list.size();
			double idf_t = Math.log(N/df_t);
			double w_tq = idf_t;
			double weight = q.weights.get(i);
			for (PostingsEntry entry : list.toList()) {
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
	
	private PostingsList searchRanked(Query query) {
		HashMap<Integer, Double> scores = getCosineScores(query);
		PostingsList results = getResultsFromScore(scores);
		Collections.sort(results.toList());
		return results;
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
