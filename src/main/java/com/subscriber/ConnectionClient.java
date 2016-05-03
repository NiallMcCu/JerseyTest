package com.subscriber;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ClientBinding;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ConnectionClient extends Thread{
	private static String query = "http://localhost:8080/myapp/subscribe/";
	private int delay;
	private int numberOfMessages = 10;
	private String filename;
	private FileWriter filewriter;
	private static int STEP = 1000;
	private int counter = 0;
	private int increment = STEP;
	private int previous = 0;
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	
	private static String getCurrentTS(){
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date now = new Date();
		return sdfDate.format(now);
	}
	
	public void run(){
		System.out.println("query = " + query);
		subscribe(filename);
		System.out.println("running Thread for " + filename + " delay = " + delay);
		try{
			while (true){
				Thread.sleep(1000);}
		}catch (InterruptedException e){
			e.printStackTrace();
		} finally {
			try {
				filewriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void subscribe (String name){
		System.out.println("suscribing to "+ query);
		EventListener listener = new EventListener() {
			@Override
			public void onEvent(InboundEvent inboundEvent){
				String data = null;
				if ((counter > (numberOfMessages + previous)) && (counter < (STEP+ previous)) && (delay != 0)){
					System.out.println(getCurrentTS() + " Sleeping " + filename);
					try {
						sleep(delay);
						System.out.println(getCurrentTS() + " back running " + filename);
					}catch (InterruptedException e){
						e.printStackTrace();
					}
					counter += STEP;
					increment += STEP;
					previous = counter;
				}
				try{
					try{
						if(inboundEvent.getId() == null){
							filewriter.write("inboundEvent is null");
							filewriter.flush();
							System.out.println("inboundEvent is null for "+ filename);
							return;
						}
						
						data = inboundEvent.readData();
						String comment = inboundEvent.getComment();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						
						Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(data.getBytes("utf-8"))));
						String countID = doc.getDocumentElement().getFirstChild().getTextContent();
						
						filewriter.write(inboundEvent.getName() + " : " + inboundEvent.getId() + "\t: " + comment + " : " + countID + "\n");
						filewriter.flush();
						counter += 1;
					} catch (RuntimeException e){
						filewriter.write(e.toString() + "\n");
						filewriter.write(inboundEvent.getName() + " : "+ inboundEvent.getId() + "\n");
						filewriter.flush();
						e.printStackTrace();
					} catch (SAXException | ParserConfigurationException e){
						filewriter.write(e.toString() + '\n' + getReducedString(data));
						e.printStackTrace();
						System.out.println("error data = " + getReducedString(data));
					}
				}catch (IOException e){
					System.out.println(e);
				}catch (ServiceUnavailableException ex){
					System.out.println("ServiceUnavailableException found = "+ ex);
					throw ex;
				}
			}
		};

		WebTarget target = ClientBuilder.newClient().register(SseFeature.class).target(query);
		
		EventSource source = EventSource.target(target).named(name).build();
		source.register(listener);
		source.open();
	}
	
	public ConnectionClient (String filename, int delay, int numberOfMessages) throws IOException {
		this.delay = delay;
		this.numberOfMessages = numberOfMessages;
		if(!filename.equals(null))
			this.filename = filename;
		else this.filename ="output.txt";
		
		filewriter = new FileWriter(this.filename);
	}
	
	public String getReducedLine(String fileText){
		Pattern p = Pattern.compile("\n");
		String[] strs = p.split(fileText, -1);
		System.out.println("count = " + strs.length);
		String shortResult = null;
		if(strs.length > 3){
			shortResult = "the first 2 & last 2 lines are \n" + strs[0] + "\n" + strs[1] + "\n" + strs[strs.length -2] + "\n" + strs[strs.length -1] + "\n";
		}
		return (shortResult == null) ? strs[0] : shortResult;
	}
	
	public String getReducedString(String fileText){
		int size = fileText.length();
		int character_lenght = 80;
		System.out.println("count = " + size);
		String shortResult = null;
		if (size > (character_lenght*2))
			shortResult = "the first " +character_lenght + " & last "+character_lenght
			+" Characters are \n" + fileText.substring(0, character_lenght) +
			"\n" + fileText.substring(size - character_lenght, size - 1);
		return (shortResult == null) ? fileText : shortResult;
	}

}
