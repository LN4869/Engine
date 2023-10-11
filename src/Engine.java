import java.util.*;
public class Engine {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Board.setup();
		System.out.println(TT.zobrist);
		//Board.board[11][3]=7;
		/*ArrayList<int[]> moves = Board.gen_moves(0,false);
		for(int[] move : moves) {
			Board.play_move(move);
			Board.unplay_move(move, 0);
			System.out.println(TT.zobrist);
		}*/
		Board.print_board(Board.board);
		System.out.println(Evaluation.evaluate());
		int eval = Search.search(0,0,7,-1000000, 1000000);
		System.out.println(eval);
		System.out.println(TT.zobrist);
		System.out.println(System.currentTimeMillis()-start+" ms");
	}

}
