package metweaks.client.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;

public class TosNoticeReader extends Thread {
	
	static List<String> configChanges = new ArrayList<>();
	static List<String> configChangesASM = new ArrayList<>();
	static List<String> linesLeft = new ArrayList<>();
	static List<String> linesRight = new ArrayList<>();
	
	private boolean fetchTime = MeTweaksConfig.tosNotice == 1;
	private String file = fetchTime ? "reenable-notice.txt" : "tos-notice.txt";
	
	public TosNoticeReader(String name) {
		super(name);
		start();
	}

	public static void fetchTOSNotice() {
		
		new TosNoticeReader(MeTweaks.MODNAME+" TOS Notice Reader");
	}
	
	public void processReenableTime(BufferedReader reader) throws IOException, NumberFormatException {
		long time = Long.parseLong(reader.readLine());
		long last = Long.parseLong(MeTweaksConfig.reenableNoticeTime);
		if(time > last) {
			GuiTOSNotice.setEnableOnDemand(2);
			MeTweaksConfig.config.save();
		}
		if(MeTweaksConfig.debug >= 1) {
    		System.out.println("Reenable: "+(time > last)+" Last: "+last+" Time: "+time);
		}
	}
	
	public void processNotice(BufferedReader reader) throws IOException {
		int mode = 0;
	    String line;
	    while ((line = reader.readLine()) != null) {
	    	if(!line.isEmpty()) {
	    		char start = line.charAt(0);
				if(start == '$') {
					mode++;
					continue;
				}
	    	}
	    	
	    	switch(mode) {
	    		case 0:
	    			linesLeft.add(line);
	    			break;
	    		case 1:
	    			linesRight.add(line);
	    			break;
	    		case 2:
	    			configChangesASM.add(line);
	    			break;
	    		case 3:
	    			configChanges.add(line);
	    			break;
	    	}
	    	
	    }
	}

	public void run() {
		
		
		
		try{
			HttpURLConnection request = null;
			BufferedReader reader = null;
			try {
				
				URL url = new URL("https://gitlab.com/Jonosa/MiddleEarth-Tweaks/raw/main/metweaks/"+file);
				request = (HttpURLConnection) url.openConnection();
				request.setConnectTimeout(4000);
				request.setReadTimeout(4000);
				request.setDoOutput(false);
			    request.connect();
			    reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8), 2048);
			    if(fetchTime) {
			    	processReenableTime(reader);
			    }
			    else {
			    	processNotice(reader);
			    }
			    reader.close();
			    request.disconnect();
			}
			catch(Exception ex) {
				
				System.out.println("Failed to read "+file+": "+ex.getClass().getName()+" "+ ex.getMessage());
				if(reader != null)
					reader.close();
				if(request != null)
					request.disconnect();
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		
		
		
		
		
	}
}
