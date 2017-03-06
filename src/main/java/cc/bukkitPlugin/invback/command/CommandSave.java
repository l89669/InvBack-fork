package cc.bukkitPlugin.invback.command;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.invback.InvBack;

public class CommandSave extends TACommandBase<InvBack,CommandExc>{

    public CommandSave(CommandExc pExector){
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

        Player saveFrom=Bukkit.getPlayer(pArgs[0]);
        if(saveFrom==null)
            return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[0]));
        OfflinePlayer saveTo=null;
        if(pArgs.length==2){
            saveTo=Bukkit.getOfflinePlayer(pArgs[1]);
            if(saveTo==null)
                return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[0]));
        }else{
            if(pSender instanceof Player){
                saveTo=(Player)pSender;
            }else{
                return send(pSender,C("MsgConsoleShouldInputPlayer"));
            }
        }
        this.mPlugin.getDataManager().savePlayerData(pSender,saveFrom,saveTo);
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> helps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            helps.add(this.constructCmdUsage()+" <"+C("WordPlayer")+"> ["+C("WordOfflinePlayer")+"]");
            helps.add(this.constructCmdDesc());
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpSave1"));
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpSave2"));
        }
        return helps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        switch(pArgs.length){
        case 1:
            return BukkitUtil.getOnlinePlayersName();
        case 2:
            return BukkitUtil.getOfflinePlayersName();
        }
        return new ArrayList<>(0);
    }

}
