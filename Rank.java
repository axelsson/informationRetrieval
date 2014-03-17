package ir;

public class Rank implements Comparable<Rank>{
	public String name;
	public double rank;
	public Rank(String n, double r){
		name = n;
		rank = r;
	}
	@Override
	public int compareTo (Rank r){ 
		if (rank > r.rank) 
			return -1; 
		if (rank < r.rank) 
			return 1; 
		return 0; 
	} 
}
