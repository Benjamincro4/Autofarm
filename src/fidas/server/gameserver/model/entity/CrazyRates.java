package fidas.server.gameserver.model.entity;

import fidas.server.Config;
import fidas.server.gameserver.Announcements;

public class CrazyRates
{
	public static void DoubleRates()
	{
		Config.RATE_XP = Config.RateMultipler * Config.RATE_XP;
		Config.RATE_SP = Config.RateMultipler * Config.RATE_SP;
		Config.RATE_DROP_ITEMS = Config.RateMultipler * Config.RATE_DROP_ITEMS;
		Config.RATE_DROP_ITEMS_BY_RAID = Config.RateMultipler * Config.RATE_DROP_ITEMS_BY_RAID;
		Config.ENCHANT_CHANCE = Config.RateMultipler * Config.ENCHANT_CHANCE;
		Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL = Config.RateMultipler * Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL;
		Config.ENCHANT_CHANCE_ELEMENT_ENERGY = Config.RateMultipler * Config.ENCHANT_CHANCE_ELEMENT_ENERGY;
		Config.ENCHANT_CHANCE_ELEMENT_JEWEL = Config.RateMultipler * Config.ENCHANT_CHANCE_ELEMENT_JEWEL;
		Config.ENCHANT_CHANCE_ELEMENT_STONE = Config.RateMultipler * Config.ENCHANT_CHANCE_ELEMENT_STONE;
		Announcements.getInstance().announceToAll("O Evento Crazy Rates comecou voce tem " + Config.time_crazyrate + " minutos para se divertir com todas as rates multiplicadas por " + Config.RateMultipler);
	}

	public static void DivideRates()
	{
		Config.RATE_XP = Config.RATE_XP / Config.RateMultipler;
		Config.RATE_SP = Config.RATE_SP / Config.RateMultipler;
		Config.RATE_DROP_ITEMS = Config.RATE_DROP_ITEMS / Config.RateMultipler;
		Config.RATE_DROP_ITEMS_BY_RAID = Config.RATE_DROP_ITEMS_BY_RAID / Config.RateMultipler;
		Config.ENCHANT_CHANCE =  Config.ENCHANT_CHANCE /  Config.RateMultipler;
		Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL = Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL * Config.RateMultipler;
		Config.ENCHANT_CHANCE_ELEMENT_ENERGY = Config.ENCHANT_CHANCE_ELEMENT_ENERGY * Config.RateMultipler;
		Config.ENCHANT_CHANCE_ELEMENT_JEWEL = Config.ENCHANT_CHANCE_ELEMENT_JEWEL * Config.RateMultipler;
		Config.ENCHANT_CHANCE_ELEMENT_STONE = Config.ENCHANT_CHANCE_ELEMENT_STONE * Config.RateMultipler;
		Announcements.getInstance().announceToAll("O Evento Crazy Rates terminou, todas as rates voltaram ao normal!");
	}

	public static void EventManager()
	{
		DoubleRates();
		try
		{
			Thread.sleep(Config.time_crazyrate * 1000 * 60); // Tempo em Minutos
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		DivideRates();
	}
}