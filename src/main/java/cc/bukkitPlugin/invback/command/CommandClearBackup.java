package cc.bukkitPlugin.invback.command;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.invback.InvBack;

public class CommandClearBackup extends TACommandBase<InvBack,CommandExc>{

    public CommandClearBackup(CommandExc pExector){
        super(pExector,0);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length>0)
            return errorArgsNumber(pSender,pArgs.length);

        this.mPlugin.getDataManager().clearPlayerData(pSender);
        return true;
    }

}
