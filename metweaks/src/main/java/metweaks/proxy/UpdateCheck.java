package metweaks.proxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import cpw.mods.fml.relauncher.ReflectionHelper;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListOps;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public class UpdateCheck extends Thread {
	
	private static Field field_opmap;
	
	public static boolean canNotify(EntityPlayer player) {
		MinecraftServer server = MinecraftServer.getServer();
		if(server.isSinglePlayer()) {
			
			return true;
		}
		else {													
			UserListOps ops = server.getConfigurationManager().func_152603_m();
			
			if(field_opmap == null)
				field_opmap = ReflectionHelper.findField(UserList.class, new String[] {"field_152696_d", "d"});
				
			try {
				@SuppressWarnings("rawtypes")
				Map map = (Map) field_opmap.get(ops);
				boolean notify = map != null && map.containsKey(player.getUniqueID().toString());
				
				if(notify) field_opmap = null;
				return notify;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
	
	/*
	
	if has op or singleplayer or everyone?
	reset updatecheck every 3 days (for servers that dont restart)
	dont stop after one player sent, keep things in a map
	
	if client has metweaks
	
	sent packet to client to tell them that i have sent the info
	
	
	
	
	
	
	#1 if client on multiplayer server, show update info if:
	- server has no metweaks
	- server has up to date metweaks
	
	#2 if client on multiplayer server, show update info if:
	- server has no metweaks
	- or server has up to date metweaks
	- or server has outdated metweaks {
	 		but update info disabled
	 		but you are not op
	 		"can show update" on join, can only run once
	 }
	 
	 
	 
	 #1A on server, write to log at server done if update info enabled
	 send to player if singleplayer or op (use cache)
	 reset update check every 3 days
	 
	 #4 client shows info only, for servers it does print to log
	 
	*/
	
	public static String LATEST;
	public static String DOWNLOAD_URL;
	
	public UpdateCheck(String name) {
		super(name);
		start();
	}

	public static void checkForUpdate() {
		
		new UpdateCheck(MeTweaks.MODNAME+" UpdateCheck");
	}
	
	public static IChatComponent notifyUpdate() {
		
		
		ChatComponentText text = new ChatComponentText(StatCollector.translateToLocalFormatted("metweaks.updateAvaible", LATEST));
		ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, DOWNLOAD_URL);
        text.getChatStyle().setChatClickEvent(click);
		LATEST = null;
		DOWNLOAD_URL = null;
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
		    reader.close();
		    
		    request.disconnect();
		    
		    
		    if(str_latest != null && str_download != null) {
		    	
		    	if(needsUpdate(MeTweaks.VERSION, str_latest.trim())) {
		        	LATEST = str_latest;
		        	DOWNLOAD_URL = str_download.trim();
		        }
		        else if(MeTweaksConfig.debug >= 1) {
		        	System.out.println("Everything up to date.");
		        }
		    	
		    	if(MeTweaksConfig.debug >= 1)
		    		System.out.println("Latest: "+LATEST+" Current: "+MeTweaks.VERSION+" Download: "+DOWNLOAD_URL);
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
