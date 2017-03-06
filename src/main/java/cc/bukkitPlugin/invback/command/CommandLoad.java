package cc.bukkitPlugin.invback.command;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.invback.InvBack;

public class CommandLoad extends TACommandBase<InvBack,CommandExc>{

    public CommandLoad(CommandExc pExector){
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

        OfflinePlayer loadFrom=Bukkit.getOfflinePlayer(pArgs[0]);
        if(loadFrom==null)
            return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[0]));
        Player loadTo=null;
        if(pArgs.length==2){
            loadTo=Bukkit.getPlayer(pArgs[1]);
            if(loadTo==null)
                return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[1]));
        }else{
            if(pSender instanceof Player){
                loadTo=(Player)pSender;
            }else{
                return send(pSender,C("MsgConsoleShouldInputPlayer"));
            }
        }
        this.mPlugin.getDataManager().loadPlayerData(pSender,loadTo,loadFrom);
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> helps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            helps.add(this.constructCmdUsage()+" <"+C("WordOfflinePlayer")+"> ["+C("WordPlayer")+"]");
            helps.add(this.constructCmdDesc());
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpLoad1"));
        }
        return helps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        switch(pArgs.length){
        case 1:
            return BukkitUtil.getOfflinePlayersName();
        case 2:
            return BukkitUtil.getOnlinePlayersName();
        }
        return new ArrayList<>(0);
    }

}
