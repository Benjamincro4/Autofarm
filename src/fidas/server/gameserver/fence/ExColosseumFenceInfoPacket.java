package fidas.server.gameserver.fence;

import fidas.server.gameserver.network.serverpackets.L2GameServerPacket;

public class ExColosseumFenceInfoPacket extends L2GameServerPacket
{
	private final int _type;
	private final L2FenceInstance _activeChar;
	private final int _width;
	private final int _height;
	
	public ExColosseumFenceInfoPacket(L2FenceInstance activeChar)
	{
		_activeChar = activeChar;
		_type = activeChar.getType();
		_width = activeChar.getWidth();
		_height = activeChar.getHeight();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x03);
		writeD(_activeChar.getObjectId());
		writeD(_type);
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_width);
		writeD(_height);
	}
}