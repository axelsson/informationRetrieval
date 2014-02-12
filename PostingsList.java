/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {

	/** The postings list as a linked list. */
	private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();


	/**  Number of postings in this list  */
	public int size() {
		return list.size();
	}

	public boolean contains( PostingsEntry entry){
		return list.contains(entry);
	}

	/**  Returns the ith posting */
	public PostingsEntry get( int i ) {
		try {
			return list.get( i );
		}
		catch( IndexOutOfBoundsException e){
			return null;
		}
	}
	
	public PostingsEntry getLast(){
		return list.getLast();
	}

	public void insert(int docID, int offset){
		if(list.size() == 0 || list.getLast().docID != docID){
			PostingsEntry entry = new PostingsEntry(docID, 1, offset);
			list.add(entry);
		}
		else if(list.getLast().docID != docID){
			PostingsEntry entry = new PostingsEntry(docID, 1, offset);
			list.add(entry);
		}
		else{
			PostingsEntry entry = list.getLast();
			entry.insertOffset(offset);
		}
	}
}



