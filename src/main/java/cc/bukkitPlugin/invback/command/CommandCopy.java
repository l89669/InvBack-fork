package cc.bukkitPlugin.invback.command;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.manager.DataManager;
import cc.bukkitPlugin.util.CCBukkit;
import cc.bukkitPlugin.util.plugin.command.TACommandBase;

public class CommandCopy extends TACommandBase<InvBack,CommandExc>{

    public CommandCopy(CommandExc pExector){
        super(pExector,2);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length>2)
            return errorArgsNumber(pSender,pArgs.length);
        if(pArgs.length==0)
            return help(pSender,pLabel);

        Player dataFrom=Bukkit.getPlayer(pArgs[0]),dataTo=null;
        if(dataFrom==null)
            return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[0]));
        if(pArgs.length==2){
            dataTo=Bukkit.getPlayer(pArgs[1]);
            if(dataTo==null)
                return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[1]));
        }else{
            if(pSender instanceof Player){
                dataTo=(Player)pSender;
            }else{
                return send(pSender,C("MsgConsoleShouldInputPlayer"));
            }
        }
        if(dataFrom==dataTo)
            return send(pSender,C("MsgNoSenseToCopyDataFromSelf"));

        this.mPlugin.getManager(DataManager.class).copyPlayerData(pSender,dataFrom,dataTo);
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> helps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            helps.add(this.constructCmdUsage()+" <"+C("WordPlayer")+"1> ["+C("WordPlayer")+"2]");
            helps.add(this.constructCmdDesc());
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpCopy1"));
        }
        return helps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        return CCBukkit.getOnlinePlayersName();
    }

}
