package Servidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class Servidor extends Thread{
	//Conection
	private static final int PUERTO = 3400; //Puerto
	private static CyclicBarrier barrera;
	private static final String LOGPATH = "logs/Servidor/";
	//Client
	private static int totalClients = 0;
	private static int clientCounter = 1;
	//file
	private int tipoArchivo;
	private String fileName;
	private String hash;
	private File file;
	private long fileSize;

	
	public Servidor(int tipoArchivo, int totalClients) {
		Servidor.totalClients = totalClients;
		this.tipoArchivo = tipoArchivo;
		barrera = new CyclicBarrier(totalClients);
	}
	public void run() {
        ServerSocket ss;
		//LOGS DATE
		File logFile = new File(LOGPATH+getDate()+"log.txt");
		try {
			//File and Hash
			file = new File(tipoArchivo ==100? "assets/Servidor/Interstellar.webm":"assets/Servidor/f2");
			this.fileSize = file.length();
			this.fileName = file.getName();

			MessageDigest ms =MessageDigest.getInstance("SHA-256");
			hash = Servidor.checksum(ms,file);

			//LOGS
			FileOutputStream logOutput = new FileOutputStream(logFile);
			String message = "Name File:"+this.fileName+" Size:"+String.valueOf(tipoArchivo)+"MB";
			logOutput.write(message.getBytes(), 0, message.length());
			logOutput.close();

		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		try {
			ss = new ServerSocket(PUERTO);
			//defined by the serverSocket
			int MAXCLIENTS = 50;
			while(clientCounter <= totalClients){
				Socket s = ss.accept();
				//Threads
				System.out.println("Se recibe una conexion de cliente (numero "+clientCounter+")");
				Handler thread= new Handler(s, barrera, this.file, this.fileSize, this.hash,this.fileName, clientCounter, totalClients, logFile);
				thread.start();
				System.out.println("TOTAL:"+clientCounter+'/'+totalClients);
				clientCounter++;
			}
			// while(clientCounter <= MAXCLIENTS ) {
			// }
			join();
			ss.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	// Appends information to file
	public static void writeLog(String message,File file) throws IOException{
		if (file.exists()){
			Scanner myReader = new Scanner(file);
			String data = "";
			while (myReader.hasNextLine()) {
			  data = data+myReader.nextLine()+"\n";
			}
			myReader.close();
			FileOutputStream output = new FileOutputStream(file);
			output.write((data+message).getBytes(), 0,message.length()+data.length());
			output.close();
		}else{
			FileOutputStream output = new FileOutputStream(file);
			output.write(message.getBytes(), 0, message.length());
			output.close();
		}
	}
	public static String getDate(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");  
		Date date = new Date();  
		String strdate = String.valueOf(formatter.format(date)); 
		return strdate; 
	}
	public static String checksum(MessageDigest digest,File file) throws IOException{
		// Get file input stream for reading the file
		// content
		FileInputStream fis = new FileInputStream(file);

		// Create byte array to read data in chunks
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		// read the data from file and update that data in
		// the message digest
		while ((bytesCount = fis.read(byteArray)) != -1)
		{
		digest.update(byteArray, 0, bytesCount);
		};

		// close the input stream
		fis.close();

		// store the bytes returned by the digest() method
		byte[] bytes = digest.digest();

		// this array of bytes has bytes in decimal format
		// so we need to convert it into hexadecimal format

		// for this we create an object of StringBuilder
		// since it allows us to update the string i.e. its
		// mutable
		StringBuilder sb = new StringBuilder();

		// loop through the bytes array
		for (int i = 0; i < bytes.length; i++) {

		// the following line converts the decimal into
		// hexadecimal format and appends that to the
		// StringBuilder object
		sb.append(Integer
		.toString((bytes[i] & 0xff) + 0x100, 16)
		.substring(1));
		}

		// finally we return the complete hash
		return sb.toString();
	}
} 

