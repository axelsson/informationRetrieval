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
		//PostingsEntry entry = new PostingsEntry(docID, 1);
		if (index.containsKey(token)){
			list = index.get(token);
			//if(!(list.getLast().docID == docID))
				list.insert(docID, offset);
		}
		else{
			list = new PostingsList();
			index.put(token, list);
			list.insert(docID, offset);
		}
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
		if(queryType == Index.PHRASE_QUERY){

		}
		else if(queryType == Index.INTERSECTION_QUERY) {
			if(query.size() > 1){
				PostingsList prevResult = index.get(query.terms.get(0));
				for (int i = 1; i < query.size(); i++) {
					prevResult = intersection(prevResult, index.get(query.terms.get(i)));
				}
				result = prevResult;
			}
			else{
				result = index.get(query.terms.get(0));
			}
		}
		return result;
	}

	public PostingsList intersection(PostingsList a, PostingsList b){
		PostingsList result = new PostingsList();
		int i = 0;
		int j = 0;
		while (a.get(i) != null && b.get(j) != null){
			if (a.get(i).docID == b.get(j).docID){
				result.insert(a.get(i).docID, 1);
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
	 *  No need for cleanup in a HashedIndex.
	 */
	public void cleanup() {
	}
}
