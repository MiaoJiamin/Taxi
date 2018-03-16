package taxi;

import java.util.Iterator;

/*
 * Overview:
 * 可追踪出租车
 * 继承父类Taxi的repOK();
 * 
 * 不变式:(state==0 || state==1 || state==2 || state==3)&&
 *  	   (0<=x<=79) && (0<=y<=79) && (credit>=0) &&
 *  	   (0<=src_x<=79) && (0<=src_y<=79) && (0<=dst_x<=79) && (0<=dst_y<=79) &&
 *  	   (0<=id<=99) && (dir==0 || dir==1 || dir==2 || dir==3) &&
 *  	   (map!=null) && (for all 0<=i,j<80, (map[i][j]==0 || map[i][j]==1 || map[i][j]==2 || map[i][j]==3))&&
 *  	   (Weights!=null) && (for all 0<=i,j<6400, (Weights[i][j]==1 || Weights[i][j]==1000000)) &&
 *  	   (lights!=null) && (for all 0<=i,j<80, lights[i][j].repOK())
 */
public class Taxi2 extends Taxi {
		
	Taxi2(int[][] map, int[][] Weights2, TaxiGUI gui, Traffic_lights[][] lights, long initTime) {
		super(map, Weights2, gui, lights, initTime);
		// TODO Auto-generated constructor stub
	}
		
	public void print(){
		/*@ REQUIRES: None;
		  @ MODIFIES: None;
		  @ EFFECTS:  print the information of the special taxi;
		 */
		Iterator<Taxi_output> it = taxi2_out.iterator();
		while(it.hasNext()){
			Taxi_output out = it.next();
			
			System.out.println(out.req.toString());
			System.out.println("请求产生时刻: " + ((out.time+initTime)/100)*100);
			System.out.println("抢单时所处位置: " + "(" + out.x + "," + out.y + ")");
			System.out.println("请求发出位置: " + "(" + out.src_x + "," + out.src_y + ")");
			System.out.println("目的地位置: " + "(" + out.dst_x + "," + out.dst_y + ")");
			System.out.print("接客路径: ");
			for(int j = 0; j < out.take.size(); j++){
				System.out.print(out.take.get(j));
			}
			
			System.out.print("\n送客路径: ");
			for(int j = 0; j < out.send.size(); j++){
				System.out.print(out.send.get(j));
			}
			System.out.println("\n");
		}
		
	}
	
	
}
