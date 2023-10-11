import java.util.*;

public class TT {
	static long[][][] random_long = new long[24][14][14];
	static Entry[] tt = new Entry[1048576];
	static long zobrist;
	public static class Entry {
		long key;
		int depth;
		int[] move;
		int score;
		int bound;
		Entry(long k, int d, int[] m, int s, int b) {
			this.key=k;
			this.depth=d;
			this.move=m;
			this.score=s;
			this.bound=b;
		}
	}
	
	static void initiate_zobrist() {
		Random rand = new Random();
		for(int k=0; k<24; k++) {
			for(int i=0; i<14; i++) {
				for(int j=0; j<14; j++) {
					random_long[k][i][j]=Math.abs(rand.nextLong());
				}
			}
		}
		for(int i=0; i<14; i++) {
			for(int j=0; j<14; j++) {
				if(Board.board[i][j]!=-1&&Board.board[i][j]!=-3) {
					zobrist^=random_long[Math.abs(Board.board[i][j])][i][j];
				}
			}
		}
	}
	
	static int probe_hash(int depth, int alpha, int beta) {
		Entry entry = tt[(int)(zobrist%tt.length)];
		if(entry==null)return Integer.MIN_VALUE;
		if(entry.key==TT.zobrist) {
			if(entry.depth>=depth) {
				if(entry.bound==0) return entry.score;
				if(entry.bound==1&&entry.score>=beta)return beta;
				if(entry.bound==2&&entry.score<=alpha)return alpha;
			}
		}
		return Integer.MIN_VALUE;
	}
	
}
