package taxi;

import java.util.ArrayList;

/*
 * Overview:
 * 定义可追踪出租车输出信息的属性
 */
public class Taxi_output {
	Request req;
	long time;
	int src_x, src_y;
	int dst_x, dst_y;
	int x, y;
	ArrayList<String> take = new ArrayList<>();
	ArrayList<String> send = new ArrayList<>();
	
}
