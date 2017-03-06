package cc.bukkitPlugin.invback.command;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.invback.InvBack;

public class CommandRunTask extends TACommandBase<InvBack,CommandExc>{

    private HashSet<String> mAlias=new HashSet<>();
    
    public CommandRunTask(CommandExc pExector){
        super(pExector,0);
        
        this.mAlias.add("run");
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length>0)
            return errorArgsNumber(pSender,pArgs.length);

        this.mPlugin.getTaskExc().backupPlayerData();
        return send(pSender,C("MsgBackupTaskHasRun"));
    }
    
    @Override
    public HashSet<String> getCommandLabelAlias(){
        HashSet<String> tAliaCopy=new HashSet<>(this.mAlias.size());
        tAliaCopy.addAll(this.mAlias);
        return tAliaCopy;
    }
    
    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        if(pLabel.equalsIgnoreCase("run"))
            return new ArrayList<>(0);
        return super.getHelp(pSender,pLabel);
    }
    
}
