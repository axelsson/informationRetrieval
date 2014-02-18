/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


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
		list.insert(docID, offset);
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
						result.insert(b.get(j).docID, offset);
					}
				}
				else{
					result.insert(a.get(i).docID, 1);
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
