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
package vehicles.SoAController;

import vehicles.AirShipController;

import fidas.server.gameserver.model.Location;
import fidas.server.gameserver.model.VehiclePathPoint;

public class SoAController extends AirShipController
{
	private static final int DOCK_ZONE = 50604;
	private static final int LOCATION = 103;
	private static final int CONTROLLER_ID = 32779;
	
	private static final VehiclePathPoint[] ARRIVAL =
	{
		new VehiclePathPoint(-174946, 155306, 3105, 280, 2000)
	};
	
	private static final VehiclePathPoint[] DEPART =
	{
		new VehiclePathPoint(-175063, 155726, 4105, 280, 2000),
		new VehiclePathPoint(-179546, 161425, 5105, 280, 2000)
	};
	
	private static final VehiclePathPoint[][] TELEPORTS =
	{
		{
			new VehiclePathPoint(-175063, 155726, 4105, 280, 2000),
			new VehiclePathPoint(-179546, 161425, 5105, 280, 2000),
			new VehiclePathPoint(-179438, 162776, 5129, 0, 0)
		},
		{
			new VehiclePathPoint(-175063, 155726, 4105, 280, 2000),
			new VehiclePathPoint(-179546, 161425, 5105, 280, 2000),
			new VehiclePathPoint(-195357, 233430, 2500, 0, 0)
		}
	};
	
	private static final int[] FUEL =
	{
		0, 150
	};
	
	public SoAController(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(CONTROLLER_ID);
		addFirstTalkId(CONTROLLER_ID);
		addTalkId(CONTROLLER_ID);
		
		_dockZone = DOCK_ZONE;
		addEnterZoneId(DOCK_ZONE);
		addExitZoneId(DOCK_ZONE);
		
		_shipSpawnX = -178871;
		_shipSpawnY = 156664;
		_shipSpawnZ = 6105;
		
		_oustLoc = new Location(-175689, 154160, 2712);
		
		_locationId = LOCATION;
		_arrivalPath = ARRIVAL;
		_departPath = DEPART;
		_teleportsTable = TELEPORTS;
		_fuelTable = FUEL;
		
		_movieId = 1004;
		
		validityCheck();
	}
	
	public static void main(String[] args)
	{
		new SoAController(-1, SoAController.class.getSimpleName(), "vehicles");
	}
}