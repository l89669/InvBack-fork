package cc.bukkitPlugin.invback.command;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.util.plugin.command.TACommandBase;


public class CommandReset extends TACommandBase<InvBack,CommandExc>{

    public CommandReset(CommandExc pExector){
        super(pExector,1);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);
        
        if(pArgs.length>1)
            return errorArgsNumber(pSender,pArgs.length);
        
        Player tTargetPlayer=null;
        if(pArgs.length==1){
            tTargetPlayer=Bukkit.getPlayer(pArgs[0]);
            if(tTargetPlayer==null)
                return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[0]));
        }else{
            if(pSender instanceof Player){
                tTargetPlayer=(Player)pSender;
            }else{
                return send(pSender,C("MsgConsoleShouldInputPlayer"));
            }
        }
        this.mPlugin.getDataManager().resetPlayerData(pSender,tTargetPlayer);
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> tHelps=new ArrayList<>(2);
        if(hasCmdPermission(pSender)){
            tHelps.add(this.constructCmdUsage()+" "+C("WordPlayer"));
            tHelps.add(this.constructCmdDesc());
        }
        return tHelps;
    }
    
}
