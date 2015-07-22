package connection;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;


public class Filo {
	
	private static String path;
	@SuppressWarnings("unused")
	private static File logFile;
	private static File dir;
	@SuppressWarnings("unused")
	private static String name;
	
	
	private Filo(){
		
	}
	
	public static void log(String message)
	{
			PrintWriter printer = null;
			try{
				LocalDateTime timeStamp = LocalDateTime.now();
				String name = timeStamp.getMonthValue() + "-" + timeStamp.getDayOfMonth() + "-" + timeStamp.getYear() + ".txt";
				
				dir = new File("\\Logs");
				if(!dir.exists()){
					dir.mkdirs();
				}
				
				path = dir + "\\" + name;
				logFile = new File(path);
				
				printer = new PrintWriter(new FileWriter(path, true), true);
				printer.println(timeStamp.getHour() + ":" + timeStamp.getMinute() + ":" + timeStamp.getSecond() + " - " + message);
				
				printer.close();
				
			}
			catch(Exception e){
				Filo.log("Logging error: " + e.getMessage());
			}
			finally{
				if(printer != null){
					printer.close();
				}
			}
	}
}
