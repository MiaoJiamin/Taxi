package taxi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/*Overview:
 *读取文件map.txt中的内容，存入array[80][80]中
 *有效字符为0 1 2 3
 *
 *不变式: (array != null) && (for all i,j, 0<=i,j<80, map[i][j]==0||map[i][j]==1||map[i][j]==2||map[i][j]==3)
 */

public class Map {
	private int[][] array = new int[80][80];
	
	public int[][] readmap(){
		/*@ REQUIRES: None;
		  @ MODIFIES: array;
		  @ EFFECTS: normal_behavior 
		  @			 (\forall temp=reader.read(); 
		  @			 temp!=-1 && temp>=0 && temp<=3;
		  @			 temp=reader.read()) ==> array[row][column] = temp;
		  @			 (map.txt不存在)==>exceptional_behavior(Exception);
		  @*/
		File file = new File("map.txt");
		Reader reader = null;
		int flag = 0;
		try{
			reader = new InputStreamReader(new FileInputStream(file));
			int temp;
			int row = 0;
			while((temp = reader.read()) != -1){		//80*80  无其他字符
				int column = 0;
				if(flag == 0){
					while(temp != '\r' && temp != '\n' && temp != -1){
						if(temp == ' ' || temp == '\t'){
							temp = reader.read();
							continue;
						}else if(temp >= '0' && temp <= '3'){
							array[row][column] = temp - '0';
							column ++;
						}else{
							flag = 1;
							break;
						}
						temp = reader.read();
					}
					if(column < 80){
						flag = 1;
					}
					row ++;
				}else if(flag == 1){
					System.out.println("INVALID MAP");
					break;
				}
				temp = reader.read();
			}
			if(row < 80){
				flag = 1;
				System.out.println("INVALID MAP");
			}
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		if(flag == 0){			//特殊行列
			for(int i = 0; i < 80; i++){
				if(array[79][i]==2 || array[79][i]==3){
					System.out.println("INVALID MAP");
				}
			}
			for(int j = 0; j < 80; j++){
				if(array[j][79]==1 || array[j][79]==3){
					System.out.println("INVALID MAP");
				}
			}
		}

		return array;
	}	
	
	
	public boolean repOK(){
		if(array==null)
			return false;
		for(int i = 0; i < 80; i++){
			for(int j = 0; j < 80; j++){
				int x = array[i][j];
				if(x!=0 && x!=1 && x!=2 && x!=3)
					return false;
			}
		}
		return true;
		
	}
	
}
