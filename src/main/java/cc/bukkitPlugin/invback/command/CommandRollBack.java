package cc.bukkitPlugin.invback.command;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.invback.InvBack;

public class CommandRollBack extends TACommandBase<InvBack,CommandExc>{

    public CommandRollBack(CommandExc pExector){
        super(pExector,1);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);
        
        if(pArgs.length>1)
            return errorArgsNumber(pSender,pArgs.length);

        Player restoreTarget=null;
        if(pArgs.length==1){
            restoreTarget=Bukkit.getPlayer(pArgs[0]);
            if(restoreTarget==null)
                return send(pSender,C("MsgPlayerNotOnlineOrExist",new String[]{"player"},pArgs[0]));
        }else{
            if(pSender instanceof Player)
                restoreTarget=(Player)pSender;
            else return send(pSender,C("MsgConsoleShouldInputPlayer"));
        }
        this.mPlugin.getDataManager().loadFromMemoryMap(pSender,restoreTarget,true);
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> helps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            helps.add(this.constructCmdUsage()+" ["+C("WordPlayer")+"]");
            helps.add(this.constructCmdDesc());
        }
        return helps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        return BukkitUtil.getOnlinePlayersName();
    }
}
