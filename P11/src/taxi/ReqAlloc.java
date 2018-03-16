package taxi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
/*
 * Overview:
 * 根据要求给每个合法请求分配出租车
 * 
 * 不变式:(taxis!=null) && (for all i, 0<=i<100,taxis[i].repOK()) && (req.repOK())
 */
public class ReqAlloc extends Thread{
	private Request req;
	private Taxi taxis[];
	private long initTime;
	
	
	ReqAlloc(Request req,Taxi taxis[],long initTime){
		/*@ REQUIRES: Request req,Taxi taxis[],long initTime
		  @ MODIFIES: this.req;
		  @			  this.taxis[];
		  @			  this.initTime;
		  @ EFFECTS: None;
		  @*/
		this.req = req;
		this.taxis=taxis;
		this.initTime = initTime;	
		
	}

	public void run(){
		/*@ REQUIRES: None;
		  @ MODIFIES: taxis[];
		  @ EFFECTS: None;
		  @*/
		alloc(req,taxis,initTime);
	}
	
	public void alloc(Request req,Taxi taxis[],long initTime){
		/*@ REQUIRES: Request req,Taxi taxis[],long initTime;
		  @ MODIFIES: taxis[];
		  @ EFFECTS: (\forall taxis[i];
		  @			 taxis[i]在请求发出地2*2范围外;
		  @			 taxis[i]<==>\old(taxis[i]));
		  @ EFFECTS: (\forall taxis[i];
		  @			 taxis[i]在请求发出地2*2范围内 && 为等待状态;
		  @			 qiangdan[i] = 1, taxis[i].getcredit() = \old(taxis[i].getcreadit)+1 );			 
		  @ EFFECTS: (\forall taxis[i];
		  @			 flag1[i]==1;
		  @			 打印“taxi:i  state:getstate() 信用信息: getcredit() 在范围内”到对应文件中);
		  @ EFFECTS: (\forall taxis[i];
		  @			 qiangdan[i]==1;
		  @			 打印“taxi:i 抢单”到对应文件中);
		  @ EFFECTS: \result == (\max int i; 0<=i && i<qiangdan.length;qiangdan[i].getcredit());
		  @ EFFECTS: \result == (\min int i; 0<=i && i<maxcredit_taxi.length;maxcredit_taxi[i]到请求发出地的最短距离);
		  @ EFFECTS: 如果mindis_taxi 存在，修改对应taix的属性，打印相应的信息
		  @			 如果不存在，输出提示
		  @*/
		int[] qiangdan = new int[100];
		int[] flag1 = new int[100];
		long time;
		while(((System.currentTimeMillis() - initTime)/100 - req.getReqtime()/100) < 30){
			for(int i=0; i<100;i++){

				if(taxis[i].getcurrentx()>=(req.getsrc_x()-2) && taxis[i].getcurrentx()<=(req.getsrc_x()+2) 
						&& taxis[i].getcurrenty()>=(req.getsrc_y()-2) && taxis[i].getcurrenty()<=(req.getsrc_y()+2)){
					flag1[i] = 1;
					if(taxis[i].getstate() == 2){
						if(qiangdan[i]==0){
							qiangdan[i] = 1;
							taxis[i].setcredit(taxis[i].getcredit() + 1);
						}
					}
				}
			}
			long time1 = System.currentTimeMillis();
			do{
				time = System.currentTimeMillis();
			}while(time - time1 <= 200);
		}

		for(int i = 0; i < 100; i++){
			if(flag1[i] == 1){
				String s = req.toString()+ ".txt"; 
				toFile("taxi:" + taxis[i].getid() + " 状态:" + taxis[i].getstate() + " 信用信息" + taxis[i].getcredit() + " 在范围内" +"\r\n",s);
//				System.out.println("taxi:" + taxis[i].getid() + " 状态:" + taxis[i].getstate() + " 信用信息" + taxis[i].getcredit() + " 在范围内");
			}
		}
		for(int i = 0; i < 100; i++){
			if(qiangdan[i] == 1){
				String s = req.toString()+ ".txt";
				toFile(taxis[i].getid() + "抢单" + "\r\n",s);
//				System.out.println(taxis[i].getid() + " 抢单");
			}
		}
		
		
		
		int maxcredit = 0;
		Taxi[] maxcredit_taxis = new Taxi[100];
		for(int i = 0; i< 100; i++){
			if(qiangdan[i] == 1){
				if(taxis[i].getcredit() > maxcredit){
					for(int j = 0; j < 100; j++){
						maxcredit_taxis[j] = null;
					}
					maxcredit = taxis[i].getcredit();
					maxcredit_taxis[i] = taxis[i];
				}else if(taxis[i].getcredit() == maxcredit){
					maxcredit_taxis[i] = taxis[i];
				}else{
					continue;
				}
			}else{
				continue;
			}
		}				//找到信用度最高的出租车,存到数组中对应的位置
		int flag = 0;
		int mindis = 6400;
		Taxi mindis_taxi = null;
		int[] distance = new int[100];
		for(int i = 0; i < 100; i++){
			if(maxcredit_taxis[i] != null){
				distance[i] = (maxcredit_taxis[i].getcurrentx()-req.getsrc_x())^2 + (maxcredit_taxis[i].getcurrenty()-req.getsrc_y())^2;
				if(distance[i] <= mindis){
					mindis = distance[i];
					mindis_taxi = maxcredit_taxis[i];
					flag = i;
				}else
					continue;
			}else
				continue;
		}		//找到距离最短的出租车mindis_taxi;
		if(mindis_taxi != null && mindis_taxi.getstate()==2){
			taxis[flag].setcredit(taxis[flag].getcredit()+3);
			taxis[flag].setsrc_x(req.getsrc_x());
			taxis[flag].setsrc_y(req.getsrc_y());
			taxis[flag].setdst_x(req.getdst_x());
			taxis[flag].setdst_y(req.getdst_y());
			taxis[flag].setstate(3);
			taxis[flag].setreq(req);
			
			String s = req.toString()+ ".txt";
//			toFile("[CR,("+req.getsrc_x()+","+req.getsrc_y()+"),("+req.getdst_x()+","+req.getdst_y()+")]",s);
			toFile(taxis[flag].getid() + "抢到单" + "\r\n",s);
			System.out.println(taxis[flag].getid() + " 抢到单" );
									
		}
		
		if(mindis_taxi == null){
			System.out.println(req.toString() + "无出租车响应");
		}
	}
	
	
	public static void toFile(String str,String path){
		/*@ REQUIRES: String str,String path;
		  @ MODIFIES: new file(path);
		  @ EFFECTS:  wtite str in the profile whose route is path;
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
	
	public boolean repOK(){
		if(taxis==null)		return false;
		for(int i = 0; i < 100; i++){
			taxis[i].repOK();
		}
		req.repOK();
		
		return true;
	}
	
}

