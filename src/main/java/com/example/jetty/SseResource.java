package com.example.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;

/**
 * Root resource (exposed at "subscribe" path)
 */
@Path("subscribe")
public class SseResource {
	private static String getCurrentTS(){
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date now = new Date();
		return sdfDate.format(now);
	}
	
	static int global_count = 0;
	
	private String xml = "<breakfast_menu>\n<food>\n<name>Belgian Waffles</name>\n<price>$5.95</price>\n<description>\nTwo of our famous Belgian Waffles with plenty of real maple syrup\n</description>\n<calories>650</calories>\n</food>\n<food>\n<name>Strawberry Belgian Waffles</name>\n<price>$7.95</price>\n<description>\nLight Belgian waffles covered with strawberries and whipped cream\n</description>\n<calories>900</calories>\n</food>\n<food>\n<name>Berry-Berry Belgian Waffles</name>\n<price>$8.95</price>\n<description>\nLight Belgian waffles covered with an assortment of fresh berries and whipped cream\n</description>\n<calories>900</calories>\n</food>\n<food>\n<name>French Toast</name>\n<price>$4.50</price>\n<description>\nThick slices made from our homemade sourdough bread\n</description>\n<calories>600</calories>\n</food>\n<food>\n<name>Homestyle Breakfast</name>\n<price>$6.95</price>\n<description>\nTwo eggs, bacon or sausage, toast, and our ever-popular hash browns\n</description>\n<calories>950</calories>\n</food>\n</breakfast_menu>";
	private String xml_p1 = "<breakfast_menu><countID>";
	private String xml_p2 = "</countID>\n<food>\n<name>Belgian Waffles</name>\n<price>$5.95</price>\n<description>\nTwo of our famous Belgian Waffles with plenty of real maple syrup\n</description>\n<calories>650</calories>\n</food>\n<food>\n<name>Strawberry Belgian Waffles</name>\n<price>$7.95</price>\n<description>\nLight Belgian waffles covered with strawberries and whipped cream\n</description>\n<calories>900</calories>\n</food>\n<food>\n<name>Berry-Berry Belgian Waffles</name>\n<price>$8.95</price>\n<description>\nLight Belgian waffles covered with an assortment of fresh berries and whipped cream\n</description>\n<calories>900</calories>\n</food>\n<food>\n<name>French Toast</name>\n<price>$4.50</price>\n<description>\nThick slices made from our homemade sourdough bread\n</description>\n<calories>600</calories>\n</food>\n<food>\n<name>Homestyle Breakfast</name>\n<price>$6.95</price>\n<description>\nTwo eggs, bacon or sausage, toast, and our ever-popular hash browns\n</description>\n<calories>950</calories>\n</food>\n</breakfast_menu>";
	private String big_xml = null;
	
	public SseResource() {
		try{
			File f_big_xml = new File(ClassLoader.getSystemResource("xml-names-10-3e_1line.xml").toURI());
			this.big_xml = readFile(f_big_xml.getPath(), "utf-8");
		}catch (URISyntaxException | IOException e){
			e.printStackTrace();
		}
	}
 
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput getServerSentEvents() {
        final EventOutput eventOutput = new EventOutput();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                	System.out.println(getCurrentTS() + " starting counting at 0");
                    for (int i = 0; i < 1000; i++) {
                        // ... code that waits 1 second
//                    	Thread.sleep(100);
                        final OutboundEvent.Builder eventBuilder
                        = new OutboundEvent.Builder();
                        eventBuilder.name("message-to-client");
                        global_count++;
                        xml = xml_p1 + global_count + xml_p2;
                        if (((i % 2) == 0 ) & (i != 0))
                        	eventBuilder.data(String.class, big_xml);
                        else
                        	eventBuilder.data(String.class, xml);
                        eventBuilder.comment(getCurrentTS());
                        eventBuilder.id(Integer.toString(i));
                        eventBuilder.reconnectDelay(1);
                        final OutboundEvent event = eventBuilder.build();
                        eventOutput.write(event);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(
                        "Error when writing the event.", e);
//                } catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
				} finally {
                    try {
                        eventOutput.close();
                    } catch (IOException ioClose) {
                        throw new RuntimeException(
                            "Error when closing the event output.", ioClose);
                    }
                }
            }
        }).start();
        return eventOutput;
    }
    
    private String readFile(String path, String encoding) throws IOException{
    	byte[] encoded = Files.readAllBytes(Paths.get(path));
    	return new String(encoded, encoding);
    }
}
