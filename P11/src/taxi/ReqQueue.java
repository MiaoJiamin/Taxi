package taxi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Overview:
 * 判断输入的请求是否合法，是否是相同的请求，将满足要求的请求存入请求队列中
 * 查询指定出租车的信息
 * 查询指定状态的所有车租车
 * 实现道路的开闭，表现为修改Weights，若i 和 j 之间联通，则Weights[i][j]和Weights[j][i]==1	；若不联通，则Weights[i][j]和Weights[j][i]==1000000；
 *
 * 不变式: (for all 0<=i<queue.size(), queue.get(i).repOK()) && (Weights!=null) && (for all 0<=i,j<6400, (Weights[i][j]==1 || Weights[i][j]==1000000))
 */
public class ReqQueue {
	private int src_x;
	private int src_y;
	private int dst_x;
	private int dst_y;
	private ArrayList<Request> queue = new ArrayList<>();
	private int count = 0;
	private int[][] map;
	private int[][] Weights;
	TaxiGUI gui;
	
	
	ReqQueue(int[][] map, int[][] Weights, TaxiGUI gui){
		/*@ REQUIRES: int[][] map, int[][] Weights, TaxiGUI gui;
		  @ MODIFIES: this.map;
		  @			  this.Weights;
		  @			  this.gui;
		  @ EFFECTS: None;
		  @*/
		this.map = map;
		this.Weights = Weights;
		this.gui = gui;
	}
	String regex = "\\[CR,\\(\\+?\\d+,\\+?\\d+\\),\\(\\+?\\d+,\\+?\\d+\\)\\]";
	Pattern pattern = Pattern.compile(regex);
	
	String regex2 = "taxi:\\d+";
	Pattern pattern2 = Pattern.compile(regex2);
	
	String regex3 = "state:(0|1|2|3)?";
	Pattern pattern3 = Pattern.compile(regex3);
	
	String regex4 = "open:\\(\\d+,\\d+\\),\\(\\d+,\\d+\\)";
	Pattern pattern4 = Pattern.compile(regex4);
	
	String regex5 = "close:\\(\\d+,\\d+\\),\\(\\d+,\\d+\\)";
	Pattern pattern5 = Pattern.compile(regex5);
	
	String regex6 = "specialtaxi:\\d+";
	Pattern pattern6 = Pattern.compile(regex6);
	
	public void addreq(long initTime,Taxi[] taxis){
		/*@ REQUIRES: long initTime,Taxi[] taxis;
		  @ MODIFIES: taxis,gui;
		  @ EFFECTS: (\forall line=scanner.nextLine();
		  @			 matcher.matches() == true;
		  @			 start ReqAlloc Thread);
		  @ EFFECTS：(\forall line=scanner.nextLine();
		  @			 matcher2.matches() == true;
		  @			 print the information of the taxi);
		  @ EFFECTS：(\forall line=scanner.nextLine();
		  @			 matcher3.matches() == true;
		  @			 print all the taxis which equals this state);
		  @ EFFECTS: (\forall line=scanner.nextLine();
		  @			 matcher4.matches() == true;
		  @			 open the path && update the gui);
		  @ EFFECTS: (\forall line=scanner.nextLine();
		  @			 matcher5.matches() == true;
		  @			 close the path && update the gui);
		  @ EFFECTS: (\forall line=scanner.nextLine();
		  @			 matcher6.matches() == true;
		  @			 print the information of the special taxi);
		  @*/
		String line = null;
		Scanner scanner = new Scanner(System.in);
		
		while(!"run".equals(line=scanner.nextLine())){
			int flag = 0;
			line = line.replaceAll(" ", "");
			Matcher matcher = pattern.matcher(line);
			Matcher matcher2 = pattern2.matcher(line);
			Matcher matcher3 = pattern3.matcher(line);
			Matcher matcher4 = pattern4.matcher(line);
			Matcher matcher5 = pattern5.matcher(line);
			Matcher matcher6 = pattern6.matcher(line);
			if(matcher.matches() == true){
				line = line.replaceAll("\\(", "");
				line = line.replaceAll("\\)", "");
				line = line.replaceAll("\\[", "");
				line = line.replaceAll("\\]", "");
				List<String> list = new ArrayList<>();
				list = Arrays.asList(line.split( ","));
				src_x = Integer.parseInt(list.get(1));
				src_y = Integer.parseInt(list.get(2));
				dst_x = Integer.parseInt(list.get(3));
				dst_y = Integer.parseInt(list.get(4));
				if(src_x<0||src_x>79||src_y<0||src_y>79||dst_x<0||dst_x>79||dst_y<0||dst_y>79){
					System.out.println("[CR,(" + src_x+","+src_y+"),(" + dst_x +"," +dst_y +")]" +"坐标不正确");
				}else{
					if(src_x == dst_x && src_y == dst_y){
						System.out.println("出发地和目的地相同");
					}else{
						if(count == 0){
							queue.add(new Request());
							queue.get(count).setsrc_x(src_x);
							queue.get(count).setsrc_y(src_y);
							queue.get(count).setdst_x(dst_x);
							queue.get(count).setdst_y(dst_y);
							queue.get(count).setReqtime(System.currentTimeMillis()-initTime);
							
							ReqAlloc req = new ReqAlloc(queue.get(count),taxis,initTime);
							new Thread(req).start();
							count++;
						}else{
							for(int i = 0; i < count; i++){
								if(src_x == queue.get(i).getsrc_x() && src_y == queue.get(i).getsrc_y() 
										&& dst_x == queue.get(i).getdst_x() && dst_y == queue.get(i).getdst_y()
										&& ((System.currentTimeMillis()-initTime)/100)*100 == (queue.get(i).getReqtime()/100)*100){
									flag = 1;
								}
							}
							if(flag == 1){
								System.out.println("[CR,("+ src_x +","+src_y+"),("+dst_x+","+ dst_y +")]"+"相同的请求");
							}
							if(flag == 0){
								queue.add(new Request());
								queue.get(count).setsrc_x(src_x);
								queue.get(count).setsrc_y(src_y);
								queue.get(count).setdst_x(dst_x);
								queue.get(count).setdst_y(dst_y);
								queue.get(count).setReqtime(System.currentTimeMillis()-initTime);
								ReqAlloc req = new ReqAlloc(queue.get(count),taxis,initTime);
								new Thread(req).start();
								count++;
							}
						}
					}
				}	
			}else if(matcher2.matches()==true){
				List<String> list = new ArrayList<>();
				list = Arrays.asList(line.split( ":"));
				int id = Integer.parseInt(list.get(1));
				System.out.println("taxi:" + id + " state:" + taxis[id].getstate() + " ponit:" + "(" + taxis[id].getcurrentx() + "," + taxis[id].getcurrenty() +") " + System.currentTimeMillis()/100); 
			}else if(matcher3.matches()==true){
				List<String> list = new ArrayList<>();
				list = Arrays.asList(line.split( ":"));
				int state = Integer.parseInt(list.get(1));
				System.out.println("state " + state + ": ");
				for(int i = 0; i < 100; i++){
					if(taxis[i].getstate() == state){
						System.out.print("taxi:"+ i + "  ");
					}
				}
			}else if(matcher4.matches()==true){
				line = line.replaceAll("open:", "");
				line = line.replaceAll("\\(", "");
				line = line.replaceAll("\\)", "");
				List<String> list = new ArrayList<>();
				list = Arrays.asList(line.split( ","));
				int num1_x = Integer.parseInt(list.get(0));
				int num1_y = Integer.parseInt(list.get(1));
				int num2_x = Integer.parseInt(list.get(2));
				int num2_y = Integer.parseInt(list.get(3));
				if(num1_x == num2_x && num1_y == num2_y-1){
					if(map[num1_x][num1_y]==0){
						map[num1_x][num1_y] = 1;
						Weights[num1_x*80 + num1_y][num2_x*80 + num2_y] = 1;
						Weights[num2_x*80 + num2_y][num1_x*80 + num1_y] = 1;
					}else if(map[num1_x][num1_y]==2){
						map[num1_x][num1_y] = 3;
						Weights[num1_x*80 + num1_y][num2_x*80 + num2_y] = 1;
						Weights[num2_x*80 + num2_y][num1_x*80 + num1_y] = 1;
					}
				}else if(num1_y == num2_y && num1_x == num2_x-1){
					if(map[num1_x][num1_y]==0){
						map[num1_x][num1_y] = 2;
						Weights[num1_x*80 + num1_y][num2_x*80 + num2_y] = 1;
						Weights[num2_x*80 + num2_y][num1_x*80 + num1_y] = 1;
					}else if(map[num1_x][num1_y]==1){
						map[num1_x][num1_y] = 3;
						Weights[num1_x*80 + num1_y][num2_x*80 + num2_y] = 1;
						Weights[num2_x*80 + num2_y][num1_x*80 + num1_y] = 1;
					}
				}
				Point p1 = new Point(num1_x, num1_y);
				Point p2 = new Point(num2_x, num2_y);
				gui.SetRoadStatus(p1, p2, 1);
			}else if(matcher5.matches()==true){
				line = line.replaceAll("close:", "");
				line = line.replaceAll("\\(", "");
				line = line.replaceAll("\\)", "");
				List<String> list = new ArrayList<>();
				list = Arrays.asList(line.split( ","));
				int num1_x = Integer.parseInt(list.get(0));
				int num1_y = Integer.parseInt(list.get(1));
				int num2_x = Integer.parseInt(list.get(2));
				int num2_y = Integer.parseInt(list.get(3));
				if(num1_x == num2_x && num1_y == num2_y-1){
					if(map[num1_x][num1_y]==1){
						map[num1_x][num1_y] = 0;
						Weights[num1_x*80 + num1_y][num2_x*80 + num2_y] = 1000000;
						Weights[num2_x*80 + num2_y][num1_x*80 + num1_y] = 1000000;
					}else if(map[num1_x][num1_y]==3){
						map[num1_x][num1_y] = 2;
						Weights[num1_x*80 + num1_y][num2_x*80 + num2_y] = 1000000;
						Weights[num2_x*80 + num2_y][num1_x*80 + num1_y] = 1000000;
					}
				}else if(num1_y == num2_y && num1_x == num2_x-1){
					if(map[num1_x][num1_y]==2){
						map[num1_x][num1_y] = 0;
						Weights[num1_x*80 + num1_y][num2_x*80 + num2_y] = 1000000;
						Weights[num2_x*80 + num2_y][num1_x*80 + num1_y] = 1000000;
					}else if(map[num1_x][num1_y]==3){
						map[num1_x][num1_y] = 1;
						Weights[num1_x*80 + num1_y][num2_x*80 + num2_y] = 1000000;
						Weights[num2_x*80 + num2_y][num1_x*80 + num1_y] = 1000000;
					}
				}
				
				Point p1 = new Point(num1_x, num1_y);
				Point p2 = new Point(num2_x, num2_y);
				gui.SetRoadStatus(p1, p2, 0);
			}else if(matcher6.matches()==true){
				line = line.replaceAll("specialtaxi:", "");
				int num = Integer.parseInt(line);
				taxis[num].print();
			}else{
				System.out.println("非法输入");
			}
		}
		scanner.close();
	}
	
	
	public boolean repOK(){
		for(int i = 0; i < queue.size(); i++){
			Request req = queue.get(i);
			req.repOK();
		}
		if(Weights == null){
			System.out.println("false");
			return false;
		}
		for(int i = 0; i < 6400; i++){
			for(int j = 0; j < 6400; j++){
				int num = Weights[i][j];
				if(num!=1 && num!=1000000){
					System.out.println("false");
					return false;
				}
					
			}
		}
		return true;
	}
	
}
