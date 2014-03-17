/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class SavedIndex implements Index {

	/** The index as a hashtable. */
	private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
	int previousFile = -1;
	int files = 0;

	public boolean filesLeft(){
		if (files > 0){
			return true;
		}
		return false;
	}
	/**
	 *  Inserts this token in the index.
	 */
	public void insert( String token, int docID, int offset ) {
		if(docID != previousFile){
			files++;
			System.err.println(files);
			previousFile = docID;
			if(files > 500){
				writeToFile();
				files = 0;
				index.clear();
			}
		}
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

	public void writeToFile(){
		try{
		for (Entry<String, PostingsList> entry : index.entrySet()) {
			if(entry.getKey().equals(""))
				continue;
			String filename = SearchGUI.path+"/"+entry.getKey();
			File file = new File(filename);
			if(!file.exists()){
				file.createNewFile();
			}
			BufferedWriter f = new BufferedWriter(new FileWriter(file, true));
			PostingsList list = entry.getValue();
			for (int i = 0; i < entry.getValue().size(); i++) {
				PostingsEntry elem = list.get(i);
				StringBuilder sb = new StringBuilder();
				LinkedList offsets = elem.offsets;
				//System.err.println("docID:"+elem.docID);
				sb.append(elem.docID).append(' ');
				
				for (int j = 0; j < offsets.size(); j++) {
					//System.err.println("offset: "+offsets.get(j));
					sb.append(offsets.get(j)).append(' ');
				}
				sb.append("\n");
				f.append(sb);
				//System.err.println("sb: "+sb);
			}
			f.close();
		}} catch(IOException e ){
			e.printStackTrace();
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
		PostingsList result = new PostingsList();
		try(BufferedReader br = new BufferedReader(new FileReader(new File(SearchGUI.path)))){
			String[] line = br.readLine().split(" ");
			while(line!=null){
				PostingsEntry entry = new PostingsEntry(Integer.parseInt(line[0]), 1, Integer.parseInt(line[1]));
				for (int i = 2; i < line.length; i++) {
					entry.insertOffset(Integer.parseInt(line[i]));
				}
				line = br.readLine().split(" ");
			}
		} catch (Exception e) {
			return null;
		}
		return result;
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
			PostingsList prevResult = getPostings(query.terms.get(0));
			for (int i = 1; i < query.size(); i++) {
				System.err.println("intersection with term: "+query.terms.get(i)+ " prevres size: "+prevResult.size());
				prevResult = intersection(prevResult, getPostings(query.terms.get(i)), phrase);
			}
			result = prevResult;
		}
		else{
			result = getPostings(query.terms.get(0));
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
						result.insert(b.get(j).docID, offset,1);
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
}
