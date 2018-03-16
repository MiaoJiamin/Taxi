package taxi;

import java.awt.Point;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;


/*
 * Overview:
 * 实现出租车四种状态的运行和状态的转换
 *  
 *  不变式:(state==0 || state==1 || state==2 || state==3)&&
 *  	   (0<=x<=79) && (0<=y<=79) && (credit>=0) &&
 *  	   (0<=src_x<=79) && (0<=src_y<=79) && (0<=dst_x<=79) && (0<=dst_y<=79) &&
 *  	   (0<=id<=99) && (dir==0 || dir==1 || dir==2 || dir==3) &&
 *  	   (map!=null) && (for all 0<=i,j<80, (map[i][j]==0 || map[i][j]==1 || map[i][j]==2 || map[i][j]==3))&&
 *  	   (Weights!=null) && (for all 0<=i,j<6400, (Weights[i][j]==1 || Weights[i][j]==1000000)) &&
 *  	   (lights!=null) && (for all 0<=i,j<80, lights[i][j].repOK())
 */
public class Taxi extends Thread{
	private int state;//服务1	接单3	等待2	停止0
	private int x,y;	//出租车的坐标
	private int credit;	//信用
	private int src_x,src_y;
	private int dst_x,dst_y;
	private int id;
	int[][] map;
	int[][] Weights;
	private Request req;
	private Traffic_lights[][] lights;
	long initTime;
	private int dir;		//0123 车头方向东西南北
	private TaxiGUI gui;
	
	ArrayList<Taxi_output> taxi2_out = new ArrayList<>(); 
	int count = 0;
	
	Taxi(int[][] map,int[][] Weights, TaxiGUI gui, Traffic_lights[][] lights, long initTime){
		/*@ REQUIRES: int[][] map, int[][] Weights, TaxiGUI gui;
		  @ MODIFIES: this.map;
		  @			  this.Weights;
		  @			  this.gui;
		  @ EFFECTS: None;
		  @*/
		Random random = new Random();
		state = 2;
		x = random.nextInt(80);
		y = random.nextInt(80);
		credit = 0;
		this.map = map;
		this.Weights = Weights;
		this.gui = gui;
		this.lights = lights;
		this.initTime = initTime;
		dir = random.nextInt(4);
	}
	
	public void run(){
		/*@ REQUIRES: None;
		  @ MODIFIES: taxis,gui;
		  @ EFFECTS: None;
		  @*/
		while(true){
			long time1 = System.currentTimeMillis();
			if(state==1){
				running(Weights);
			}else if(state==3){
				order_taking(Weights);
			}else if(state==2){
				while(state==2){
					if((System.currentTimeMillis() - time1) < 20000){
						waiting(map);
					}else{
						state = 0;
						Point p = new Point(x, y); 
						gui.SetTaxiStatus(id,p,state);
					}
				}
			}else if(state==0){
				stoped();
			}
		}
	}
	
	public synchronized void running(int[][] Weights){
		/*@ REQUIRES: int[][] Weights;
		  @ MODIFIES: taxis,gui;
		  @ EFFECTS: find the shortest route that conforms to the condition to drive;
		  @ THREAD_REQUIRES: \locked(this.taxis);
		  @ THREAD_EFFECTS: \locked();
		  @*/
		int v0;
		int VNUM;
		int v;
		int[] path = new int[6400];
		ArrayList<Integer> shortpath1 = new ArrayList<>();
		
		
		v0 = src_x*80 + src_y;
		VNUM = 6400;
		path=Dijkstra(v0,VNUM,Weights);
		
		v = dst_x*80 + dst_y;
		shortpath1.add(v);
		while(v != v0){
			shortpath1.add(path[v]);
			v = path[v];
		}
		
		int[] shortpath = new int[6400];
		int k = 0;
		
		for(int i = 0; i < 6400; i++)
			shortpath[i] = 1000000;
		
		for(int i = shortpath1.size()-1; i >= 0; i--){
			shortpath[k++] = shortpath1.get(i);
		}
				
		
		String s = "[CR,("+req.getsrc_x()+","+req.getsrc_y()+"),("+req.getdst_x()+","+req.getdst_y()+")]" + ".txt";
		toFile("服务行驶路线：",s);
		
		for(int i = 0; shortpath[i]!=1000000; i++){
			long time = 0;
			int flag = 0;
			int xsrc = x;
			int ysrc = y;
			if(lights[xsrc][ysrc].getvalid() == 1){
				if(lights[xsrc][ysrc].getDir() == 0){
					if(dir==0 && shortpath[i]/80==xsrc && shortpath[i]%80==ysrc+1)		flag = 1;
					else if(dir==1 && shortpath[i]/80==xsrc && shortpath[i]%80==ysrc-1)	flag = 1;
					else if(dir==2 && shortpath[i]/80==xsrc && shortpath[i]%80==ysrc-1) flag = 1;
					else if(dir==3 && shortpath[i]/80==xsrc && shortpath[i]%80==ysrc+1)	flag = 1;
				}else if(lights[xsrc][ysrc].getDir() == 1){
					if(dir==0 && shortpath[i]%80==ysrc && shortpath[i]/80==xsrc-1)	flag = 1;
					if(dir==1 && shortpath[i]%80==ysrc && shortpath[i]/80==xsrc+1)	flag = 1;
					if(dir==2 && shortpath[i]%80==ysrc && shortpath[i]/80==xsrc-1) 	flag = 1;
					if(dir==3 && shortpath[i]%80==ysrc && shortpath[i]/80==xsrc+1)	flag = 1;
				}
				
				if(flag == 1){
//					System.out.println("wait for traffic lights");
					time = lights[x][y].gettime() - (System.currentTimeMillis() - initTime)%lights[x][y].gettime();
				}
			}
			try {
				sleep(200+time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			y =shortpath[i]%80;
			x = shortpath[i]/80;
			Point p = new Point(x, y); 
			gui.SetTaxiStatus(id,p,state);
			
			if(x==dst_x && y==dst_y){
				toFile("(" + x + "," + y + ")",s);	
				taxi2_out.get(count).send.add("(" + x + "," + y + ")");
				count ++;
				break;
			}else{
				toFile("(" + x + "," + y + ")" + "-->",s);
				taxi2_out.get(count).send.add("(" + x + "," + y + ")" + "-->");
			}
			
			if(y==ysrc+1)		dir = 0;
			else if(y==ysrc-1)	dir = 1;
			else if(x==xsrc-1)	dir = 2;
			else if(x==xsrc+1)	dir = 3;
			
			if(Weights[shortpath[i]][shortpath[i+1]]==1000000){
				v0 = shortpath[i];
				VNUM = 6400;
				path=Dijkstra(v0,VNUM,Weights);
				
				for(int m = 0; m < shortpath.length;m++){
					shortpath[m] = 0;
				}
				
				v = dst_x*80 + dst_y;
				while(v != v0){
					shortpath1.add(path[v]);
					v = path[v];
				}
				
				int n = i;
				for(int j = shortpath1.size()-1; j >= 0; j--){
					shortpath[n] = shortpath1.get(j);
					n++;
				}			
			}
			
		}
		
		toFile("\r\n",s);	
		try {
			sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		x = dst_x;
		y = dst_y;
		state = 0;		
		Point p = new Point(x, y); 
		gui.SetTaxiStatus(id,p,state);
		
	}
	
	public synchronized void order_taking(int[][] Weights){
		/*@ REQUIRES: int[][] Weights;
		  @ MODIFIES: taxis,gui;
		  @ EFFECTS: find the shortest route that conforms to the condition to drive;
		  @ THREAD_REQUIRES: \locked(this.taxis);
		  @ THREAD_EFFECTS: \locked();
		  @*/
		int v0;
		int VNUM;
		int v;
		int[] path = new int[6400];
		ArrayList<Integer> shortpath2 = new ArrayList<>();
		
		taxi2_out.add(new Taxi_output());
		taxi2_out.get(count).req = req;
		taxi2_out.get(count).time = req.getReqtime();
		taxi2_out.get(count).x = x;
		taxi2_out.get(count).y = y;
		taxi2_out.get(count).src_x = req.getsrc_x();
		taxi2_out.get(count).src_y = req.getsrc_y();
		taxi2_out.get(count).dst_x = req.getdst_x();
		taxi2_out.get(count).dst_y = req.getdst_y();
		
		
		v0 = x*80 + y;
		VNUM = 6400;
		path=Dijkstra(v0,VNUM,Weights);
		
		v = src_x*80 + src_y;
		shortpath2.add(v);
		while(v != v0){
			shortpath2.add(path[v]);
			v = path[v];
		}
		
		int[] shortpath = new int[6400];
		int k = 0;
		for(int i = 0; i < 6400; i++)
			shortpath[i] = 1000000;
		for(int i = shortpath2.size()-1; i >= 0; i--){
			shortpath[k++] = shortpath2.get(i);
		}
		
		
		String s ="[CR,("+req.getsrc_x()+","+req.getsrc_y()+"),("+req.getdst_x()+","+req.getdst_y()+")]" + ".txt";
		toFile("接单行驶路线：",s);
		
		for(int i = 0; shortpath[i]!=1000000; i++){
			long time = 0;
			int flag = 0;
			int xsrc = x;
			int ysrc = y;
			if(lights[xsrc][ysrc].getvalid() == 1){
				if(lights[xsrc][ysrc].getDir() == 0){
					if(dir==0 && shortpath[i]/80==xsrc && shortpath[i]%80==ysrc+1)		flag = 1;
					else if(dir==1 && shortpath[i]/80==xsrc && shortpath[i]%80==ysrc-1)	flag = 1;
					else if(dir==2 && shortpath[i]/80==xsrc && shortpath[i]%80==ysrc-1) flag = 1;
					else if(dir==3 && shortpath[i]/80==xsrc && shortpath[i]%80==ysrc+1)	flag = 1;
				}else if(lights[xsrc][ysrc].getDir() == 1){
					if(dir==0 && shortpath[i]%80==ysrc && shortpath[i]/80==xsrc-1)	flag = 1;
					if(dir==1 && shortpath[i]%80==ysrc && shortpath[i]/80==xsrc+1)	flag = 1;
					if(dir==2 && shortpath[i]%80==ysrc && shortpath[i]/80==xsrc-1) 	flag = 1;
					if(dir==3 && shortpath[i]%80==ysrc && shortpath[i]/80==xsrc+1)	flag = 1;
				}
				
				if(flag == 1){
//					System.out.println("wait for traffic lights");
					time = lights[x][y].gettime() - (System.currentTimeMillis() - initTime)%lights[x][y].gettime();
				}
			}
			try {
				sleep(200+time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			y =shortpath[i]%80;
			x = shortpath[i]/80;	
			Point p = new Point(x, y); 
			gui.SetTaxiStatus(id,p,state);
			
			if(x==src_x && y==src_y){
				toFile("(" + x + "," + y + ")",s);	
				taxi2_out.get(count).take.add("(" + x + "," + y + ")");
				break;
			}else{
				toFile("(" + x + "," + y + ")" + "-->",s);	
				taxi2_out.get(count).take.add("(" + x + "," + y + ")" + "-->");
			}
			
			if(y==ysrc+1)		dir = 0;
			else if(y==ysrc-1)	dir = 1;
			else if(x==xsrc-1)	dir = 2;
			else if(x==xsrc+1)	dir = 3;
			
			if(Weights[shortpath[i]][shortpath[i+1]]==1000000){
				
				v0 = shortpath[i];
				VNUM = 6400;
				path=Dijkstra(v0,VNUM,Weights);
				
				for(int m = 0; m < shortpath.length;m++){
					shortpath[m] = 0;
				}
				
				v = src_x*80 + src_y;
				while(v != v0){
					shortpath2.add(path[v]);
					v = path[v];
				}
				
				int n = i;
				for(int j = shortpath2.size()-1; j >= 0; j--){
					shortpath[n] = shortpath2.get(j);
					n++;
				}			
			}
		}
		toFile("\r\n",s);	
		try {
			sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		x = src_x;
		y = src_y;
		state = 0;
		Point p = new Point(x, y); 
		gui.SetTaxiStatus(id,p,state);
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		state = 1;
		p = new Point(x, y); 
		gui.SetTaxiStatus(id,p,state);
	}


	public synchronized void waiting(int[][] map){
		/*@ REQUIRES: int[][] map;
		  @ MODIFIES: taxis,gui;
		  @ EFFECTS: find the road where the traffic volume is the minimum to drive;
		  @ THREAD_REQUIRES: \locked(this.taxis);
		  @ THREAD_EFFECTS: \locked();
		  @*/
		long time = 0;
		int xsrc = x;
		int ysrc = y;
		if(lights[xsrc][ysrc].getvalid()==1){
			time = lights[x][y].gettime() - (System.currentTimeMillis()-initTime)%lights[xsrc][ysrc].gettime();
		}
		try {
			sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[] edge = new int[4];	//	上下左右
		for(int i = 0; i < 4; i++){
			edge[i] = 0;
		}

		if(x==0 && y!= 0){
				if(map[x][y-1]==1 || map[x][y-1]==3){				
					edge[2] = 1;
				}
				if(map[x][y]==1 || map[x][y]==3){				
					edge[3] = 1;
				}
				if(map[x][y]==2 || map[x][y]==3){
					edge[1] = 1;
				}
			}else if(y==0 && x!=0){
				if(map[x-1][y]==2 || map[x-1][y]==3){
					edge[0] = 1;
				}
				if(map[x][y]==1 || map[x][y]==3){				
					edge[3] = 1;
				}
				if(map[x][y]==2 || map[x][y]==3){
					edge[1] = 1;
				}
			}else if(x==0 && y==0){
				if(map[x][y]==1 || map[x][y]==3){				
					edge[3] = 1;
				}
				if(map[x][y]==2 || map[x][y]==3){
					edge[1] = 1;
				}
			}else{
				if(map[x-1][y]==2 || map[x-1][y]==3){
					edge[0] = 1;
				}
				if(map[x][y-1]==1 || map[x][y-1]==3){				
					edge[2] = 1;
				}
				if(map[x][y]==1 || map[x][y]==3){				
					edge[3] = 1;
				}
				if(map[x][y]==2 || map[x][y]==3){
					edge[1] = 1;
				}
			}
			
			int count = 0;
			for(int i = 0; i < 4; i++){
				if(edge[i]==1){
					count ++;
				}
			}
			int index;
			Random random1 = new Random();
			index = random1.nextInt(count);
			int num= 0;
			int tempx = x;
			int tempy = y;
			for(int i = 0; i < 4; i++){
				if(edge[i]==1){
					num ++;
					if(num == index + 1){
						if(i==0){
							tempx = x - 1;
						}
						if(i==1){
							tempx = x + 1;
						}
						if(i==2){
							tempy = y - 1;
						}
						if(i==3){
							tempy = y + 1;
						}
					}
				}
			}
			int flag = 0;
			if(lights[xsrc][ysrc].getvalid() == 1){
				if(lights[xsrc][ysrc].getDir()==0){
					if(dir==0 && tempx==xsrc && tempy==ysrc+1)	flag = 1;
					if(dir==1 && tempx==xsrc && tempy==ysrc-1)	flag = 1;
					if(dir==2 && tempx==xsrc && tempy==ysrc-1) 	flag = 1;
					if(dir==3 && tempx==xsrc && tempy==ysrc+1)	flag = 1;
				}else if(lights[xsrc][ysrc].getDir()==1){
					if(dir==0 && tempy==ysrc && tempx==xsrc-1)	flag = 1;
					if(dir==1 && tempy==ysrc && tempx==xsrc+1)	flag = 1;
					if(dir==2 && tempy==ysrc && tempx==xsrc-1) 	flag = 1;
					if(dir==3 && tempy==ysrc && tempx==xsrc+1)	flag = 1;
				}
			}
			if(flag == 1){
//				System.out.println("wait for traffic lights");
				try {
					sleep(time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			x = tempx;
			y = tempy;
	
			if(y==ysrc+1)		dir = 0; 
			else if(y==ysrc-1)	dir = 1;
			else if(x==xsrc-1)	dir = 2;
			else if(x==xsrc+1)	dir = 3;
			
			Point p = new Point(x, y); 
			gui.SetTaxiStatus(id,p,state);
			
	}
	
	public synchronized void stoped(){
		/*@ REQUIRES: None;
		  @ MODIFIES: taxis,gui;
		  @ EFFECTS: the taxi stops for 1s && change the state;
		  @ THREAD_REQUIRES: \locked(this.taxis);
		  @ THREAD_EFFECTS: \locked();
		  @*/
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		state = 2;
		Point p = new Point(x, y); 
		gui.SetTaxiStatus(id,p,state);
	}
	
	
	int[] Dijkstra(int v0, int VNUM,int[][] Weights){
		/*@ REQUIRES: int v0, int VNUM,int[][] Weights;
		  @ MODIFIES: Spath;
		  @ EFFECTS: find the shortest route in the connected graph and store it in the Spath
		  @*/		
	    int i=0, j=0, v=0, minweight=0;
	    int[] Spath = new int[VNUM];	//用来最短路径的节点
	    int[] Sweight = new int[VNUM];
	    int[] wfound = new int[VNUM];//用于标记从v0到相应顶点是否找到最短路径，0未找到，1找到
	    
	    
	    for(i=0; i<VNUM; i++) { 
	    	Sweight[i] = Weights[v0][i];
	    	Spath[i] = v0;  //初始化数组Sweight和Spath
	    	wfound[i] = 0;
	    }
	  
	    Sweight [v0] = 0; 
	    wfound [v0] = 1;  
	    for(i=0; i< VNUM-1; i++) {  //迭代VNUM-1次
	         minweight = 1000000;
	         for(j=0; j <  VNUM;  j++) {  //找到未标记的最小权重值顶点 
	             if( wfound[j]==0 && (Sweight[j] < minweight)){
	                 v = j; 
	                 minweight = Sweight[v];
	             }
	         }
	         wfound[v] = 1;	//标记该顶点为已找到最短路径
	         for(j=0; j < VNUM; j++){ //找到未标记顶点且其权值大于v的权值+(v,j)的权值，更新其权值
	             if( wfound[j]==0  && (minweight + Weights[v][j] < Sweight[j] )){
	                 Sweight[j] = minweight + Weights[v][j];
	                 Spath[j] = v;  //记录前驱顶点
	            }
	         }
	     }
	    return Spath;
	}

	
	int getstate(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == state;
		  @*/
		return state;
	}
	void setstate(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: state;
		  @ EFFECTS: None;
		  @*/
		state = a;
	}
	
	int getcredit(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == credit;
		  @*/
		return credit;
	}
	void setcredit(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: credit;
		  @ EFFECTS: None;
		  @*/
		credit = a;
	}
	
	int getcurrentx(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == x;
		  @*/
		return x;
	}
	int getcurrenty(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == y;
		  @*/
		return y;
	}
	
	int getid(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == id;
		  @*/
		return id;
	}
	void setid(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: id;
		  @ EFFECTS: None;
		  @*/
		id = a;
	}
	
	int getsrc_x(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == src_x;
		  @*/
		return src_x;
	}
	void setsrc_x(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: src_x;
		  @ EFFECTS: None;
		  @*/
		src_x = a;
	}
	
	int getsrc_y(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == src_y;
		  @*/
		return src_y;
	}
	void setsrc_y(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: src_y;
		  @ EFFECTS: None;
		  @*/
		src_y = a;
	}
	
	int getdst_x(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == dst_x;
		  @*/
		return dst_x;
	}
	void setdst_x(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: dst_x;
		  @ EFFECTS: None;
		  @*/
		dst_x = a;
	}
	
	int getdst_y(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == dst_y;
		  @*/
		return dst_y;
	}
	void setdst_y(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: dst_y;
		  @ EFFECTS: None;
		  @*/
		dst_y = a;
	}
	
	void setreq(Request a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: req;
		  @ EFFECTS: None;
		  @*/
		req = a;
	}
	
	public static void toFile(String str,String path){
		/*@ REQUIRES: String str,String path;
		  @ MODIFIES: new file(path);
		  @ EFFECTS:  write str in the profile whose route is path 
		 */
		Charset charset = Charset.forName("UTF-8");
		try{
			FileOutputStream out = new FileOutputStream(path, true); 
			out.write(str.getBytes(charset)); 
			out.close();    
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
	
	public void print(){
		/*@ REQUIRES: None;
		  @ MODIFIES: None;
		  @ EFFECTS:  None 
		 */
	}
	
	
	public boolean repOK(){
		if(state != 0 && state != 1 && state != 2 && state != 3)	return false;
		if(x < 0 || x > 79)		return false;
		if(y < 0 || y > 79)		return false;
		if(credit < 0)			return false;
		if(src_x < 0 || src_x > 79)		return false;
		if(src_y < 0 || src_y > 79)		return false;
		if(dst_x < 0 || dst_x > 79)		return false;
		if(dst_y < 0 || dst_y > 79)		return false;
		if(id < 0 || id > 99)	return false;
		if(dir != 0 && dir != 1 && dir!= 2 && dir != 3)		return false;
		if(map==null)	return false;
		for(int i = 0; i < 80; i++){
			for(int j = 0; j < 80; j++){
				int x = map[i][j];
				if(x!=0 && x!=1 && x!=2 && x!=3)
					return false;
			}
		}
		
		if(Weights == null)		return false;
		for(int i = 0; i < 6400; i++){
			for(int j = 0; j < 6400; j++){
				int num = Weights[i][j];
				if(num!=1 && num!=1000000)
					return false;
			}
		}
		
		if(lights==null)		return false;
		for(int i = 0; i < 80; i++){
			for(int j = 0; j < 80; j++){
				lights[i][j].repOK();
			}
		}
		return true;
		
	}
	
	
}
