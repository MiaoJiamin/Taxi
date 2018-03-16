package taxi;

public class TaxiSystem {
	public static void main(String[] args){
		long initTime = System.currentTimeMillis();
		Taxi[] taxis = new Taxi[100];
		
		TaxiGUI gui=new TaxiGUI();
		Map map1 = new Map();
		int[][] map = map1.readmap();
		gui.LoadMap(map, 80);
		
		lights light = new lights(gui);
		light.readtraffic_lights();
		light.start();
		
		int INF=1000000;
		int Weights[][] = new int[6400][6400];
		for(int init1 = 0 ; init1<6400 ; init1 ++){
			for(int init2 = 0 ; init2<6400 ; init2++){
				Weights[init1][init2]=INF;
			}
		}
		
		for(int m = 0 ; m<80; m ++){
			for(int n2 = 0 ; n2<80; n2++){
				switch(map[m][n2]){
				   case 0:break;
				   case 1:Weights[m*80+n2][m*80+n2+1]=1;Weights[m*80+n2+1][m*80+n2]=1;break;
				   case 2:Weights[m*80+n2][(m+1)*80+n2]=1;Weights[(m+1)*80+n2][m*80+n2]=1;break;
				   case 3:Weights[m*80+n2][m*80+n2+1]=1;Weights[m*80+n2+1][m*80+n2]=1;Weights[m*80+n2][(m+1)*80+n2]=1;Weights[(m+1)*80+n2][m*80+n2]=1;break;
				   default:break;
				}
			}
		}
		
		//Weights2 不变化;Weights随道路开闭变化
		int Weights2[][] = new int[6400][6400];
		for(int i = 0; i < 6400; i++){
			for(int j = 0; j < 6400; j++){
				Weights2[i][j] = Weights[i][j];
			}
		}	
		
		taxis = init_taxi(map, Weights, Weights2, gui, light.getlights(), initTime);

		ReqQueue Queue = new ReqQueue(map,Weights,gui);
		Queue.addreq(initTime,taxis);
		
	}
	
	static Taxi[] init_taxi(int[][] map, int[][] Weights, int[][] Weights2, TaxiGUI gui, Traffic_lights[][] lights, long initTime){
		Taxi[] taxis = new Taxi[100];
		//普通出租车
		for(int i = 0; i < 70; i++){
			taxis[i] = new Taxi(map,Weights,gui,lights, initTime);
			taxis[i].setid(i);
			taxis[i].start();
			gui.SetTaxiType(i,0);
		}
		
		//可追踪出租车
		for(int i = 70; i < 100; i++){
			taxis[i] = new Taxi2(map,Weights2,gui,lights, initTime);
			taxis[i].setid(i);
			taxis[i].start();
			gui.SetTaxiType(i,1);
			taxis[i].print();
			
		}
		
		return taxis;
	}
	
	
}