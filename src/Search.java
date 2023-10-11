import java.util.*;
public class Search {
	
	static int inf = 1000000000;
	static int max_depth = 7;

	
	static void iterative_deepening() {
		for(int i=0; i<=max_depth; i++) {
			search(Board.to_move,0,i,-inf,inf);
		}
	}
	
	static int search(int color, int depth, int max, int alpha, int beta){
		ArrayList<int[]> moves = Board.gen_moves(color,false);
		if(moves.size()==0) {
			if(Board.is_checked(color))return -inf;
			else return 0;
		}
		if(depth==max) {
			return q_search((color+1)%4, -beta,-alpha);
			/*switch(color%2) {
			case 0: return Evaluation.evaluate();
			case 1: return -Evaluation.evaluate();
			}*/
		}
		int pre = Board.en_passant[color];
		for(int[] move : moves) {
			Board.play_move(move);
			int evaluation = -search((color+1)%4, depth+1, max,-beta,-alpha);
			Board.unplay_move(move,pre);
			if(evaluation>=beta) {
				return beta;
			}
			if(evaluation>alpha) {
				alpha = evaluation;
			}
		}
		return alpha;
	}
	
	static int q_search(int color, int alpha, int beta) {
		int eval=0;
		switch(color%2) {
		case 0:
			eval = Evaluation.evaluate(); 
			break;
		case 1:
			eval = -Evaluation.evaluate();
			break;
		}
		if(eval>=beta) return beta;
		alpha = Math.max(alpha, eval);
		ArrayList<int[]> moves = Board.gen_moves(color, true);
		int pre = Board.en_passant[color];
		for(int[] move : moves) {
			Board.play_move(move);
			eval = -q_search((color+1)%4, -beta,-alpha);
			Board.unplay_move(move,pre);
			if(eval>=beta)return beta;
			alpha = Math.max(alpha, eval);
		}
		return alpha;
	}
	
}
