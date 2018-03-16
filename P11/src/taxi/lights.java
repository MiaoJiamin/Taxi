package taxi;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Random;

/*
 * Overview:
 *读取文件traffic lights.txt中的内容，存入lights[80][80]中，并且根据相应的时间实现红绿灯的颜色变化
 *lights.valid 为0或1
 *当lights.valid 为1时	lights.Dir 为0或1  	lights.time 为200到500之间的数
 * 
 * 不变式: (lights!=null) && (for all 0<=i,j<80, lights[i][j].repOK())
 */
public class lights extends Thread{
	private TaxiGUI gui;
	private Traffic_lights[][] lights = new Traffic_lights[80][80];
	
	lights(TaxiGUI gui){
		/*@ REQUIRES: TaxiGUI gui;
		  @ MODIFIES: this.gui;
		  @			  this.lights;
		  @ EFFECTS: None;
		  @*/
		this.gui = gui;
		for(int i = 0; i < 80; i++){
			for(int j = 0; j < 80; j++){
				lights[i][j] = new Traffic_lights();
				lights[i][j].setvalid(0);
				lights[i][j].setDir(0);
				lights[i][j].settime(0);
			}
		}
	}
	
	public void run(){
		/*@ REQUIRES: None;
		  @ MODIFIES: lights;
		  @ EFFECTS: (\forall Traffic_lights lights[i][j] && 0<=i<=79 && 0<=j<=79;
		  @			 lights[i][j].valid == 1;
		  @			 time[i][j] = System.currentTimeMillis()) 
		  @*/
		long[][] time = new long[80][80];
		for(int i = 0; i < 80; i++){
			for(int j = 0; j < 80; j++){
				if(lights[i][j].getvalid() == 1){
					time[i][j] = System.currentTimeMillis();
				}
			}
		}
		while(true){
			changelights(time);
		}
	}
	public void readtraffic_lights(){
		/*@ REQUIRES: None;
		  @ MODIFIES: lights;
		  @ EFFECTS: normal_behavior 
		  @			 (\forall temp=reader.read(); 
		  @			 temp!=-1 && (temp==0 || temp==1);
		  @			 temp=reader.read()) ==> lights[row][column].valid = temp; Dir==0||Dir==1; time为50到100之间的任意数
		  @			 (traffic lights.txt不存在)==>exceptional_behavior(Exception);
		  @*/
		File file = new File("traffic lights.txt");
		Reader reader = null;
		try{
			reader = new InputStreamReader(new FileInputStream(file));
			int temp;
			int row = 0;
			while((temp = reader.read()) != -1){		//80*80  无其他字符
				int column = 0;
				
				while(temp != '\r' && temp != '\n' && temp != -1){
					if(temp == ' ' || temp == '\t'){
						temp = reader.read();
						continue;
					}else if(temp == '0' || temp == '1'){
						Point p = new Point(row,column);
						lights[row][column].setvalid(temp - '0');
						if(temp=='0')	gui.SetLightStatus(p,0);
						
						Random random = new Random();
						lights[row][column].settime(random.nextInt(301)+200);
//						lights[row][column].settime(500);
						
						int tempdir = random.nextInt(2);
						lights[row][column].setDir(tempdir);
						if(tempdir==0)	gui.SetLightStatus(p,2);
						else if(tempdir==1)	gui.SetLightStatus(p, 1);
						
						
						column ++;
					}
					temp = reader.read();
				}
				row ++;
				temp = reader.read();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changelights(long[][] time){
		/*@ REQUIRES: time;
		  @ MODIFIES: lights;
		  @ EFFECTS:  (\all Traffic_lights lights[i][j]; (System.currentTimeMillis() - time[i][j]) > lights[i][j].gettime();
		  @				if(\old(lights[i][j].Dir==1))==>lights[i][j].Dir==0
		  @				if(\old(lights[i][j].Dir==0))==>lights[i][j].Dir==1)
		  @*/
		for(int i = 0; i < 80; i++){
			for(int j = 0; j < 80; j++){
				if(lights[i][j].getvalid() == 1){
					while((System.currentTimeMillis() - time[i][j]) > lights[i][j].gettime()){
						if(lights[i][i].getDir() == 1){
							lights[i][j].setDir(0);
							Point p = new Point(i,j);
							gui.SetLightStatus(p, 2);
						}else if(lights[i][j].getDir() == 0){
							lights[i][j].setDir(1);
							Point p = new Point(i,j);
							gui.SetLightStatus(p, 1);
						}
						time[i][j] = System.currentTimeMillis();
					}
				}
			}
		}	
	}
	
	Traffic_lights[][] getlights(){
		/*@ REQUIRES: None;
		  @ MODIFIES: None;
		  @ EFFECTS: \result = lights;
		  @*/
		return lights;
	}
	
	public boolean repOK(){
		if(lights==null)
			return false;
		for(int i = 0; i < 80; i++){
			for(int j = 0; j < 80; j++){
				lights[i][j].repOK();
			}
		}
		return true;
	}
}
