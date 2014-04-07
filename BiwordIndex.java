package ir;

import java.util.Iterator;

public class BiwordIndex implements Index{

	@Override
	public void insert(String token, int docID, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<String> getDictionary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PostingsList getPostings(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PostingsList search(Query query, int queryType, int rankingType,
			int structureType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
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
