import sys
from fidas.server.gameserver.model.quest import State
from fidas.server.gameserver.model.quest import QuestState
from fidas.server.gameserver.model.quest import Quest
from fidas.server.gameserver.datatables import DoorTable
from fidas.server.gameserver.datatables import SkillTable
from fidas.server.gameserver.model.quest.jython import QuestJython as JQuest
from fidas.server.gameserver import Announcements
from fidas.server import L2DatabaseFactory
from fidas.server.gameserver.ai import CtrlIntention
from fidas.server.util import Rnd
from java.lang import System
from fidas.server.gameserver.model import L2World
from fidas.server.gameserver.model.actor.instance import L2DoorInstance
from fidas.server.gameserver.datatables import DoorTable;

qn = "LastHero"
# =======================================#
#               Settings                 #
# =======================================#
# Name of Event
Event_name = "LastHero" 

#The city in which you want to put the registration
StartLocation = "Giran" 

# ID-number recording NPC
Reg_Npc = 65535

#Coordinates, which will appear registering npc
StartNpcCoordinat = [82698,148638,-3468] 

# Items needed to participate in the Event.
# Entry format [itemId1, itemId2 ,....]
Price = [6673]

# How many specified items required for participation in the Event. If you want to participate was free - Make your 0.
# Entry format [itemId1_count, itemId2_count ,....]
Price_count = [3]

#The minimum level at which the player can participate in the Event
Min_level = 76

# How long will run after you restart the Event Server. (In seconds)
Time_to_start_after_restart = 1800

# How long after teleporting to the Event will start for the battle. (In seconds)
Time_to_wait_battle = 40

# Duration opening event. (Specify in minutes)
EVENT_INTERVAL = 10

# Time between Event. (Specify in minutes)
Time_to_next_start = 60

# Time to register. (Specify in minutes)
Time_for_regestration = 10

#How often announcement about registration Event. (In seconds)
Announce_reg_delay = 300

# Minimum number of players (at least 2).
Min_participate_count = 2

# Maximum number of players.
Max_participate_count = 100

# List of awards for winning the opening event. Is issued to each player.
# Entry format: [[itemId1, count1, chance1], [itemId2, count2, chanceN ],...]
Rewards = [[6673,50,100],[57,10000000,100]] 

# The coordinates of the point where the players will teleport.
Teleport_coordinat = [149438, 46785, -3413]

# The doors will be closed on periood passage opening event.
# Entry format: [Door_id1, Door_id2 ,...]
Doors = [24190002,24190003]


# ================Don't change this================ #
lastPlayers = []
lastX = []
lastY = []
lastZ = []
closed = 1
Players = []
Deadplayers = []
annom = 1
f = 0
# ========================================== #

class Quest (JQuest) :
 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def init_LoadGlobalData(self) :
   self.startQuestTimer("open_reg", Time_to_start_after_restart *1000, None, None)
   return

 def onTalk (Self,npc,player):
  global Players,closed
  st = player.getQuestState(qn)
  npcId = npc.getNpcId()
  if npcId == Reg_Npc:
   if closed<>1:
    if not player.isInOlympiadMode() :
     if player.getLevel() >= Min_level:
      if player.getName() not in Players:
       if len(Players) <= Max_participate_count :
        if Price_count[0]<>0:   
         if st.getQuestItemsCount(Price[0])>Price_count[0]:
           st.takeItems(Price[0],Price_count[0])
           Players.append(player.getName())
           return "reg.htm"
         else:
           st.exitQuest(1)
           return "noPrice.htm"
        else:
            Players.append(player.getName())
            return "reg.htm"
       else:         
         return "max.htm"
      else:
       return "yje.htm"
     else:
      return "lvl.htm"
    else:
     return "You register in olympiad games now"
   else:
    return "noreg.htm"
  return

 def onAdvEvent (self,event,npc,player):
   global Deadplayers,Players,annom,closed,Doors,lastPlayers,lastX,lastY,lastZ,f,n
   if event == "open_reg" :
    closed = 0
    annom = 1
    lastPlayers = []
    Players = []
    Deadplayers = []
    lastX = []
    lastY = []
    lastZ = []
    npc = self.addSpawn(Reg_Npc,StartNpcCoordinat[0],StartNpcCoordinat[1],StartNpcCoordinat[2],30000,False,0)
    self.startQuestTimer("wait_battle", Time_for_regestration*60000, npc, None)
    self.startQuestTimer("announce", Announce_reg_delay*1000, None, None)
    Announcements.getInstance().announceToAll("Opened registration for "+str(Event_name)+" event! You can register in "+str(StartLocation)+".")
   if event == "start_event":
    if len(Players)< Min_participate_count :
      closed=1
      Announcements.getInstance().announceToAll("Event "+str(Event_name)+" was canceled due lack of participation.")
      self.startQuestTimer("set_winner", 1000, None, None)
      self.startQuestTimer("open_reg", Time_to_next_start*60000, None, None)
    else:
      closed=1
      Announcements.getInstance().announceToAll("Event "+str(Event_name)+" has started!")
      self.startQuestTimer("konec", EVENT_INTERVAL*60000, None, None)
      f=0
      for nm in Players :
       i=L2World.getInstance().getPlayer(nm)
       if i<>None:
        if i.isOnline() :
            i.getAppearance().setVisible()
            i.broadcastStatusUpdate()
        i.broadcastUserInfo()
      while len(Players)>1 :
       for nm in Players :
        i=L2World.getInstance().getPlayer(nm)
        if i<>None:
         if i.isDead():
          i.reviveAnswer(0)
          Deadplayers.append(i.getName())
          Players.remove(i.getName())
      self.startQuestTimer("set_winner", 1000, None, None)
   if event == "announce" and closed==0 and (Time_for_regestration*60 - Announce_reg_delay * annom)>0: 
     Announcements.getInstance().announceToAll(str(Time_for_regestration*60 - Announce_reg_delay * annom ) + " seconds until event "+str(Event_name)+" will start! You can register in "+str(StartLocation)+".")
     annom=annom+1
     self.startQuestTimer("announce", Announce_reg_delay*1000, None, None)
   if event == "set_winner" :
     if len(Players) > 0 and len(Players + Deadplayers) >= Min_participate_count:
         winner=L2World.getInstance().getPlayer(Players[0])
         Deadplayers.append(Players[0])
         if winner.isDead():
             Announcements.getInstance().announceToAll("Event "+str(Event_name)+" has ended. All players is dead. Nobody Win")
         else :
             f=1 
             Announcements.getInstance().announceToAll("Event "+str(Event_name)+" has ended. "+str(Players[0])+" win!")
         for nm in Deadplayers :
             i=L2World.getInstance().getPlayer(nm)
             if i<>None and i.isOnline():
                 if i.isDead():
                     i.doRevive()
                 i.setCurrentCp(i.getMaxCp())
                 i.setCurrentHp(i.getMaxHp())
                 i.setCurrentMp(i.getMaxMp())
                 i.stopAllEffects()
                 i.broadcastStatusUpdate()
                 i.broadcastUserInfo()
         if len(Deadplayers)>0:
             n = 0
             for nm in lastPlayers :
                 i=L2World.getInstance().getPlayer(nm)
                 if i <> None:
                     i.teleToLocation(lastX[n],lastY[n],lastZ[n])
                     n = n + 1
         if winner<>None:
           if winner.isOnline() :
               L2World.getInstance().getPlayer(Players[0]).setHero(True)
     Announcements.getInstance().announceToAll("Next time registration opend at "+str(Time_to_next_start)+" minute(s)")
     for d in Doors:
         door = DoorTable.getInstance().getDoor(d)
         door.openMe()
     lastPlayers = []
     Players = []
     Deadplayers = []
     lastX = []
     lastY = []
     lastZ = []
     self.startQuestTimer("open_reg", Time_to_next_start*60000, None, None)
   if event == "exit" :
     if player.getName() in Players:
       Players.remove(player.getName())
       return "exit.htm"
     else:
       return "default.htm"

   if event == "konec" :
    if f==0:
     for nm in Players :
      i=L2World.getInstance().getPlayer(nm)
      if i<>None:
       if i.isOnline() :
        i.teleToLocation(82698,148638,-3468)
        i.broadcastStatusUpdate()
        i.broadcastUserInfo()
     Announcements.getInstance().announceToAll("Event "+str(Event_name)+" was ended in drawn.")
     self.startQuestTimer("open_reg", Time_to_next_start*60000, None, None)

   if event == "wait_battle":
    npc.deleteMe()
    if len(Players) >= Min_participate_count:
      for nm in Players:
        i=L2World.getInstance().getPlayer(nm)
        if i<>None:
          if not i.isOnline() or i.isInOlympiadMode() or i.isInJail():
            Players.remove(nm)
        else:
            Players.remove(nm)
      for nm in Players:
        i=L2World.getInstance().getPlayer(nm)
        if i<>None:
          if i.isOnline() :
            if i.isDead():
              i.doRevive()
            i.setCurrentCp(i.getMaxCp())
            i.setCurrentHp(i.getMaxHp())
            i.setCurrentMp(i.getMaxMp())
            i.stopAllEffects()
            i.getAppearance().setInvisible();
            i.broadcastStatusUpdate()
            i.broadcastUserInfo()
            lastPlayers.append(nm)
            lastX.append(i.getX())
            lastY.append(i.getY())
            lastZ.append(i.getZ())
            i.teleToLocation(Teleport_coordinat[0],Teleport_coordinat[1],Teleport_coordinat[2])
      for d in Doors:
        door = DoorTable.getInstance().getDoor(d)
        door.closeMe()
      Announcements.getInstance().announceToAll("Event "+str(Event_name)+": Registration close. You have "+str(Time_to_wait_battle)+" seconds for buffs before battle start")
      self.startQuestTimer("start_event", Time_to_wait_battle*1000, None, None)
    else :
        self.startQuestTimer("start_event", 1000, None, None)

QUEST = Quest(-1, qn, "events")

QUEST.addStartNpc(int(Reg_Npc))
QUEST.addTalkId(int(Reg_Npc))