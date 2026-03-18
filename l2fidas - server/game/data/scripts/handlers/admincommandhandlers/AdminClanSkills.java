package handlers.admincommandhandlers;
 
import fidas.server.gameserver.datatables.ClanTable;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.handler.IAdminCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.skills.L2Skill;
  
public class AdminClanSkills implements IAdminCommandHandler
{
       
        public static final String[] ADMIN_COMMANDS = { "admin_clanskills" };
 
        @Override
		public boolean useAdminCommand (String command,L2PcInstance activeChar)
        {
                if (command.startsWith("admin_clanskills"))
                {
                        L2PcInstance target = (L2PcInstance) activeChar.getTarget();
                        ClanTable.getInstance().getClan(target.getClanId());
                        L2Skill skilll = SkillTable.getInstance().getInfo(370, 3);
                        L2Skill skill2 = SkillTable.getInstance().getInfo(373, 3);
                        L2Skill skill3 = SkillTable.getInstance().getInfo(379, 3);
                        L2Skill skill4 = SkillTable.getInstance().getInfo(391, 1);
                        L2Skill skill5 = SkillTable.getInstance().getInfo(371, 3);
                        L2Skill skill6 = SkillTable.getInstance().getInfo(374, 3);
                        L2Skill skill7 = SkillTable.getInstance().getInfo(379, 3);
                        L2Skill skill8 = SkillTable.getInstance().getInfo(376, 3);
                        L2Skill skill9 = SkillTable.getInstance().getInfo(377, 3);
                        L2Skill skill10 = SkillTable.getInstance().getInfo(383, 3);
                        L2Skill skill11 = SkillTable.getInstance().getInfo(380, 3);
                        L2Skill skill12 = SkillTable.getInstance().getInfo(382, 3);
                        L2Skill skill13 = SkillTable.getInstance().getInfo(384, 3);
                        L2Skill skill14 = SkillTable.getInstance().getInfo(385, 3);
                        L2Skill skill15 = SkillTable.getInstance().getInfo(386, 3);
                        L2Skill skill16 = SkillTable.getInstance().getInfo(387, 3);
                        L2Skill skill17 = SkillTable.getInstance().getInfo(388, 3);
                        L2Skill skill18 = SkillTable.getInstance().getInfo(390, 3);
                        L2Skill skill19 = SkillTable.getInstance().getInfo(372, 3);
                        L2Skill skill20 = SkillTable.getInstance().getInfo(375, 3);
                        L2Skill skill21 = SkillTable.getInstance().getInfo(378, 3);
                        L2Skill skill22 = SkillTable.getInstance().getInfo(381, 3);
                        L2Skill skill23 = SkillTable.getInstance().getInfo(389, 3);
                        target.getClan().addSkill(skilll);
                        target.getClan().addSkill(skill2);
                        target.getClan().addSkill(skill3);
                        target.getClan().addSkill(skill4);
                        target.getClan().addSkill(skill5);
                        target.getClan().addSkill(skill6);
                        target.getClan().addSkill(skill7);
                        target.getClan().addSkill(skill8);
                        target.getClan().addSkill(skill9);
                        target.getClan().addSkill(skill10);
                        target.getClan().addSkill(skill11);
                        target.getClan().addSkill(skill12);
                        target.getClan().addSkill(skill13);
                        target.getClan().addSkill(skill14);
                        target.getClan().addSkill(skill15);
                        target.getClan().addSkill(skill16);
                        target.getClan().addSkill(skill17);
                        target.getClan().addSkill(skill18);
                        target.getClan().addSkill(skill19);
                        target.getClan().addSkill(skill20);
                        target.getClan().addSkill(skill21);
                        target.getClan().addSkill(skill22);
                        target.getClan().addSkill(skill23);
                }
                 
                   else
                	if (!activeChar.isClanLeader())
                		           {
                		                activeChar.sendMessage("What are you doing? He's not a clan leader.Action  Failed!");
                		                return false;
                		            }

                else
                {
                        activeChar.sendMessage("Clan not found");
                }
                return true;
        }
                @Override
				public String[] getAdminCommandList()
                {
                        return ADMIN_COMMANDS;
                }
        }