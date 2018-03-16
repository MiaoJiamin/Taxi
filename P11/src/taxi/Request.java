package taxi;

/*
 * Overview:
 * 定义Request类属性
 * 
 * 不变式:(0<=src_x<=79) && (0<=src_y<=79) && (0<=dst_x<=79) && (0<=dst_y<=79)
 */
public class Request{
	private int src_x;
	private int src_y;
	private int dst_x;
	private int dst_y;
	private long Reqtime;
	
	public String toString(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result == s;
		  @*/
		String s = "[CR,("+src_x +","+src_y +"),("+ dst_x +","+ dst_y +")]";
		return s;
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
		  @ EFFECTS: \result ==  dst_y;
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
	
	long getReqtime(){
		/*@ REQUIRES: None
		  @ MODIFIES: None;
		  @ EFFECTS: \result ==  Reqtime;
		  @*/
		return Reqtime;
	}
	void setReqtime(long a){
		/*@ REQUIRES: int a;
		  @ MODIFIES: Reqtime;
		  @ EFFECTS: None;
		  @*/
		Reqtime = a;
	}
	
	
	public boolean repOK(){
		if(src_x < 0 || src_x > 79)	
			return false;
		if(src_y < 0 || src_y > 79)
			return false;
		if(dst_x < 0 || dst_x > 79)
			return false;
		if(dst_y < 0 || dst_y > 79)
			return false;
		
		return true;
	}
}
