package metweaks.proxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public class UpdateCheck extends Thread {
	
	public static boolean canNotify(EntityPlayer player) {
		MinecraftServer server = MinecraftServer.getServer();
		if(server.isSinglePlayer()) {
			
			return true;
		}
		else {													

			return server.getConfigurationManager().func_152596_g(player.getGameProfile());
		}
		
	}
	
	public static boolean remoteAvailable;
	
	public static boolean canNotifyClient() {
		MinecraftServer server = MinecraftServer.getServer();
		if(server != null && server.isServerRunning() && server.isSinglePlayer()) {
			return false;
		}
		return !remoteAvailable;
	}
	
	
	
	
	
	public static String LATEST;
	public static String DOWNLOAD_URL;
	public static String LATEST_TOS;
	public static String DOWNLOAD_URL_TOS;
	
	public UpdateCheck(String name) {
		super(name);
		start();
	}
	
	public static boolean hasUpdate(boolean tos) {
		return (tos ? LATEST_TOS : LATEST) != null;
	}

	public static void checkForUpdate() {
		
		new UpdateCheck(MeTweaks.MODNAME+" UpdateCheck");
	}
	
	public static IChatComponent notifyUpdate(boolean tos) {
		
		
		ChatComponentText text = new ChatComponentText(StatCollector.translateToLocalFormatted("metweaks.updateAvaible", tos ? LATEST_TOS : LATEST));
		ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, tos ? DOWNLOAD_URL_TOS : DOWNLOAD_URL);
        text.getChatStyle().setChatClickEvent(click);
		LATEST = null;
		DOWNLOAD_URL = null;
		LATEST_TOS = null;
		DOWNLOAD_URL_TOS = null;
		return text;
	}

	public void run() {
		
		HttpURLConnection request = null;
		try {
			
			URL url = new URL("https://gitlab.com/Jonosa/MiddleEarth-Tweaks/raw/main/metweaks/version.txt");
			request = (HttpURLConnection) url.openConnection();
			request.setConnectTimeout(4000);
			request.setReadTimeout(4000);
			request.setDoOutput(false);
			
		    request.connect();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()), 1024);
		    String str_latest = reader.readLine();
		    String str_download = reader.readLine();
		    
		    String str_latest_tos = reader.readLine();
		    String str_download_tos = reader.readLine();
		    reader.close();
		    
		    request.disconnect();
		    
		    
		    if(str_latest != null && str_download != null) {
		    	
		    	if(needsUpdate(MeTweaks.VERSION, str_latest.trim())) {
		        	LATEST = str_latest;
		        	DOWNLOAD_URL = str_download.trim();
		        	
		        	if(str_latest_tos != null && str_download_tos != null) {
			        	if(needsUpdate(MeTweaks.VERSION, str_latest_tos.trim())) {
			        		LATEST_TOS = str_latest_tos;
				        	DOWNLOAD_URL_TOS = str_download_tos.trim();
			        	}
		        	}
		        }
		        else if(MeTweaksConfig.debug >= 1) {
		        	System.out.println("Everything up to date.");
		        }
		    	
		    	if(MeTweaksConfig.debug >= 1) {
		    		System.out.println("Latest: "+LATEST+" Current: "+MeTweaks.VERSION+" Download: "+DOWNLOAD_URL);
		    		System.out.println("TOS: Latest: "+LATEST_TOS+" Download: "+DOWNLOAD_URL_TOS);
		    	}
		    }
		    else {
		    	throw new Exception("Request return is empty");
		    	
		    }
		}
		catch(Exception ex) {
			
			System.out.println("Failed to check for Updates: "+ex.getClass().getName()+" "+ ex.getMessage());
		
			if(request != null)
				request.disconnect();
		}
		
		
		
		
		
		
	}

	
	
	public static int[] parseVersion(String str_version) throws Exception {
		String[] str_versions = str_version.split("\\.", 4);
		int[] versions = new int[str_versions.length];
		
		for(int i = 0; i < str_versions.length; i++) {
			try {
				versions[i] = Integer.parseInt(str_versions[i]);
			}
			catch(NumberFormatException e) {
				
				throw new Exception("Unable to parse version: \""+str_version+"\"");
				
			}
		}
		return versions;
	}

	public static boolean needsUpdate(String current, String latest) throws Exception {
		int[] current_ver = parseVersion(current);
		int[] latest_ver = parseVersion(latest);
		
		for(int i = 0; i < Math.max(current_ver.length, latest_ver.length); i++) {
			int currentN = i < current_ver.length ? current_ver[i] : 0;
			int latestN = i < latest_ver.length ? latest_ver[i] : 0;
			
			if(latestN > currentN) {
				return true;
			}
			else if(latestN < currentN) {
				return false;
			}
		}
		return false;
	}
}
