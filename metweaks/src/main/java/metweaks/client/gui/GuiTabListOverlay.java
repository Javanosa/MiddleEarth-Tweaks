package metweaks.client.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import metweaks.ASMConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class GuiTabListOverlay extends Gui {

	public static GuiTabListOverlay instance = new GuiTabListOverlay();

	static SlotComparator ENTRY_ORDERING = new SlotComparator();

	static class SlotComparator implements Comparator<GuiPlayerInfo> {

		@Override
		public int compare(GuiPlayerInfo info1, GuiPlayerInfo info2) {
			Scoreboard board = Minecraft.getMinecraft().theWorld.getScoreboard();
			ScorePlayerTeam team1 = board.getPlayersTeam(info1.name);
			ScorePlayerTeam team2 = board.getPlayersTeam(info2.name);

			int compare = (team2 != null ? team2.getRegisteredName() : "").compareTo(team1 != null ? team1.getRegisteredName() : "");
			if(compare == 0) {
				return info1.name.compareTo(info2.name);
			}
			return compare;

		}
	}


	@SuppressWarnings("unchecked")
	public void renderPlayerlist(int width) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.mcProfiler.startSection("playerList");
		Scoreboard scoreboard = mc.theWorld.getScoreboard();
		ScoreObjective objective = scoreboard.func_96539_a(0);

		NetHandlerPlayClient nethandlerplayclient = mc.thePlayer.sendQueue;
		List<GuiPlayerInfo> list = nethandlerplayclient.playerInfoList;



		if(ASMConfig.tabListSort)
			Collections.sort(list, ENTRY_ORDERING);

		int maxLenName = 0;
		int maxLenScore = 0;

		for(GuiPlayerInfo playerInfo : list) {
			ScorePlayerTeam team2 = mc.theWorld.getScoreboard().getPlayersTeam(playerInfo.name);
			int lenName = mc.fontRenderer.getStringWidth(ScorePlayerTeam.formatPlayerName(team2, playerInfo.name));

			maxLenName = Math.max(maxLenName, lenName);

			if(objective != null) {
				int lenScore = mc.fontRenderer.getStringWidth(" " + scoreboard.func_96529_a(playerInfo.name, objective).getScorePoints());
				maxLenScore = Math.max(maxLenScore, lenScore);
			}
		}

		list = list.subList(0, Math.min(list.size(), 80)); // limits the list to 80 players (4 colums x 20 rows)
		int players = list.size();

		int rows = players;
		int columns;

		for(columns = 1; rows > 20; rows = (players + columns - 1) / columns) {
			columns++;
		}



		boolean showSkins = false;
		int lenScore;

		if(objective != null) {
			lenScore = maxLenScore;
		}
		else {
			lenScore = 0;
		}

		int columnSpacing = 5;
		int columnWidth = Math.min(columns * ((showSkins ? 9 : 0) + maxLenName + lenScore + 13), width - 50) / columns;
		int left = width / 2 - (columnWidth * columns + (columns - 1) * columnSpacing) / 2;

		int border = 10;
		int tabWidth = columnWidth * columns + (columns - 1) * columnSpacing;


		if(ASMConfig.tabListShowMaxPlayers) {
			List<String> headerList = null;


			IChatComponent header = new ChatComponentText("\u00a77"+players+"/"+nethandlerplayclient.currentServerMaxPlayers+" players");

			if(header != null) {
				headerList = mc.fontRenderer.listFormattedStringToWidth(header.getFormattedText(), width - 50);

				for(String line : headerList) {
					tabWidth = Math.max(tabWidth, mc.fontRenderer.getStringWidth(line));
				}
			}



			if(headerList != null) {
				drawRect(width / 2 - tabWidth / 2 - 1, border - 1, width / 2 + tabWidth / 2 + 1, border + headerList.size() * mc.fontRenderer.FONT_HEIGHT, Integer.MIN_VALUE);

				for(String line : headerList) {
					int len = mc.fontRenderer.getStringWidth(line);
					mc.fontRenderer.drawStringWithShadow(line, width / 2 - len / 2, border, -1);
					border += mc.fontRenderer.FONT_HEIGHT;
				}

				border++;
			}
		}

		drawRect(width / 2 - tabWidth / 2 - 1, border - 1, width / 2 + tabWidth / 2 + 1, border + rows * 9, Integer.MIN_VALUE);

		for(int entryIndex = 0; entryIndex < players; ++entryIndex) {
			int row = entryIndex / rows;
			int rowLeftOver = entryIndex % rows;
			int xPos = left + row * columnWidth + row * columnSpacing;
			int yPos = border + rowLeftOver * 9;
			drawRect(xPos, yPos, xPos + columnWidth, yPos + 8, 0x20FFFFFF);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL11.GL_ALPHA_TEST);

			if(entryIndex < list.size()) {
				GuiPlayerInfo playerInfo = list.get(entryIndex);



				ScorePlayerTeam scoreplayerteam = mc.theWorld.getScoreboard().getPlayersTeam(playerInfo.name);
				String name = ScorePlayerTeam.formatPlayerName(scoreplayerteam, playerInfo.name);


				mc.fontRenderer.drawStringWithShadow(name, xPos, yPos, -1);


				if(objective != null) {
					int endX = xPos + maxLenName + 1;
					int maxX = endX + lenScore;

					if (maxX - endX > 5) {
						drawScoreboardValues(objective, yPos, playerInfo.name, endX, maxX, playerInfo);
					}
				}

				drawPing(columnWidth, xPos - (showSkins ? 9 : 0), yPos, playerInfo);
			}
		}

		mc.mcProfiler.endSection();
	}

	public void drawPing(int columnWidth, int x, int y, GuiPlayerInfo playerInfo) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(icons);
		int ping = playerInfo.responseTime;

		int texOffset;

		if(ping < 0) {
			texOffset = 5; // 0 bars (x)
		}
		else if(ping < 150) {
			texOffset = 0; // 5 bars
		}
		else if(ping < 300) {
			texOffset = 1;
		}
		else if(ping < 600) {
			texOffset = 2;
		}
		else if(ping < 1000) {
			texOffset = 3;
		}
		else{
			texOffset = 4;
		}

		this.zLevel += 100.0F;
		this.drawTexturedModalRect(x + columnWidth - 11, y, 0, 176 + texOffset * 8, 10, 8);
		this.zLevel -= 100.0F;
	}


	private static void drawScoreboardValues(ScoreObjective objective, int y, String name, int endX, int maxX, GuiPlayerInfo info) {
		int i = objective.getScoreboard().func_96529_a(name, objective).getScorePoints();
		String score = EnumChatFormatting.YELLOW + "" + i;
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(score, maxX - Minecraft.getMinecraft().fontRenderer.getStringWidth(score), y, 0xFFFFFF);
	}


}
