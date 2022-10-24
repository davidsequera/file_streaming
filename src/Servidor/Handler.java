package Servidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CyclicBarrier;

class Handler extends Thread{
	//threads
	private static CyclicBarrier barrera;
	//Streams
	private Socket s;
	OutputStream output;
	InputStream inputHash;
	//Files
	private File file = null;
	private long fileSize = 0;
	private String fileHash;
	private String fileName;
	private File logFile;
	//Clients
	private int clientCounter;
	private int totalClients;
	private static final int CHUNKSIZE = 50;


	public Handler(Socket s, CyclicBarrier br, File file, long fileSize,String fileHash,String fileName, int clientCounter, int totalClients, File logFile) throws IOException, IOException {
		this.s =s;
	    Handler.barrera = br;
	    output = s.getOutputStream();
	    inputHash = s.getInputStream();
	    this.file = file;
	    this.fileSize = fileSize;
	    this.fileHash = fileHash;
	    this.fileName = fileName;
	    this.clientCounter = clientCounter;
	    this.totalClients = totalClients;
	    this.logFile = logFile;
	}
	
	public void run(){
		try {
			FileInputStream input = null;
			input = new FileInputStream(this.file);

		    System.out.println("Se esperan a los usuarios\t Usuario:" + clientCounter+"\tEsperando: "+barrera.getNumberWaiting()+"/"+totalClients);
			
			barrera.await();
		    System.out.println("Se procede a enviar los archivos");
		    long time1 = System.currentTimeMillis();

			//Metadata
			this.sendMetadata();
			
			writeFile(input, CHUNKSIZE, output);
			input.close();

		    long time2 = System.currentTimeMillis();
		    long total = time2-time1;

			//getHash from CLient
			byte bhash[] = new byte[this.fileHash.length()];
			inputHash.read(bhash, 0, this.fileHash.length());
			String clientHash = new String(bhash);
			System.out.println("[Client"+clientCounter+"Hash]"+clientHash);
			boolean GoodRead = clientHash.equals(this.fileHash) ? true : false;

			barrera.await();
			// System.out.println("[CONTROL]");

			synchronized(this.logFile){
				System.out.println("Se enviaron los archivos a"+ " Usuario " + clientCounter+ " Tiempo: " + total );
				Servidor.writeLog("Cliente:"+clientCounter+"\tVerificacion:"+GoodRead+"\tServer:"+this.fileHash+"\tCient:"+clientHash+"\tTiempo:"+String.valueOf(total), this.logFile);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeFile(FileInputStream input, int chunkSize, OutputStream output) throws IOException{
		byte[] bytes = new byte[chunkSize*1024*1024];//NMB to avoid OutOfMemoryError
		int count;
		while ((count = input.read(bytes)) > 0) {
			synchronized(output){
				output.write(bytes, 0, count);
			}
		}
		// System.out.println("[Server] FILE END");
	}
	public void sendMetadata() throws IOException{
		//Metadata
		DataOutputStream intagerSend = new DataOutputStream(s.getOutputStream());
		// Sends total amount of clients
		intagerSend.writeInt(totalClients);
		// Sends the id of the client
		intagerSend.writeInt(clientCounter);
		// Sends the size of the file
		intagerSend.writeLong(this.fileSize);
		// Sends the file hash
		intagerSend.writeUTF(this.fileHash);
		// Sends the file name
		intagerSend.writeUTF(this.fileName);
		// System.out.println("[Server] METADATA");
	}
}
