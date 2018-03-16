package taxi;

/*
 * Overview:
 * 定义Traffic_lights类的属性
 * 
 * 不变式: (valid==0 || valid==1) && (Dir==0 || Dir==1) && (time==0 ||(50<=time<=100))
 */

public class Traffic_lights {
	private int valid;	//0:没灯			1:有灯
	private int Dir=0;	//0:东西红		1:南北红
	private int time=0;
	
	int getvalid(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == valid;
		  @*/
		return valid;
	}
	
	void setvalid(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: valid;
		  @ EFFECTS: None;
		  @*/
		valid = a;
	}
	
	int getDir(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == Dir;
		  @*/
		return Dir;
	}
	
	void setDir(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: Dir;
		  @ EFFECTS: None;
		  @*/
		Dir = a;
	}
	
	int gettime(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == time;
		  @*/
		return time;
	}
	
	void settime(int a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: time;
		  @ EFFECTS: None;
		  @*/
		time = a;
	}
	
	public boolean repOK(){
		int valid = this.valid;
		int Dir = this.Dir;
		int time = this.time;
		if(valid != 0 && valid != 1)
			return false;
		if(Dir != 0 && Dir != 1)
			return false;
		if(time != 0 && ((time != 0 && time < 50) || time >100))
			return false;
		
		return true;
		
	}
	
}
