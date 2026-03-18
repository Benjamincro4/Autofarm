package handlers.voicedcommandhandlers;

import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
/**
 * @author KingHanker
 *
 */
public class Autopot implements IVoicedCommandHandler
{
    private static final String[] _voicedCommands =
    {
        "autopotion",
        "enableautopotion",
        "disableautopotion",
        "disablehp",
        "disablemp",
        "disablecp",
        "sethp",
        "setmp",
        "setcp",
        "julio"
    };

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params) {
    	String status = "disabled";
    	boolean isEnable = false;
    	if (activeChar.isAutoPotionEnabled()) {
    		status = "enabled";
    		isEnable = true;
    	}
    	int hpThreshold = 0;
    	int mpThreshold = 0;
    	int cpThreshold = 0;
    	try {
	    	hpThreshold = Integer.parseInt(activeChar.getAPConfiguredHp());
	        mpThreshold = Integer.parseInt(activeChar.getAPConfiguredMp());
	        cpThreshold = Integer.parseInt(activeChar.getAPConfiguredCp());
    	} catch (NumberFormatException e) {
            activeChar.sendMessage("Invalid parameters. Must be a number.");
        }
    	
        if (command.equalsIgnoreCase("autopotion")) {
        	String char_config_hp = activeChar.getAPConfiguredHp();
        	String char_config_mp = activeChar.getAPConfiguredMp();
        	String char_config_cp = activeChar.getAPConfiguredCp();
        	
        	String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/autopot/autopot.htm");
    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
    		npcHtmlMessage.setHtml(htmContent);
    		npcHtmlMessage.replace("%acc_autopot_status%", status);
    		if(char_config_hp.equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_hp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_hp%", char_config_hp+"%");
    		if(char_config_mp.equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_mp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_mp%", char_config_mp+"%");
    		if(char_config_cp.equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_cp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_cp%", char_config_cp+"%");    		
    		activeChar.sendPacket(npcHtmlMessage);
        } else if(command.equalsIgnoreCase("enableautopotion") || command.equalsIgnoreCase("disableautopotion")){
        	if(command.equalsIgnoreCase("enableautopotion")){
        		activeChar.setAutoPotionConfig(hpThreshold, mpThreshold, cpThreshold, true); // Habilita auto-poción
        		if(hpThreshold == 0 && mpThreshold == 0  && cpThreshold == 0){
        			activeChar.setAutoPotionConfig(hpThreshold, mpThreshold, cpThreshold, false); // Deshabilita auto-poción
                    activeChar.sendMessage("Auto Potion has been disabled. HP (DISABLED), MP (DISABLED), CP (DISABLED).");
        		}else if(hpThreshold == 0 && mpThreshold > 0  && cpThreshold > 0){
                    activeChar.sendMessage("Auto Potion enabled with custom settings: HP (DISABLED), MP < " + mpThreshold + "%, CP < " + cpThreshold + "%.");
        		}else if(hpThreshold == 0 && mpThreshold > 0  && cpThreshold == 0){
                    activeChar.sendMessage("Auto Potion enabled with custom settings: HP (DISABLED), MP < " + mpThreshold + "%, CP (DISABLED).");
        		}else if(hpThreshold == 0 && mpThreshold == 0  && cpThreshold > 0){
                    activeChar.sendMessage("Auto Potion enabled with custom settings: HP (DISABLED), MP (DISABLED), CP < " + cpThreshold + "%.");
        		}else if(hpThreshold > 0 && mpThreshold == 0  && cpThreshold > 0){
                    activeChar.sendMessage("Auto Potion enabled with custom settings: HP < " + hpThreshold + "%, MP (DISABLED), CP < " + cpThreshold + "%.");
        		}else if(hpThreshold > 0 && mpThreshold == 0  && cpThreshold == 0){
                    activeChar.sendMessage("Auto Potion enabled with custom settings: HP < " + hpThreshold + "%, MP (DISABLED), CP (DISABLED).");
        		}else if(hpThreshold > 0 && mpThreshold > 0  && cpThreshold == 0){
                    activeChar.sendMessage("Auto Potion enabled with custom settings: HP < " + hpThreshold + "%, MP < " + mpThreshold + "%, CP (DISABLED).");
        		}else{
                    activeChar.sendMessage("Auto Potion enabled with custom settings: HP < " + hpThreshold + "%, MP < " + mpThreshold + "%, CP < " + cpThreshold + "%.");
        		}
        	}else if(command.equalsIgnoreCase("disableautopotion")){
        		activeChar.setAutoPotionConfig(hpThreshold, mpThreshold, cpThreshold, false); // Deshabilita auto-poción
                activeChar.sendMessage("Auto Potion has been disabled.");
        	}
        } else if(command.equalsIgnoreCase("disablehp") || command.equalsIgnoreCase("disablemp") || command.equalsIgnoreCase("disablecp")){
        	if(command.equalsIgnoreCase("disablehp")){
        		activeChar.setAutoPotionConfig(0, mpThreshold, cpThreshold, isEnable); // Desactivar auto-poción de HP
                activeChar.sendMessage("Auto Potion of HP has been disabled.");
        	} else if(command.equalsIgnoreCase("disablemp")){
        		activeChar.setAutoPotionConfig(hpThreshold, 0, cpThreshold, isEnable); // Desactivar auto-poción de MP
                activeChar.sendMessage("Auto Potion of MP has been disabled.");
        	} else if(command.equalsIgnoreCase("disablecp")){
        		activeChar.setAutoPotionConfig(hpThreshold, mpThreshold, 0, isEnable); // Desactivar auto-poción de CP
                activeChar.sendMessage("Auto Potion of CP has been disabled.");
        	}
        	String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/autopot/autopot.htm");
    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
    		npcHtmlMessage.setHtml(htmContent);
    		npcHtmlMessage.replace("%acc_autopot_status%", status);
    		if(activeChar.getAPConfiguredHp().equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_hp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_hp%", activeChar.getAPConfiguredHp()+"%");
    		if(activeChar.getAPConfiguredMp().equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_mp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_mp%", activeChar.getAPConfiguredMp()+"%");
    		if(activeChar.getAPConfiguredCp().equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_cp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_cp%", activeChar.getAPConfiguredCp()+"%");
    		activeChar.sendPacket(npcHtmlMessage);
        } else if(command.equalsIgnoreCase("sethp") || command.equalsIgnoreCase("setmp") || command.equalsIgnoreCase("setcp")){
        	if (params != null && !params.isEmpty()) {
        		try {
	        		int threshold = Integer.parseInt(params);
                	if(threshold > 90)
                		threshold = 90;
                	if(threshold < 10 && threshold != 0)
                		threshold = 10;
                	
                	if(command.equalsIgnoreCase("sethp")){
                		activeChar.setAutoPotionConfig(threshold, mpThreshold, cpThreshold, isEnable); // Set auto-poción de HP
                		if(threshold == 0){
                			activeChar.sendMessage("Auto Potion of HP has been disabled.");
                		}else{
                			activeChar.sendMessage("Auto Potion of HP has been set to " + threshold + "%.");
                		}
                	} else if(command.equalsIgnoreCase("setmp")){
                		activeChar.setAutoPotionConfig(hpThreshold, threshold, cpThreshold, isEnable); // Set auto-poción de HP
                		if(threshold == 0){
                			activeChar.sendMessage("Auto Potion of MP has been disabled.");
                		}else{
                			activeChar.sendMessage("Auto Potion of MP has been set to " + threshold + "%.");
                		}
                	} else if(command.equalsIgnoreCase("setcp")){
                		activeChar.setAutoPotionConfig(hpThreshold, mpThreshold, threshold, isEnable); // Set auto-poción de HP
                		if(threshold == 0){
                			activeChar.sendMessage("Auto Potion of CP has been disabled.");
                		}else{
                			activeChar.sendMessage("Auto Potion of CP has been set to " + threshold + "%.");
                		}
                	}
        		} catch (NumberFormatException e) {
                    activeChar.sendMessage("Invalid parameters. Must be a number.");
                }
        	}else{
                activeChar.sendMessage("Cannot set, empty value.");
        	}
        	String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/autopot/autopot.htm");
    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
    		npcHtmlMessage.setHtml(htmContent);
    		npcHtmlMessage.replace("%acc_autopot_status%", status);    		
    		if(activeChar.getAPConfiguredHp().equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_hp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_hp%", activeChar.getAPConfiguredHp()+"%");
    		if(activeChar.getAPConfiguredMp().equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_mp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_mp%", activeChar.getAPConfiguredMp()+"%");
    		if(activeChar.getAPConfiguredCp().equalsIgnoreCase("0"))
    			npcHtmlMessage.replace("%acc_autopot_cp%", "DISABLED");
    		else
    			npcHtmlMessage.replace("%acc_autopot_cp%", activeChar.getAPConfiguredCp()+"%");
    		activeChar.sendPacket(npcHtmlMessage);
        }
        
        return true;
    }

	/**
	 * 
	 * @see fidas.server.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}