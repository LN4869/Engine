import java.util.*;
public class Evaluation {
	
	static int[] piece_values = new int[6];
	
	public static void main(String[] args) {
	}
	
	static int evaluate() {
		int eval=0;
		for(int i=0; i<14; i++) {
			for(int j=0; j<14; j++) {
				if(Board.board[i][j]!=-1&&Board.board[i][j]!=-3) {
					if((Board.board[i][j]/6)%2==0) {
						eval+=piece_values[Math.abs(Board.board[i][j])%6];
					}
					else {
						eval-=piece_values[Math.abs(Board.board[i][j])%6];
					}
				}
			}
		}
		return eval;
	}
}
