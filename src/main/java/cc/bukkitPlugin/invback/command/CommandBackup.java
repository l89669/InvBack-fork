package cc.bukkitPlugin.invback.command;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.util.CCBukkit;
import cc.bukkitPlugin.util.plugin.command.TACommandBase;

public class CommandBackup extends TACommandBase<InvBack,CommandExc>{

    public CommandBackup(CommandExc pExector){
        super(pExector,1);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length>1)
            return errorArgsNumber(pSender,pArgs.length);

        Player backupTarget=null;
        if(pArgs.length==1){
            backupTarget=Bukkit.getPlayer(pArgs[0]);
            if(backupTarget==null){
                return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[0]));
            }
        }else{
            if(pSender instanceof Player){
                backupTarget=(Player)pSender;
            }else{
                return send(pSender,C("MsgConsoleShouldInputPlayer"));
            }
        }
        this.mPlugin.getDataManager().saveToMemoryMap(pSender,backupTarget,true);
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
        return CCBukkit.getOnlinePlayersName();
    }
}
