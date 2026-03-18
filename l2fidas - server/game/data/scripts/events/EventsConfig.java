package events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.model.quest.Quest;

public class EventsConfig extends Quest
{
	private static final String qn = "EventsConfig";
	
	public static final boolean EVENTS_ENABLED = false;
	
	/**
	 * Holly Cow Event date
	 * @author Deyoun
	 * @category Custom Event polegaj�cy na przywo�aniu za kupione scrolle krowy i karmienie jej lub dojenie. Z dojenia otrzymujemy mleko daj�ce buff, je�eli dojenie si� nie uda poka�e si� byk. Byki atakuj� oraz dropi� dodatkowe rzeczy Marzec 1-31
	 */
	public static boolean HC_STARTED = false;
	/**
	 * April Fool's Day date
	 * @author Deyoun
	 * @category Custom Event polega na zbieraniu paczek. Po zebraniu odpowiedniej ilo�ci wymieniamy je na dobr� lub z�� nagrod�. Kwiecie� 1-6
	 **/
	public static boolean AP_STARTED = false;
	/**
	 * Ninja Adventures Event date
	 * @author Deyoun
	 * @category Custom Polega na przy��czeniu si� do losowej wioski i walki dla niej. Po przy��czeniu si� do wybranej wioski nale�y zabija� wybrane potwory aby pokaza� si� kot ninja, kt�ry jest celem. Za zabicie kot�w ninja s� punkty dla wojownik�w Ninja oraz dla wiosek. Za zsumowane odpowiednio
	 *           punkty mo�na wybra� nagrody z listy. Kwiecie� 7-30
	 **/
	public static boolean NA_STARTED = false;
	/**
	 * Super Star Event date
	 * @author Deyoun
	 * @category Custom Polega na zbieraniu gwiazd z instancji oraz wymianie gwiazd na nagrody u specjalnego npc. Maj 1-31
	 */
	public static boolean SS_STARTED = false;
	/**
	 * Squish and Squash Event date
	 * @author Deyoun
	 * @category Retail Event polega na podlewaniu oraz zabijaniu specjaln� broni� du�ych owoc�w. Po zabiciu tych owoc�w otrzymujemy specjalne nagrody. Czerwiec 1-30
	 **/
	public static boolean SQUASH_STARTED = false;
	public static boolean SQUASH_DROP_ACTIVE = false;
	/**
	 * L2Day Event date
	 * @author Deyoun
	 * @category Retail Event polega na zbieraniu liter i uk�adaniu z nich s��w pokazanych przez NPC. Za u�o�one s�owo dostajemy nagrody wypisane u NPC. Lipiec 1-31
	 **/
	public static boolean L2DAY_STARTED = false;
	public static boolean L2DAY_ACTIVE_DROP = false;
	/**
	 * Heavy Medal Event
	 * @author Deyoun
	 * @category Retail Event polega na zbieraniu dw�ch typ�w medali. Jedne z tych medali s� u�ywane w grze kt�ra pozwala na wybranie lepszych nagr�d. Nagrody mo�na otrzyma� za medale. Sierpie� 1-31
	 **/
	public static boolean HM_STARTED = false;
	public static boolean HM_ACTIVE_DROP = false;
	/**
	 * SchoolDays date
	 * @author Deyoun
	 * @category Custom Event polega na uratowaniu wiedzy �wiata L2. Nale�y zbiera� ksi��ki do nauki skilli swojej klasy. Za zebrane ksi��ki mo�emy u NPC otrzyma� nagrody. Wrzesie� 1-30
	 **/
	public static boolean SD_STARTED = false;
	public static boolean SD_ACTIVE_DROP = false;
	/**
	 * Trick or Transmutation Event date
	 * @author Deyoun
	 * @category Retail Event polega na wydropieniu kluczy, za pomoc� kt�rych mo�na otworzy� skrzyni�. Ze skrzyni otrzymujemy materia�y oraz recepty potrzebne nam do z�o�enia kamienia filozof�w. Gdy otworzymy z�o�ony kamie� otrzymamy losow� nagrod� wypisan� u npc. Pa�dziernik 1-31
	 **/
	public static boolean TOT_STARTED = false;
	public static boolean TOT_ACTIVE_DROP = false;
	/**
	 * Hallowed You Event date
	 * @author Deyoun
	 * @category Custom Event polega na zabijaniu duch�w kt�re wychodz� z cia� potwor�w �yj�cych na cmentarzach. Za zabicie ducha otrzymujemy nagrod� w postaci, lizaka. Lizaki mo�na u�y�, w celu regeneracji Vitality lub wymieni� na specjalne nagrody. Listopad 1-30
	 **/
	public static boolean HY_STARTED = false;
	public static boolean HY_ACTIVE_DROP = false;
	/**
	 * Christmas Is Here Event date
	 * @author Deyoun
	 * @category Custom Event polega na zbieraniu zagubionych skarpet. Skarpety mo�na wymieni� na nagrody �wi�teczne u miko�aja. Grudzie� 1-31
	 **/
	public static boolean CH_STARTED = false;
	public static boolean CH_ACTIVE_DROP = false;
	
	/**
	 * Get Event data from sql
	 * @param eventName 
	 * @return 
	 */
	private static boolean getEvent(String eventName)
	{
		boolean state = false;
		int eventMonth = 0;
		int eventStartDay = 0;
		int eventEndDay = 0;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM events WHERE name=?");
			statement.setString(1, eventName);
			ResultSet result = statement.executeQuery();
			if (result.next())
			{
				eventMonth = result.getInt("month");
				eventStartDay = result.getInt("start_day");
				eventEndDay = result.getInt("end_day");
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not read events data:" + e);
		}
		int Month = Integer.valueOf(Calendar.getInstance().get(Calendar.MONTH)) + 1;
		int Day = Integer.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		try
		{
			if ((eventMonth == Month) && (Day >= eventStartDay) && (Day <= eventEndDay))
			{
				return true;
			}
		}
		catch (Exception e)
		{
		}
		return state;
	}
	
	// Events Config is created by L2m Project
	// How to change a event
	// 1. Change Boolean value of event
	// 2. Use command //quest_reload EventsConfig
	// 3. Use command //quest_reload EventName
	// 4. Now you can use event.
	
	/**
	 * Character Birthday
	 */
	public static final int ADVENTURER_HAT = 10250;
	public static final int BIRTHDAY_CAKE_SKILL = 5950;
	/**
	 * Dimensional Merchant
	 */
	public static final boolean DIM_MERCHANT_STARTED = true;
	public static final int DIM_VITAMIN_LEVEL = 77;
	public static final int DIM_COIN = 13419;
	/**
	 * Heavy Medal
	 */
	public static final int HM_MEDAL = 6392;
	public static final int HM_GLITTERING_MEDAL = 6393;
	/**
	 * Holly Cow
	 */
	public static final int HC_ADENA = 57;
	public static final int HC_MILK = 14739;
	public static final int HC_MILK_COW_SCROLL = 14724;
	public static final int HC_HEAD_MILK_COW_SCROLL = 14725;
	public static final int HC_GLOOM_MILK_COW_SCROLL = 14726;
	public static final int HC_GLOOM_HEAD_MILK_COW_SCROLL = 14727;
	/**
	 * L2 Day
	 */
	public static final int L2DAY_A = 3875;
	public static final int L2DAY_C = 3876;
	public static final int L2DAY_E = 3877;
	public static final int L2DAY_F = 3878;
	public static final int L2DAY_G = 3879;
	public static final int L2DAY_H = 3880;
	public static final int L2DAY_I = 3881;
	public static final int L2DAY_L = 3882;
	public static final int L2DAY_N = 3883;
	public static final int L2DAY_O = 3884;
	public static final int L2DAY_R = 3885;
	public static final int L2DAY_S = 3886;
	public static final int L2DAY_T = 3887;
	public static final int L2DAY_II = 3888;
	public static final int L2DAY_Y = 13417;
	public static final int L2DAY_5 = 13418;
	public static final int L2DAY_GUIDANCE = 3926;
	public static final int L2DAY_DEATH_WHISPER = 3927;
	public static final int L2DAY_FOCUS = 3928;
	public static final int L2DAY_GREATER_ACUMEN = 3929;
	public static final int L2DAY_HASTE = 3930;
	public static final int L2DAY_AGILITY = 3931;
	public static final int L2DAY_MYSTIC_EMPOWER = 3932;
	public static final int L2DAY_MIGHT = 3933;
	public static final int L2DAY_WINDWALK = 3934;
	public static final int L2DAY_SHIELD = 3935;
	public static final int L2DAY_BSOE = 1538;
	public static final int L2DAY_BSOR = 3936;
	public static final int L2DAY_MANA_REGENERATION = 4218;
	public static final int L2DAY_ADENA = 57;
	public static final int L2DAY_ANCIENT_ADENA = 5575;
	public static final int L2DAY_MAGE_COCKTAIL = 20394;
	public static final int L2DAY_FIGHTER_COCKTAIL = 20393;
	/**
	 * Ninja Adventures
	 */
	public static final int NA_HAIRBAND = 7060;
	public static final int NA_ADENA = 57;
	public static final int NA_ANCIENT_ADENA = 5575;
	/**
	 * Trick or Transmutation
	 */
	public static final int TOT_KEY = 9205;
	public static final int TOT_RED_STONE = 9162;
	public static final int TOT_BLUE_STONE = 9163;
	public static final int TOT_ORANGE_STONE = 9164;
	public static final int TOT_BLACK_STONE = 9165;
	public static final int TOT_WHITE_STONE = 9166;
	public static final int TOT_GREEN_STONE = 9167;
	public static final int TOT_STONE_ORE = 9168;
	public static final int TOT_STONE_FORMULA = 9169;
	public static final int TOT_MAGIC_REAGENTS = 9170;
	/**
	 * HallowedYou
	 */
	public static final int HALLOWEEN_CANDY = 20706;
	/**
	 * ChristmasIsHere
	 */
	public static final int CH_CHRISTMAS_SOCK = 14612;
	/**
	 * SchoolDays
	 */
	
	/**
	 * Super Star
	 */
	public static final int SS_BACKUP_STONE_WEP_D = 12362;
	public static final int SS_BACKUP_STONE_WEP_C = 12363;
	public static final int SS_BACKUP_STONE_WEP_B = 12364;
	public static final int SS_BACKUP_STONE_WEP_A = 12365;
	public static final int SS_BACKUP_STONE_WEP_S = 12366;
	public static final int SS_BACKUP_STONE_ARM_D = 12367;
	public static final int SS_BACKUP_STONE_ARM_C = 12368;
	public static final int SS_BACKUP_STONE_ARM_B = 12369;
	public static final int SS_BACKUP_STONE_ARM_A = 12370;
	public static final int SS_BACKUP_STONE_ARM_S = 12371;
	
	/**
	 * Text - this event is disabled
	 */
	public static final String EVENT_DISABLED = "<html><title>Event Message</title><body><center><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>" + "<br><br><br><br><br><br><b><font color=LEVEL>THIS EVENT<br>IS<br>NOW<br>DISABLED</font></b><br><br><br><br><br><br><br><br><br><br><br><br>" + "<br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left></center></body></html>";
	
	public static void setValue(boolean value, boolean status)
	{
		value = status;
	}
	
	public EventsConfig(int questId, String name, String descr)
	{
		super(questId, name, descr);
		if (EVENTS_ENABLED)
		{
			HC_STARTED = getEvent("HollyCow");
			AP_STARTED = getEvent("AprilFools");
			NA_STARTED = getEvent("NinjaAdventures");
			SS_STARTED = getEvent("SuperStar");
			L2DAY_STARTED = getEvent("L2Day");
			L2DAY_ACTIVE_DROP = getEvent("L2Day");
			SD_STARTED = getEvent("SchoolDays");
			SD_ACTIVE_DROP = getEvent("SchoolDays");
			TOT_STARTED = getEvent("TrickorTransmutation");
			TOT_ACTIVE_DROP = getEvent("TrickorTransmutation");
			HY_STARTED = getEvent("HallowedYou");
			HY_ACTIVE_DROP = getEvent("HallowedYou");
			CH_STARTED = getEvent("ChristmasIsHere");
			CH_ACTIVE_DROP = getEvent("ChristmasIsHere");
		}
	}
	
	public static void main(String[] args)
	{
		new EventsConfig(-1, qn, "events");
		
		if (EVENTS_ENABLED)
		{
			_log.info("===============================");
			_log.info("Events System: Configuracao dos events:");
			_log.info("HollyCow value " + String.valueOf(HC_STARTED));
			_log.info("L2Day value " + String.valueOf(L2DAY_STARTED));
			_log.info("NinjaAdventures value " + String.valueOf(NA_STARTED));
			_log.info("SuperStar value " + String.valueOf(SS_STARTED));
			_log.info("TrickorTransmutation value " + String.valueOf(TOT_STARTED));
			_log.info("HallowedYou value " + String.valueOf(HY_STARTED));
			_log.info("ChristmasIsHere value " + String.valueOf(CH_STARTED));
			_log.info("AprillFools value " + String.valueOf(AP_STARTED));
			_log.info("SchoolDays value " + String.valueOf(SD_STARTED));
			_log.info("===============================");
		}		
	}
}