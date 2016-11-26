package cc.bukkitPlugin.invback.command;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.manager.DataManager;
import cc.bukkitPlugin.util.CCBukkit;
import cc.bukkitPlugin.util.plugin.command.TACommandBase;

public class CommandSet extends TACommandBase<InvBack,CommandExc>{

    public static final String fileShortName="\\d{2}-\\d{2}-\\d{2}";

    public CommandSet(CommandExc pExector){
        super(pExector,4);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mCmdLabel);

        if(pArgs.length<=1){
            if(pArgs.length==0||(pArgs[0].equalsIgnoreCase("help")))
                return help(pSender,pLabel);
            return unknowChildCommand(pSender,pLabel,pArgs[0]);
        }

        if(pArgs.length!=4){
            return errorArgsNumber(pSender,pArgs.length);
        }

        //第一参数--要设置的目标玩家
        Player setTarget=Bukkit.getPlayer(pArgs[0]);
        if(setTarget==null)
            return send(pSender,C("MsgPlayerNotOnlineOrExist","%player%",pArgs[0]));

        //第二参数--时间
        Date targetDay=null;
        DataManager tDataMan=this.mPlugin.getDataManager();
        try{
            targetDay=tDataMan.getFormatOfDay().parse(pArgs[1]);
        }catch(Throwable exp){
            return send(pSender,C("MsgErrorTimeFormat","%format%",tDataMan.getFormatOfDay().toPattern()));
        }
        ArrayList<File> backPackets=this.mPlugin.getDataManager().getInvBackupFile(targetDay);
        if(backPackets.size()==0)
            return send(pSender,C("MsgNoBackupDataInDay","%day%",pArgs[1]));

        //第三参数--背包序号或背包名字
        File targetPacket=null;
        if(Pattern.matches(fileShortName,pArgs[2])){
            String findFileName=pArgs[2]+".zip";
            for(File sFile : backPackets){
                if(sFile.getName().equalsIgnoreCase(findFileName)){
                    targetPacket=sFile;
                    break;
                }
            }
            if(targetPacket==null)
                return send(pSender,C("MsgBackupDataNoFoundInDay",new String[]{"%day%","%file%"},pArgs[1],findFileName));
        }else{
            int packetNumb=0;
            try{
                packetNumb=Integer.parseInt(pArgs[2]);
            }catch(NumberFormatException nfexp){
                return send(pSender,C("MsgNotCorrectBackupDataNumb","%string%",pArgs[2]));
            }
            if(packetNumb<=0||packetNumb>backPackets.size())
                return send(pSender,C("MsgBackupDataNumbNotInRange","%numb%",pArgs[2])+"[1-"+backPackets.size()+"]");
            targetPacket=backPackets.get(--packetNumb);
        }

        //第四参数--背包源
        OfflinePlayer invFrom=Bukkit.getOfflinePlayer(pArgs[3]);
        tDataMan.restorePlayerData(pSender,targetPacket,invFrom,setTarget);
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> helps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            helps.add(this.constructCmdUsage()+" <"+C("WordPlayer")+"> <"+C("WordDate")+"> <"+C("WordTime")+"> <"+C("WordOfflinePlayer")+">");
            helps.add(this.constructCmdDesc());
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpSet1"));
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpSet2"));
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpSet3"));
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpSet4"));
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpSet5"));
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpSet6"));
        }
        return helps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        DataManager tDataMan=this.mPlugin.getDataManager();
        ArrayList<String> subs=new ArrayList<>();
        switch(pArgs.length){
        case 1:
            return CCBukkit.getOnlinePlayersName();
        case 2:
            File backupDir=this.mPlugin.getConfigManager().getBackupDir();
            if(!backupDir.isDirectory())
                return subs;
            String[] listFileName=backupDir.list();
            if(listFileName!=null&&listFileName.length!=0){
                for(String backupDayDir : listFileName){
                    try{
                        tDataMan.getFormatOfDay().parse(backupDayDir);
                    }catch(ParseException psexp){
                        continue;
                    }
                    subs.add(backupDayDir);
                }
            }
            return subs;
        case 3:
            Date tDate=null;
            try{
                tDate=tDataMan.getFormatOfDay().parse(pArgs[1]);
            }catch(ParseException pexp){
                return subs;
            }
            ArrayList<File> listFile=tDataMan.getInvBackupFile(tDate);
            for(File sFile : listFile){
                String tName=sFile.getName();
                int pos=tName.lastIndexOf('.');
                if(pos!=-1)
                    tName=tName.substring(0,pos);
                subs.add(tName);
                if(subs.size()>100)
                    break;
            }
            return subs;
        case 4:
            return CCBukkit.getOfflinePlayersName();
        default:
            return subs;
        }
    }
}
