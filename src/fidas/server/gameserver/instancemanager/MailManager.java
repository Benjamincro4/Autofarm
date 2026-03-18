package fidas.server.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.idfactory.IdFactory;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Message;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.ExNoticePostArrived;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.util.L2FastMap;

public class MailManager
{
	private static Logger _log = Logger.getLogger(MailManager.class.getName());
	
	private final Map<Integer, Message> _messages = new L2FastMap<>(true);
	
	protected MailManager()
	{
		load();
	}
	
	private void load()
	{
		int count = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement ps = con.createStatement();
			ResultSet rs = ps.executeQuery("SELECT * FROM messages ORDER BY expiration"))
		{
			while (rs.next())
			{
				
				Message msg = new Message(rs);
				
				int msgId = msg.getId();
				_messages.put(msgId, msg);
				
				count++;
				
				long expiration = msg.getExpiration();
				
				if (expiration < System.currentTimeMillis())
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), 10000);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), expiration - System.currentTimeMillis());
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error loading from database:" + e.getMessage(), e);
		}
		_log.info(getClass().getSimpleName() + ": Carregado " + count + " menssagens.");
	}
	
	public final Message getMessage(int msgId)
	{
		return _messages.get(msgId);
	}
	
	public final Collection<Message> getMessages()
	{
		return _messages.values();
	}
	
	public final boolean hasUnreadPost(L2PcInstance player)
	{
		final int objectId = player.getObjectId();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && msg.isUnread())
			{
				return true;
			}
		}
		return false;
	}
	
	public final int getInboxSize(int objectId)
	{
		int size = 0;
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver())
			{
				size++;
			}
		}
		return size;
	}
	
	public final int getOutboxSize(int objectId)
	{
		int size = 0;
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getSenderId() == objectId) && !msg.isDeletedBySender())
			{
				size++;
			}
		}
		return size;
	}
	
	public final List<Message> getInbox(int objectId)
	{
		List<Message> inbox = new FastList<>();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver())
			{
				inbox.add(msg);
			}
		}
		return inbox;
	}
	
	public final List<Message> getOutbox(int objectId)
	{
		List<Message> outbox = new FastList<>();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getSenderId() == objectId) && !msg.isDeletedBySender())
			{
				outbox.add(msg);
			}
		}
		return outbox;
	}
	
	public void sendMessage(Message msg)
	{
		_messages.put(msg.getId(), msg);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = Message.getStatement(msg, con))
		{
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error saving message:" + e.getMessage(), e);
		}
		
		final L2PcInstance receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
		if (receiver != null)
		{
			receiver.sendPacket(ExNoticePostArrived.valueOf(true));
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msg.getId()), msg.getExpiration() - System.currentTimeMillis());
	}
	
	private class MessageDeletionTask implements Runnable
	{
		private final Logger _log = Logger.getLogger(MessageDeletionTask.class.getName());
		
		final int _msgId;
		
		public MessageDeletionTask(int msgId)
		{
			_msgId = msgId;
		}
		
		@Override
		public void run()
		{
			final Message msg = getMessage(_msgId);
			if (msg == null)
			{
				return;
			}
			
			if (msg.hasAttachments())
			{
				try
				{
					final L2PcInstance sender = L2World.getInstance().getPlayer(msg.getSenderId());
					if (sender != null)
					{
						msg.getAttachments().returnToWh(sender.getWarehouse());
						sender.sendPacket(SystemMessageId.MAIL_RETURNED);
					}
					else
					{
						msg.getAttachments().returnToWh(null);
					}
					
					msg.getAttachments().deleteMe();
					msg.removeAttachments();
					
					final L2PcInstance receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
					if (receiver != null)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.MAIL_RETURNED);
						// sm.addString(msg.getReceiverName());
						receiver.sendPacket(sm);
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Error returning items:" + e.getMessage(), e);
				}
			}
			deleteMessageInDb(msg.getId());
		}
	}
	
	public final void markAsReadInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE messages SET isUnread = 'false' WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error marking as read message:" + e.getMessage(), e);
		}
	}
	
	public final void markAsDeletedBySenderInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE messages SET isDeletedBySender = 'true' WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error marking as deleted by sender message:" + e.getMessage(), e);
		}
	}
	
	public final void markAsDeletedByReceiverInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE messages SET isDeletedByReceiver = 'true' WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error marking as deleted by receiver message:" + e.getMessage(), e);
		}
	}
	
	public final void removeAttachmentsInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE messages SET hasAttachments = 'false' WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error removing attachments in message:" + e.getMessage(), e);
		}
	}
	
	public final void deleteMessageInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM messages WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error deleting message:" + e.getMessage(), e);
		}
		
		_messages.remove(msgId);
		IdFactory.getInstance().releaseId(msgId);
	}
	
	public static MailManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MailManager _instance = new MailManager();
	}
}