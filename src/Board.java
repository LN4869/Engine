import java.util.*;
/*
 * pieces represented through numbers:
 * 0-5=red
 * 6-11=blue
 * 12-17=yellow
 * 18-23=green
 * red pieces:
 * king=0
 * queen=1
 * rook=2
 * bishop=3
 * knight=4
 * pawn=5
 * 
 * -1 = empty square
 * -3 = illegal square(on one of the edges)
 */

public class Board {
	
	static int[][] board;
	static ArrayList<int[]> moves;
	static int[][] king_pos = new int[4][2];
	static int to_move;
	static int[] king_moved = new int[4];
	static int[] qr_moved = new int[4];				//queenside rook
	static int[] kr_moved = new int[4];				//kingside rook
	static int[] en_passant = new int[4];
	
	public static void main(String[] args) {
		setup();
		print_board(board);
	}
	
	public static void setup() {
		int[][] b = {
				{-3,-3,-3,14,16,15,12,13,15,16,-14,-3,-3,-3},
				{-3,-3,-3,17,17,17,17,17,17,17,17,-3,-3,-3},
				{-3,-3,-3,-1,-1,-1,-1,-1,-1,-1,-1,-3,-3,-3},
				{-8,11,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,23,20},
				{10,11,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,23,22},
				{9,11,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,23,21},
				{7,11,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,23,18},
				{6,11,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,23,19},
				{9,11,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,23,21},
				{10,11,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,23,22},
				{8,11,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,23,-20},
				{-3,-3,-3,-1,-1,-1,-1,-1,-1,-1,-1,-3,-3,-3},
				{-3,-3,-3,5,5,5,5,5,5,5,5,-3,-3,-3},
				{-3,-3,-3,-2,4,3,1,0,3,4,2,-3,-3,-3}
		};
		board = b;
		king_pos[0][0]=13;
		king_pos[0][1]=7;
		king_pos[1][0]=7;
		king_pos[1][1]=0;
		king_pos[2][0]=0;
		king_pos[2][1]=6;
		king_pos[3][0]=6;
		king_pos[3][1]=13;
		Evaluation.piece_values[0]=2000000000;
		Evaluation.piece_values[1]=950;
		Evaluation.piece_values[2]=500;
		Evaluation.piece_values[3]=330;
		Evaluation.piece_values[4]=300;
		Evaluation.piece_values[5]=90;
		for(int i=0; i<4; i++)en_passant[i]=-1;
		TT.initiate_zobrist();
	}
	
	static void print_board(int[][] b) {
		for(int i=0; i<14; i++) {
			for(int j=0; j<14; j++) {
				if(b[i][j]==-10) {
					System.out.print(" o ");
					continue;
				}
				if(b[i][j]==-3)System.out.print(" x ");
				else if(b[i][j]==-1)System.out.print(" _ ");
				else if(Math.abs(b[i][j])/6==0)System.out.print("r");
				else if(Math.abs(b[i][j])/6==1)System.out.print("b");
				else if(Math.abs(b[i][j])/6==2)System.out.print("y");
				else if(Math.abs(b[i][j])/6==3)System.out.print("g");
				if(b[i][j]%6==0)System.out.print("K ");
				else if(b[i][j]%6==1)System.out.print("Q ");
				else if(b[i][j]%6==2)System.out.print("R ");
				else if(b[i][j]%6==-2)System.out.print("r ");
				else if(b[i][j]%6==3)System.out.print("B ");
				else if(b[i][j]%6==4)System.out.print("N ");
				else if(b[i][j]%6==5)System.out.print("P ");
			}
			System.out.println();
		}
		System.out.println();
		/*System.out.println(b[king_pos[0][0]][king_pos[0][1]]);
		System.out.println(b[king_pos[1][0]][king_pos[1][1]]);
		System.out.println(b[king_pos[2][0]][king_pos[2][1]]);
		System.out.println(b[king_pos[3][0]][king_pos[3][1]]);*/
	}
	
	static ArrayList<int[]> gen_moves(int color, boolean nq_moves) {
		ArrayList<int[]> moves = new ArrayList();
		boolean kc = knight_attacked(king_pos[color][0], king_pos[color][1], color);
			for(int i=0; i<14; i++) {
				for(int j=0; j<14; j++) {
					if(board[i][j]==-1||board[i][j]/6!=color||board[i][j]==-3)continue;
					switch(Math.abs(board[i][j])%6) {
					case 0:
						king_moves(i,j,color,moves,kc,nq_moves);
						break;
					case 1:
						queen_moves(i,j,color,moves,kc,nq_moves);
						break;
					case 2:
						rook_moves(i,j,color,moves,kc,nq_moves);
						break;
					case 3:
						bishop_moves(i,j,color,moves,kc,nq_moves);
						break;
					case 4:
						knight_moves(i,j,color,moves,kc,nq_moves);
						break;
					case 5:
						pawn_moves(i,j,color,moves,kc,nq_moves);
						break;
					}
				}
			}
		return moves;
	}
	
	static void king_moves(int a, int b, int color, ArrayList<int[]> list,boolean kc, boolean nq_moves) {
		int[] ar;
		if(a-1>=0) {
			if(b-1>=0&&free_square(a-1,b-1,color)) {
				add_move(a,b,a-1,b-1,color,list,kc,nq_moves);
			}
			if(free_square(a-1,b,color)) {
				add_move(a,b,a-1,b,color,list,kc,nq_moves);
			}
			if(b+1<14&&free_square(a-1,b+1,color)) {
				add_move(a,b,a-1,b+1,color,list,kc,nq_moves);
			}
		}
		if(b-1>=0&&free_square(a,b-1,color)) {
			add_move(a,b,a,b-1,color,list,kc,nq_moves);
		}
		if(b+1<14&&free_square(a,b+1,color)) {
			add_move(a,b,a,b+1,color,list,kc,nq_moves);
		}
		if(a+1<14) {
			if(b-1>=0&&free_square(a+1,b-1,color)) {
				add_move(a,b,a+1,b-1,color,list,kc,nq_moves);
			}
			if(free_square(a+1,b,color)) {
				add_move(a,b,a+1,b,color,list,kc,nq_moves);
			}
			if(b+1<14&&free_square(a+1,b+1,color)) {
				add_move(a,b,a+1,b+1,color,list,kc,nq_moves);
			}
		}
		if(king_moved[color]==0&&qr_moved[color]==0||kr_moved[color]==0) {
			if(!is_attacked(a,b,color)) {
					if(kcastle_possible(color)) {
					switch(color) {
					case 0:
						int[] gr = {13,7,13,9,13,10,13,8};
						list.add(gr);
						break;
					case 1:
						int[] br = {7,0,9,0,10,0,8,0};
						list.add(br);
						break;
					case 2:
						int[] cr = {0,6,0,4,0,3,0,5};
						list.add(cr);
						break;
					case 3:
						int[] dr = {6,13,4,13,3,13,5,13};
						list.add(dr);
						break;
					}
				}
				if(qcastle_possible(color)) {
					switch(color) {
					case 0:
						int[] gr = {13,7,13,5,13,3,13,6};
						list.add(gr);
						break;
					case 1:
						int[] br = {7,0,5,0,3,0,6,0};
						list.add(br);
						break;
					case 2:
						int[] cr = {0,6,0,8,0,10,0,7};
						list.add(cr);
						break;
					case 3:
						int[] dr = {6,13,8,13,10,13,7,13};
						list.add(dr);
						break;
					}
				}
			}
		}
	}
	
	static void queen_moves(int a, int b, int color, ArrayList<int[]> list,boolean kc,boolean nq_moves) {
		rook_moves(a,b,color,list,kc,nq_moves);
		bishop_moves(a,b,color,list,kc,nq_moves);
	}
	
	static void rook_moves(int a, int b, int color, ArrayList<int[]> list,boolean kc, boolean nq_moves) {
		int d=1;
		while(a+d<14&&board[a+d][b]!=-3) {
			if(board[a+d][b]==-1) {
				add_move(a,b,a+d,b,color,list,kc,nq_moves);
			}
			else if(same_team(board[a+d][b]/6, color))break;
			else {
				add_move(a,b,a+d,b,color,list,kc,nq_moves);
				break;
			}
			++d;
		}
		d=1;
		while(b+d<14&&board[a][b+d]!=-3) {
			if(board[a][b+d]==-1) {
				add_move(a,b,a,b+d,color,list,kc,nq_moves);
			}
			else if(same_team(board[a][b+d]/6, color))break;
			else {
				add_move(a,b,a,b+d,color,list,kc,nq_moves);
				break;
			}
			++d;
		}
		d=1;
		while(a-d>=0&&board[a-d][b]!=-3) {
			if(board[a-d][b]==-1) {
				add_move(a,b,a-d,b,color,list,kc,nq_moves);
			}
			else if(same_team(board[a-d][b]/6, color))break;
			else {
				add_move(a,b,a-d,b,color,list,kc,nq_moves);
				break;
			}
			++d;
		}
		d=1;
		while(b-d>=0&&board[a][b-d]!=-3) {
			if(board[a][b-d]==-1) {
				add_move(a,b,a,b-d,color,list,kc,nq_moves);
			}
			else if(same_team(board[a][b-d]/6, color))break;
			else {
				add_move(a,b,a,b-d,color,list,kc,nq_moves);
				break;
			}
			++d;
		}
	}
	
	static void bishop_moves(int a, int b, int color, ArrayList<int[]> list, boolean kc,boolean nq_moves) {
		int d=1;
		while(a+d<14&&b+d<14&&board[a+d][b+d]!=-3) {
			if(board[a+d][b+d]==-1) {
				add_move(a,b,a+d,b+d,color,list,kc,nq_moves);
			}
			else if(same_team(board[a+d][b+d]/6, color))break;
			else {
				add_move(a,b,a+d,b+d,color,list,kc,nq_moves);
				break;
			}
			++d;
		}
		d=1;
		while(b+d<14&&a-d>=0&&board[a-d][b+d]!=-3) {
			if(board[a-d][b+d]==-1) {
				add_move(a,b,a-d,b+d,color,list,kc,nq_moves);
			}
			else if(same_team(board[a-d][b+d]/6, color))break;
			else {
				add_move(a,b,a-d,b+d,color,list,kc,nq_moves);
				break;
			}
			++d;
		}
		d=1;
		while(a-d>=0&&b-d>=0&&board[a-d][b-d]!=-3) {
			if(board[a-d][b-d]==-1) {
				add_move(a,b,a-d,b-d,color,list,kc,nq_moves);
			}
			else if(same_team(board[a-d][b-d]/6, color))break;
			else {
				add_move(a,b,a-d,b-d,color,list,kc,nq_moves);
				break;
			}
			++d;
		}
		d=1;
		while(b-d>=0&&a+d<14&&board[a+d][b-d]!=-3) {
			if(board[a+d][b-d]==-1) {
				add_move(a,b,a+d,b-d,color,list,kc,nq_moves);
			}
			else if(same_team(board[a+d][b-d]/6, color))break;
			else {
				add_move(a,b,a+d,b-d,color,list,kc,nq_moves);
				break;
			}
			++d;
		}
	}
	
	static void knight_moves(int a, int b, int color, ArrayList<int[]> list,boolean kc,boolean nq_moves) {
		if(a-2>=0) {
			if(b-1>=0&&board[a-2][b-1]!=-3&&(board[a-2][b-1]==-1||!same_team(board[a-2][b-1]/6, color))) {
				add_move(a,b,a-2,b-1,color,list,kc,nq_moves);
			}
			if(b+1<14&&board[a-2][b+1]!=-3&&(board[a-2][b+1]==-1||!same_team(board[a-2][b+1]/6, color))) {
				add_move(a,b,a-2,b+1,color,list,kc,nq_moves);
			}
		}
		if(a+2<14) {
			if(b-1>=0&&board[a+2][b-1]!=-3&&(board[a+2][b-1]==-1||!same_team(board[a+2][b-1]/6, color))) {
				add_move(a,b,a+2,b-1,color,list,kc,nq_moves);
			}
			if(b+1<14&&board[a+2][b+1]!=-3&&(board[a+2][b+1]==-1||!same_team(board[a+2][b+1]/6, color))) {
				add_move(a,b,a+2,b+1,color,list,kc,nq_moves);
			}
		}
		if(a-1>=0) {
			if(b-2>=0&&board[a-1][b-2]!=-3&&(board[a-1][b-2]==-1||!same_team(board[a-1][b-2]/6, color))) {
				add_move(a,b,a-1,b-2,color,list,kc,nq_moves);
			}
			if(b+2<14&&board[a-1][b+2]!=-3&&(board[a-1][b+2]==-1||!same_team(board[a-1][b+2]/6, color))) {
				add_move(a,b,a-1,b+2,color,list,kc,nq_moves);
			}
		}
		if(a+1<14) {
			if(b-2>=0&&board[a+1][b-2]!=-3&&(board[a+1][b-2]==-1||!same_team(board[a+1][b-2]/6, color))) {
				add_move(a,b,a+1,b-2,color,list,kc,nq_moves);
			}
			if(b+2<14&&board[a+1][b+2]!=-3&&(board[a+1][b+2]==-1||!same_team(board[a+1][b+2]/6, color))) {
				add_move(a,b,a+1,b+2,color,list,kc,nq_moves);
			}
		}
	}
	
	static void pawn_moves(int a, int b, int color, ArrayList<int[]> list,boolean kc,boolean nq_moves) {
		switch(color) {
		case 0:
			if(a==12) {
				if(board[a-1][b]==-1&&board[a-2][b]==-1) {
					add_move(a,b,a-2,b,color,list,kc,nq_moves);
				}
			}
			if(board[a-1][b]==-1){
				if(is_promotion(a,color))add_promotion_move(a,b,a-1,b,color,list);
				else add_move(a,b,a-1,b,color,list,kc,nq_moves);
			}
			if(b-1>=0&&board[a-1][b-1]!=-3&&board[a-1][b-1]!=-1&&!same_team(board[a-1][b-1]/6, color)) {
				if(is_promotion(a,color))add_promotion_move(a,b,a-1,b-1,color,list);
				else add_move(a,b,a-1,b-1,color,list,kc,nq_moves);
			}
			if(b+1<14&&board[a-1][b+1]!=-3&&board[a-1][b+1]!=-1&&!same_team(board[a-1][b+1]/6, color)) {
				if(is_promotion(a,color))add_promotion_move(a,b,a-1,b+1,color,list);
				else add_move(a,b,a-1,b+1,color,list,kc,nq_moves);
			}
			if(en_passant[1]>=0&&b==3&&a==en_passant[1]+1&&board[a-1][b]==11) {
				if(is_promotion(a,color))ep_promotion(a,b,a-1,b-1,a-1,b,color,list);
				else ep(a,b,a-1,b-1,a-1,b,color,list);
			}
			if(en_passant[3]>=0&&b==10&&a==en_passant[3]+1&&board[a-1][b]==23) {
				if(is_promotion(a,color))ep_promotion(a,b,a-1,b+1,a-1,b,color,list);
				else ep(a,b,a-1,b+1,a-1,b,color,list);
			}
			break;
		case 2:
			if(a==1) {
				if(board[a+1][b]==-1&&board[a+2][b]==-1) {
					add_move(a,b,a+2,b,color,list,kc,nq_moves);
				}
			}
			if(board[a+1][b]==-1){
				if(is_promotion(a,color))add_promotion_move(a,b,a+1,b,color,list);
				else add_move(a,b,a+1,b,color,list,kc,nq_moves);
			}
			if(b-1>=0&&board[a+1][b-1]!=-3&&board[a+1][b-1]!=-1&&!same_team(board[a+1][b-1]/6, color)) {
				if(is_promotion(a,color))add_promotion_move(a,b,a+1,b-1,color,list);
				else add_move(a,b,a+1,b-1,color,list,kc,nq_moves);
			}
			if(b+1<14&&board[a+1][b+1]!=-3&&board[a+1][b+1]!=-1&&!same_team(board[a+1][b+1]/6, color)) {
				if(is_promotion(a,color))add_promotion_move(a,b,a+1,b+1,color,list);
				else add_move(a,b,a+1,b+1,color,list,kc,nq_moves);
			}
			if(en_passant[1]>=0&&b==3&&a==en_passant[1]-1&&board[a+1][b]==11) {
				if(is_promotion(a,color))ep_promotion(a,b,a+1,b-1,a+1,b,color,list);
				else ep(a,b,a+1,b-1,a+1,b,color,list);
			}
			if(en_passant[3]>=0&&b==10&&a==en_passant[3]-1&&board[a+1][b]==11) {
				if(is_promotion(a,color))ep_promotion(a,b,a+1,b+1,a+1,b,color,list);
				else ep(a,b,a+1,b+1,a+1,b,color,list);
			}
			break;
		case 1:
			if(b==1) {
				if(board[a][b+1]==-1&&board[a][b+2]==-1) {
					add_move(a,b,a,b+2,color,list,kc,nq_moves);
				}
			}
			if(board[a][b+1]==-1){
				if(is_promotion(b,color))add_promotion_move(a,b,a,b+1,color,list);
				else add_move(a,b,a,b+1,color,list,kc,nq_moves);
			}
			if(a+1<14&&board[a+1][b+1]!=-3&&board[a+1][b+1]!=-1&&!same_team(board[a+1][b+1]/6, color)) {
				if(is_promotion(b,color))add_promotion_move(a,b,a+1,b+1,color,list);
				else add_move(a,b,a+1,b+1,color,list,kc,nq_moves);
			}
			if(a-1>=0&&board[a-1][b+1]!=-3&&board[a-1][b+1]!=-1&&!same_team(board[a-1][b+1]/6, color)) {
				if(is_promotion(b,color))add_promotion_move(a,b,a-1,b+1,color,list);
				else add_move(a,b,a-1,b+1,color,list,kc,nq_moves);
			}
			if(en_passant[0]>=0&&a==10&&b==en_passant[0]-1&&board[a][b+1]==5) {
				if(is_promotion(b,color))ep_promotion(a,b,a+1,b+1,a,b+1,color,list);
				else ep(a,b,a+1,b+1,a,b+1,color,list);
			}
			if(en_passant[2]>=0&&a==3&&b==en_passant[2]-1&&board[a][b+1]==17) {
				if(is_promotion(b,color))ep_promotion(a,b,a-1,b+1,a,b+1,color,list);
				else ep(a,b,a-1,b+1,a,b+1,color,list);
			}
			break;
		case 3:
			if(b==12) {
				if(board[a][b-1]==-1&&board[a][b-2]==-1) {
					add_move(a,b,a,b-2,color,list,kc,nq_moves);
				}
			}
			if(board[a][b-1]==-1){
				if(is_promotion(b,color))add_promotion_move(a,b,a,b-1,color,list);
				else add_move(a,b,a,b-1,color,list,kc,nq_moves);
			}
			if(a+1<14&&board[a+1][b-1]!=-3&&board[a+1][b-1]!=-1&&!same_team(board[a+1][b-1]/6, color)) {
				if(is_promotion(b,color))add_promotion_move(a,b,a+1,b-1,color,list);
				else add_move(a,b,a+1,b-1,color,list,kc,nq_moves);
			}
			if(a-1>=0&&board[a-1][b-1]!=-3&&board[a-1][b-1]!=-1&&!same_team(board[a-1][b-1]/6, color)) {
				if(is_promotion(b,color))add_promotion_move(a,b,a-1,b-1,color,list);
				else add_move(a,b,a-1,b-1,color,list,kc,nq_moves);
			}
			if(en_passant[0]>=0&&a==10&&b==en_passant[0]+1&&board[a][b-1]==5) {
				if(is_promotion(b,color))ep_promotion(a,b,a+1,b-1,a,b-1,color,list);
				else ep(a,b,a+1,b-1,a,b-1,color,list);
			}
			if(en_passant[2]>=0&&a==3&&b==en_passant[2]+1&&board[a][b-1]==17) {
				if(is_promotion(b,color))ep_promotion(a,b,a-1,b-1,a,b-1,color,list);
				else ep(a,b,a-1,b-1,a,b-1,color,list);
			}
			break;
		}
	}
	
	static boolean same_team(int a, int b) {
		if(a<0)a*=-1;
		if(b<0)b*=-1;
		if(a==b||Math.max(a,b)-Math.min(a,b)==2)return true;
		return false;
	}
	
	static boolean harmless_piece_b(int i, int j, int color) {		//considering bishop ray
		if(board[i][j]==-1)return false;
		if(same_team(board[i][j]/6, color))return true;
		if(board[i][j]%6!=1&&board[i][j]%6!=3)return true;
		return false;
	}
	
	static boolean harmless_piece_r(int i, int j, int color) {		//rook ray
		if(board[i][j]==-1)return false;
		if(same_team(board[i][j]/6, color))return true;
		if(board[i][j]%6!=1&&Math.abs(board[i][j])%6!=2)return true;
		return false;
	}
	
	static int[] create_move(int a, int b, int c, int d) {
		int[] t = new int[5];
		t[0]=a; t[1]=b; t[2]=c; t[3]=d; t[4]=board[c][d];
		return t;
	}
	
	static boolean knight_attacked(int i, int j, int color) {		//if adversary color attacks
		if(i-1>=0) {
			if(j-2>=0&&board[i-1][j-2]%6==4&&!same_team(board[i-1][j-2]/6, color))return true;
			if(j+2<14&&board[i-1][j+2]%6==4&&!same_team(board[i-1][j+2]/6, color))return true;
		}
		if(i-2>=0) {
			if(j-1>=0&&board[i-2][j-1]%6==4&&!same_team(board[i-2][j-1]/6, color))return true;
			if(j+1<14&&board[i-2][j+1]%6==4&&!same_team(board[i-2][j+1]/6, color))return true;
		}
		if(i+1<14) {
			if(j-2>=0&&board[i+1][j-2]%6==4&&!same_team(board[i+1][j-2]/6, color))return true;
			if(j+2<14&&board[i+1][j+2]%6==4&&!same_team(board[i+1][j+2]/6, color))return true;
		}
		if(i+2<14) {
			if(j-1>=0&&board[i+2][j-1]%6==4&&!same_team(board[i+2][j-1]/6, color))return true;
			if(j+1<14&&board[i+2][j+1]%6==4&&!same_team(board[i+2][j+1]/6, color))return true;
		}
		return false;
	}
	
	static boolean ray_attacked(int i, int j, int color) {
		int count=0;
		int d = 1;
		boolean[] bool = new boolean[8];
		while(count<8) {			//8 rays around square, first bishop rays then rook rays
			//bishop rays
			if(!bool[0]) {
				if(i-d<0||j-d<0||board[i-d][j-d]==-3||harmless_piece_b(i-d,j-d,color)) {
					bool[0]=true;
					++count;
				}
				else if(board[i-d][j-d]!=-1)return true;
			}
			if(!bool[1]) {
				if(i-d<0||j+d>13||board[i-d][j+d]==-3||harmless_piece_b(i-d,j+d,color)) {
					bool[1]=true;
					++count;
				}
				else if(board[i-d][j+d]!=-1)return true;
			}
			if(!bool[2]) {						
				if(i+d>13||j-d<0||board[i+d][j-d]==-3||harmless_piece_b(i+d,j-d,color)) {
					bool[2]=true;
					++count;
				}
				else if(board[i+d][j-d]!=-1)return true;
			}
			if(!bool[3]) {
				if(i+d>13||j+d>13||board[i+d][j+d]==-3||harmless_piece_b(i+d,j+d,color)) {
					bool[3]=true;
					++count;
				}
				else if(board[i+d][j+d]!=-1) {
					return true;
				}
			}
			//rook rays
			if(!bool[4]) {
				if(i-d<0||board[i-d][j]==-3||harmless_piece_r(i-d,j,color)){
					bool[4]=true;
					++count;
				}
				else if(board[i-d][j]!=-1)return true;
			}
			if(!bool[5]) {
				if(i+d>13||board[i+d][j]==-3||harmless_piece_r(i+d,j,color)){
					bool[5]=true;
					++count;
				}
				else if(board[i+d][j]!=-1)return true;
			}
			if(!bool[6]) {
				if(j-d<0||board[i][j-d]==-3||harmless_piece_r(i,j-d,color)){
					bool[6]=true;
					++count;
				}
				else if(board[i][j-d]!=-1)return true;
			}
			if(!bool[7]) {
				if(j+d>13||board[i][j+d]==-3||harmless_piece_r(i,j+d,color)){
					bool[7]=true;
					++count;
				}
				else if(board[i][j+d]!=-1)return true;
			}
			++d;
		}
		return false;
	}
	
	static boolean pawn_attacked(int i, int j, int color) {
		if(i-1>=0&&j-1>=0) {
			if(board[i-1][j-1]%6==5&&!same_team(board[i-1][j-1]/6, color)) {
				if(board[i-1][j-1]/6==1||board[i-1][j-1]/6==2)return true;
			}
		}
		if(i-1>=0&&j+1<14) {
			if(board[i-1][j+1]%6==5&&!same_team(board[i-1][j+1]/6, color)) {
				if(board[i-1][j+1]/6==3||board[i-1][j+1]/6==2)return true;
			}
		}
		if(i+1<14&&j-1>=0) {
			if(board[i+1][j-1]%6==5&&!same_team(board[i+1][j-1]/6, color)) {
				if(board[i+1][j-1]/6==0||board[i+1][j-1]/6==1)return true;
			}
		}
		if(i+1<14&&j+1<14) {
			if(board[i+1][j+1]%6==5&&!same_team(board[i+1][j+1]/6, color)) {
				if(board[i+1][j+1]/6==3||board[i+1][j+1]/6==0)return true;
			}
		}
		return false;
	}
	
	static boolean king_attacked(int i, int j, int color) {
		switch(color%2) {
		case 0:
			int a = king_pos[1][0];
			int b = king_pos[1][1];
			if(a!=i||b!=j)if(Math.max(a,i)-Math.min(a,i)<=1&&Math.max(b,j)-Math.min(b,j)<=1)return true;
			a = king_pos[3][0];
			b = king_pos[3][1];
			if(a!=i||b!=j)if(Math.max(a,i)-Math.min(a,i)<=1&&Math.max(b,j)-Math.min(b,j)<=1)return true;
			break;
		case 1:
			a = king_pos[0][0];
			b = king_pos[0][1];
			if(a!=i||b!=j)if(Math.max(a,i)-Math.min(a,i)<=1&&Math.max(b,j)-Math.min(b,j)<=1)return true;
			a = king_pos[2][0];
			b = king_pos[2][1];
			if(a!=i||b!=j)if(Math.max(a,i)-Math.min(a,i)<=1&&Math.max(b,j)-Math.min(b,j)<=1)return true;
		}
		return false;
	}
	
	static boolean is_attacked(int i, int j, int color) {
		if(knight_attacked(i,j,color)||pawn_attacked(i,j,color)||ray_attacked(i,j,color)||king_attacked(i,j,color))return true;
		return false;
	}
	
	static boolean kcastle_possible(int color) {
		if(kr_moved[color]>0)return false;
			switch(color) {
			case 0:
				if(board[13][8]!=-1||board[13][9]!=-1||board[13][10]!=2)return false;
				if(is_attacked(13,8,color)||is_attacked(13,9,color))return false;
				break;
			case 1:
				if(board[8][0]!=-1||board[9][0]!=-1||board[10][0]!=8)return false;
				if(is_attacked(8,0,color)||is_attacked(9,0,color))return false;
				break;
			case 2:
				if(board[0][5]!=-1||board[0][4]!=-1||board[0][3]!=14)return false;
				if(is_attacked(0,5,color)||is_attacked(0,4,color))return false;
				break;
			case 3:
				if(board[5][13]!=-1||board[4][13]!=-1||board[3][13]!=20)return false;
				if(is_attacked(5,13,color)||is_attacked(4,13,color))return false;
				break;
			}	
			return true;
	}
	
	static boolean qcastle_possible(int color) {
		if(qr_moved[color]>0)return false;
			switch(color) {
			case 0:
				if(board[13][6]!=-1||board[13][5]!=-1||board[13][4]!=-1||board[13][3]!=-2)return false;
				if(is_attacked(13,6,color)||is_attacked(13,5,color))return false;
				break;
			case 1:
				if(board[6][0]!=-1||board[5][0]!=-1||board[4][0]!=-1||board[0][3]!=-8)return false;
				if(is_attacked(6,0,color)||is_attacked(5,0,color))return false;
				break;
			case 2:
				if(board[0][7]!=-1||board[0][8]!=-1||board[0][9]!=-1||board[0][10]!=-14)return false;
				if(is_attacked(0,7,color)||is_attacked(0,8,color))return false;
				break;
			case 3:
				if(board[7][13]!=-1||board[8][13]!=-1||board[9][13]!=-1||board[13][10]!=-20)return false;
				if(is_attacked(7,13,color)||is_attacked(8,13,color))return false;
				break;
			}	
			return true;
	}
	
	static void do_move(int[] a) {			//only checking for checks, not picking move
		int piece = board[a[0]][a[1]];
		if(a.length==5) {
			board[a[0]][a[1]]=-1;
			board[a[2]][a[3]]=piece;
		}
		else if(a.length==8) {					//castle
			board[a[2]][a[3]]=board[a[0]][a[1]];
			board[a[0]][a[1]]=-1;
			board[a[6]][a[7]]=board[a[4]][a[5]];
			board[a[4]][a[5]]=-1;
		}
		else if(a.length==6) {					//promotion
			board[a[2]][a[3]]=a[5];
			board[a[0]][a[1]]=-1;
		}
		else if(a.length==7) {
			board[a[0]][a[1]]=-1;
			board[a[2]][a[3]]=a[6];
			board[a[4]][a[5]]=-1;
		}
	}
	
	static void undo_move(int[] a) {
		int piece = board[a[2]][a[3]];
		if(a.length==5) {
			board[a[0]][a[1]]=piece;
			board[a[2]][a[3]]=a[4];
		}
		else if(a.length==8) {
			board[a[0]][a[1]]=board[a[2]][a[3]];
			board[a[2]][a[3]]=-1;
			board[a[4]][a[5]]=board[a[6]][a[7]];
			board[a[6]][a[7]]=-1;
		}
		else if(a.length==6) {
			board[a[0]][a[1]]=board[a[2]][a[3]]/6*6+5;
			board[a[2]][a[3]]=a[4];
		}
		else if(a.length==7) {
			int color = board[a[2]][a[3]]/6;
			board[a[0]][a[1]]=color*6+5;
			board[a[2]][a[3]]=-1;
			switch(color%2) {
			case 0:
				if(a[1]==3)board[a[4]][a[5]]=11;
				else board[a[4]][a[5]]=23;
				break;
			case 1:
				if(a[0]==3)board[a[4]][a[5]]=17;
				else board[a[4]][a[5]]=5;
				break;
			}
		}
	}
	
	static void play_move(int[] a) {
		update_zobrist(a);
		int piece = board[a[0]][a[1]];
		if(piece%6==5&&(Math.max(a[0], a[2])-Math.min(a[0], a[2])>1||Math.max(a[1],a[3])-Math.min(a[1],a[3])>1)){
			switch((piece/6)%2) {
			case 0:
				en_passant[piece/6]=a[1];
				break;
			case 1:
				en_passant[piece/6] = a[0];
				break;
			}
		}
		else {
			en_passant[piece/6]=-1;
		}
		if(a.length==5) {
			if(piece%6==0) {
				king_moved[piece/6]++;
				king_pos[piece/6][0]=a[2];
				king_pos[piece/6][1]=a[3];
			}
			else if(piece%6==2)kr_moved[piece/6]++;
			else if(piece%6==-2)qr_moved[piece/6]++;
			board[a[0]][a[1]]=-1;
			board[a[2]][a[3]]=piece;
		}
		else if(a.length==8) {					//castle
			king_moved[board[a[0]][a[1]]/6]++;
			king_pos[piece/6][0]=a[2];
			king_pos[piece/6][1]=a[3];
			board[a[2]][a[3]]=board[a[0]][a[1]];
			board[a[0]][a[1]]=-1;
			board[a[6]][a[7]]=board[a[4]][a[5]];
			board[a[4]][a[5]]=-1;
		}
		else if(a.length==6) {					//promotion
			board[a[2]][a[3]]=a[5];
			board[a[0]][a[1]]=-1;
		}
		else if(a.length==7) {					//ep
			board[a[0]][a[1]]=-1;
			board[a[2]][a[3]]=a[6];
			board[a[4]][a[5]]=-1;
		}
	}
	
	static void unplay_move(int[] a, int prev) {
		reverse_zobrist(a);
		int piece = board[a[2]][a[3]];
		en_passant[piece/6]=prev;
		if(a.length==5) {
			if(piece%6==0) {
				king_moved[piece/6]--;
				king_pos[piece/6][0]=a[0];
				king_pos[piece/6][1]=a[1];
			}
			else if(piece%6==2)kr_moved[piece/6]--;
			else if(piece%6==-2)qr_moved[piece/6]--;
			board[a[0]][a[1]]=piece;
			board[a[2]][a[3]]=a[4];
		}
		else if(a.length==8) {
			king_pos[piece/6][0]=a[0];
			king_pos[piece/6][1]=a[1];
			board[a[0]][a[1]]=board[a[2]][a[3]];
			board[a[2]][a[3]]=-1;
			board[a[4]][a[5]]=board[a[6]][a[7]];
			board[a[6]][a[7]]=-1;
			king_moved[board[a[0]][a[1]]/6]--;
		}
		else if(a.length==6) {
			board[a[0]][a[1]]=board[a[2]][a[3]]/6*6+5;
			board[a[2]][a[3]]=a[4];
		}
		else if(a.length==7) {
			int color = board[a[2]][a[3]]/6;
			board[a[0]][a[1]]=color*6+5;
			board[a[2]][a[3]]=-1;
			switch(color%2) {
			case 0:
				if(a[1]==3) {
					board[a[4]][a[5]]=11;
					TT.zobrist^=TT.random_long[11][a[4]][a[5]];
				}
				else {
					board[a[4]][a[5]]=23;
					TT.zobrist^=TT.random_long[23][a[4]][a[5]];
				}
				break;
			case 1:
				if(a[0]==3) {
					board[a[4]][a[5]]=17;
					TT.zobrist^=TT.random_long[17][a[4]][a[5]];
				}
				else {
					board[a[4]][a[5]]=5;
					TT.zobrist^=TT.random_long[5][a[4]][a[5]];
				}
				break;
			}
		}
	}
	
	static boolean possible(int[] a, int color) {
		do_move(a);
		if(is_checked(color)) {
			undo_move(a);
			return false;
		}
		undo_move(a);
		return true;
	}
	
	static boolean possible(int[] a, int i, int j, int color) {
		do_move(a);
		if(is_attacked(i,j,color)) {
			undo_move(a);
			return false;
		}
		undo_move(a);
		return true;
	}
	
	static boolean is_checked(int color) {
		if(is_attacked(king_pos[color][0], king_pos[color][1], color))return true;
		return false;
	}
	
	static int[] promotion(int a, int b, int c, int d, int e) {
		int t[] = new int[6];
		t[0]=a;
		t[1]=b;
		t[2]=c;
		t[3]=d;
		t[4]=board[c][d];
		t[5]=e;
		return t;
	}
	
	public static void add_move(int a, int b, int c, int d, int color, ArrayList<int[]> list, boolean kc, boolean nq_moves) {
		if(nq_moves) {
			if(board[c][d]!=-1) {
				int[] ar = create_move(a,b,c,d);
				if(possible(ar, color))list.add(ar);
			}
			else if(check(c,d,board[a][b])) {
				int[] ar = create_move(a,b,c,d);
				if(possible(ar, color))list.add(ar);
			}
			return;
		}
		if(board[a][b]%6==0) {
			int[] ar = create_move(a,b,c,d);
			if(possible(ar, color))list.add(ar);
		}
		else if(kc) {
			if(board[c][d]%6==4) {
				int[] ar = create_move(a,b,c,d);
				if(possible(ar, color))list.add(ar);
			}
		}
		else if(on_king_ray(a,b,color)){
			int[] ar = create_move(a,b,c,d);
			if(possible(ar, color))list.add(ar);
		}
		else {
			int[] ar = create_move(a,b,c,d);
			list.add(ar);
		}
	}
	
	
	static boolean is_promotion(int i, int color) {
		if(color==0||color==3)return i==4;
		else return i==9;
	}
	
	static void add_promotion_move(int a, int b, int c, int d, int color,ArrayList<int[]> list) {
		int[] ar = promotion(a,b,c,d,color*6+1);
		if(possible(ar, color)) {
			list.add(ar);
			for(int i=color*6+2; i<color*6+5; i++) {
				ar = promotion(a,b,c,d,i);
				list.add(ar);
			}
		}
	}
	
	static void ep_promotion(int a, int b, int c, int d, int e, int f, int color, ArrayList<int[]> list) {
		int[] ar = {a,b,c,d,e,f,color*6+1};
		if(possible(ar, color)) {
			list.add(ar);
			for(int i=color*6+2; i<color*6+5; i++) {
				list.add(ep_array(a,b,c,d,e,f,i));
			}
		}
	}
	
	static void ep(int a, int b, int c, int d, int e, int f, int color, ArrayList<int[]> list) {
		int[] ar = {a,b,c,d,e,f,color*6+5};
		if(possible(ar, color))list.add(ar);
	}
	static int[] ep_array(int a, int b, int c, int d, int e, int f,int g) {
		int[] t = new int[7];
		t[0]=a; t[1]=b; t[2]=c; t[3]=d; t[4]=e; t[5]=f; t[6]=g;
		return t;
	}
	
	
	static boolean free_square(int i, int j, int color) {
		if(board[i][j]==-1)return true;
		if(board[i][j]!=-2&&board[i][j]!=-1&&!same_team(board[i][j]/6,color))return true;
		return false;
	}
	
	static boolean check(int a, int b, int piece) {
		if(piece%6==1||piece%6==2) {
			boolean[] bool = new boolean[4];
			for(int i=1; i<14; i++) {
				if(!bool[0]&&a+i<14&&board[a+i][b]!=-1) {
					if(board[a+i][b]%6==0&&!same_team(board[a+i][b]/6,piece/6))return true;
					else bool[0]=true;
				}
				if(!bool[1]&&a-i>=0&&board[a-i][b]!=-1) {
					if(board[a-i][b]%6==0&&!same_team(board[a-i][b]/6,piece/6))return true;
					else bool[1]=true;
				}
				if(!bool[2]&&b+i<14&&board[a][b+i]!=-1) {
					if(board[a][b+i]%6==0&&!same_team(board[a][b+i]/6,piece/6))return true;
					else bool[2]=true;
				}
				if(!bool[3]&&b-i>=0&&board[a][b-i]!=-1) {
					if(board[a][b-i]%6==0&&!same_team(board[a][b-i]/6,piece/6))return true;
					else bool[3]=true;
				}
			}
		}
		if(piece%6==1||piece%6==3) {
			boolean[] bool = new boolean[4];
			for(int i=1; i<14; i++) {
				if(!bool[0]&&a+i<14&&b+i<14&&board[a+i][b+i]!=-1) {
					if(board[a+i][b+i]%6==0&&!same_team(board[a+i][b+i]/6,piece/6))return true;
					else bool[0]=true;
				}
				if(!bool[1]&&a-i>=0&&b+i<14&&board[a-i][b+i]!=-1) {
					if(board[a-i][b+i]%6==0&&!same_team(board[a-i][b+i]/6,piece/6))return true;
					else bool[1]=true;
				}
				if(!bool[2]&&a+i<14&&b-i>=0&&board[a+i][b-i]!=-1) {
					if(board[a+i][b-i]%6==0&&!same_team(board[a+i][b-i]/6,piece/6))return true;
					else bool[2]=true;
				}
				if(!bool[3]&&a-i>=0&&b-i>=0&&board[a-i][b-i]!=-1) {
					if(board[a-i][b-i]%6==0&&!same_team(board[a-i][b-i]/6,piece/6))return true;
					else bool[3]=true;
				}
			}
		}
		if(piece%6==4) {
			if(a-2>=0) {
				if(b-1>=0&&board[a-2][b-1]%6==0&&!same_team(board[a-2][b-1]/6,piece/6))return true;
				if(b+1<14&&board[a-2][b+1]%6==0&&!same_team(board[a-2][b+1]/6,piece/6))return true;
			}
			if(a+2<14) {
				if(b-1>=0&&board[a+2][b-1]%6==0&&!same_team(board[a+2][b-1]/6,piece/6))return true;
				if(b+1<14&&board[a+2][b+1]%6==0&&!same_team(board[a+2][b+1]/6,piece/6))return true;
			}
			if(a-1>=0) {
				if(b-2>=0&&board[a-1][b-2]%6==0&&!same_team(board[a-1][b-2]/6, piece/6))return true;
				if(b+2<14&&board[a-1][b+2]%6==0&&!same_team(board[a-1][b+2]/6, piece/6))return true;
			}
			if(a+1<14) {
				if(b-2>=0&&board[a+1][b-2]%6==0&&!same_team(board[a+1][b-2]/6, piece/6))return true;
				if(b+2<14&&board[a+1][b+2]%6==0&&!same_team(board[a+1][b+2]/6, piece/6))return true;
			}
		}
		if(piece%6==5) {
			switch(piece/6) {
			case 0:
				if(b-1>=0&&board[a-1][b-1]%6==0&&!same_team(0,board[a-1][b-1])||b+1<14&&board[a-1][b+1]%6==0&&!same_team(0,board[a-1][b+1]))return true;
				break;
			case 1:
				if(a+1<14&&board[a+1][b+1]%6==0&&!same_team(0,board[a+1][b+1])||a-1>=0&&board[a-1][b+1]%6==0&&!same_team(0,board[a-1][b+1]))return true;
				break;
			case 2:
				if(b-1>=0&&board[a+1][b-1]%6==0&&!same_team(0,board[a+1][b-1])||b+1<14&&board[a+1][b+1]%6==0&&!same_team(0,board[a+1][b+1]))return true;
				break;
			case 3:
				if(a+1<014&&board[a+1][b-1]%6==0&&!same_team(0,board[a+1][b-1])||a-1>=0&&board[a-1][b-1]%6==0&&!same_team(0,board[a-1][b-1]))return true;
				break;
			}
		}
		return false;
	}
	
	static boolean on_king_ray(int a, int b, int color) {
		int i = king_pos[color][0];
		int j = king_pos[color][1];
		if(a==i||j==b)return true;
		if(Math.abs(a-i)==Math.abs(b-j))return true;
		return false;
	}
	
	static void update_zobrist(int[] a) {
		int piece = Math.abs(board[a[0]][a[1]]);
		TT.zobrist^=TT.random_long[piece][a[0]][a[1]];
		switch(a.length) {
		case 5:
			if(a[4]!=-1) {
				TT.zobrist^=TT.random_long[Math.abs(a[4])][a[2]][a[3]];
			}
			TT.zobrist^=TT.random_long[piece][a[2]][a[3]];
			break;
		case 6:
			if(a[4]!=-1)TT.zobrist^=TT.random_long[Math.abs(a[4])][a[2]][a[3]];
			TT.zobrist^=TT.random_long[a[5]][a[2]][a[3]];
			break;
		case 7:
			TT.zobrist^=TT.random_long[a[6]][a[2]][a[3]];
			print_board(board);
			TT.zobrist^=TT.random_long[board[a[4]][a[5]]][a[4]][a[5]];
			break;
		case 8:
			TT.zobrist^=TT.random_long[board[a[0]][a[1]]][a[2]][a[3]];
			TT.zobrist^=TT.random_long[Math.abs(board[a[4]][a[5]])][a[4]][a[5]];
			TT.zobrist^=TT.random_long[Math.abs(board[a[4]][a[5]])][a[6]][a[7]];
			break;
		}	
	}
	static void reverse_zobrist(int[] a) {
		int piece = Math.abs(board[a[2]][a[3]]);
		switch(a.length) {
		case 5:
			if(a[4]!=-1)TT.zobrist^=TT.random_long[Math.abs(a[4])][a[2]][a[3]];
			TT.zobrist^=TT.random_long[piece][a[2]][a[3]];
			TT.zobrist^=TT.random_long[piece][a[0]][a[1]];
			break;
		case 6:
			if(a[4]!=-1)TT.zobrist^=TT.random_long[a[4]][a[2]][a[3]];
			TT.zobrist^=TT.random_long[a[5]][a[2]][a[3]];
			TT.zobrist^=TT.random_long[a[5]/6*6+5][a[0]][a[1]];
			break;
		case 7:
			TT.zobrist^=TT.random_long[a[6]][a[2]][a[3]];
			TT.zobrist^=TT.random_long[board[a[2]][a[3]]/6*6+5][a[0]][a[1]];
			break;
		case 8:
			TT.zobrist^=TT.random_long[board[a[2]][a[3]]][a[2]][a[3]];
			TT.zobrist^=TT.random_long[board[a[2]][a[3]]][a[0]][a[1]];
			TT.zobrist^=TT.random_long[board[a[6]][a[7]]][a[4]][a[5]];
			TT.zobrist^=TT.random_long[board[a[6]][a[7]]][a[6]][a[7]];
			break;
		}	
	}
}
