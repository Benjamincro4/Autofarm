/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package fidas.server.util;

/**
 * Hold number of milliseconds for wide-using time periods
 * @author GKR
 */

public enum TimeConstant
{
	NONE(-1L, "", ""),
	SECOND(1000L, "SECOND", "s"),
	MINUTE(60000L, "MINUTE", "m"),
	HOUR(3600000L, "HOUR", "h"),
	DAY(86400000L, "DAY", "d"),
	WEEK(604800000L, "WEEK", "w"),
	MONTH(2592000000L, "MONTH", "M");

	/** Count of milliseconds */
	private final long _millis;
	/** Mnemonic name of period */
	private final String _name;
	/** Short name of period */
	private final String _shortName;

	private TimeConstant(long millis, String name, String shortName)
	{
		_millis = millis;
		_name = name;
		_shortName = shortName;
	}
	
 /**
  * @return number of millisecond in time period 
  */
	public long getTimeInMillis()
	{
		return _millis;
	}

 /**
  * @return mnemonic name of time period 
  */
	public String getName()
	{
		return _name;
	}

 /**
  * @return short name of time period 
  */
	public String getShortName()
	{
		return _shortName;
	}
} 


