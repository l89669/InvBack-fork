package cc.bukkitPlugin.invback.command;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.manager.DataManager;
import cc.bukkitPlugin.util.JsonExtra;
import cc.bukkitPlugin.util.JsonExtra.ClickAction;
import cc.bukkitPlugin.util.JsonExtra.Color;
import cc.bukkitPlugin.util.JsonExtra.HoverAction;
import cc.bukkitPlugin.util.JsonExtra.Style;
import cc.bukkitPlugin.util.plugin.command.TACommandBase;

public class CommandList extends TACommandBase<InvBack,CommandExc>{

    public CommandList(CommandExc pExector){
        super(pExector,"list");
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length>2)
            return errorArgsNumber(pSender,pArgs.length);

        Date targetDay;
        DataManager tDataMan=this.mPlugin.getManager(DataManager.class);
        if(pArgs.length==0){
            pArgs=new String[]{tDataMan.getFormatOfDay().format(new Date())};
        }else if(pArgs.length==1){
            if(pArgs[0].equalsIgnoreCase("help"))
                return help(pSender,pLabel);
            if(pArgs[0].matches("[\\d]{1,4}")){
                pArgs=new String[]{tDataMan.getFormatOfDay().format(new Date()),pArgs[0]};
            }
        }
        try{
            targetDay=tDataMan.getFormatOfDay().parse(pArgs[0]);
        }catch(Throwable exp){
            return send(pSender,C("MsgErrorTimeFormat","%format%",tDataMan.getFormatOfDay().toPattern()));
        }
        int page=1;
        if(pArgs.length==2){
            try{
                page=Integer.parseInt(pArgs[1]);
            }catch(NumberFormatException ignore){}
        }
        if(page<=0)
            page=1;
        ArrayList<File> backPackets=tDataMan.getInvBackupFile(targetDay);
        if(backPackets.isEmpty())
            return send(pSender,C("MsgNoBackupDataInDay","%day%",pArgs[0]));

        int totalPage=backPackets.size()/20+(backPackets.size()%20==0?0:1);
        if(page>totalPage)
            page=totalPage;
        send(pSender,String.format("========[%s]--[%d/%d]========",pArgs[0],page,totalPage));

        JsonExtra extra=new JsonExtra(InvBack.getMsgPrefix()+"    ");
        JsonExtra zipPack=new JsonExtra("",Color.blue);
        zipPack.setStyle(Style.bold);
        zipPack.setHoverEvent(HoverAction.show_text,C("MsgClickToGenerateCmd"));
        for(int i=(page-1)*20;i<page*20&&i<backPackets.size();i++){
            String tName=backPackets.get(i).getName();
            int pos=tName.lastIndexOf('.');
            if(pos!=-1)
                tName=tName.substring(0,pos);
            zipPack.setText(String.format("%3d: %s",i+1,tName));
            zipPack.setClickEvent(ClickAction.suggest_command,this.mMainCmdLabel+" set "+pSender.getName()+" "+pArgs[0]+" "+tName+" "+pSender.getName());
            extra.clone().addExtra(zipPack).sendToPlayer(pSender);
        }
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> helps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            helps.add(this.constructCmdUsage()+" <"+C("WordDate")+"> ["+C("WordPage")+"]");
            helps.add(this.constructCmdUsage());
            helps.add(this.mExector.getCmdDescPrefix()+C("HelpList1"));
        }
        return helps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> subs=new ArrayList<>();
        if(pArgs.length==1){
            File backupDir=this.mPlugin.getConfigManager().getBackupDir();
            if(!backupDir.isDirectory())
                return subs;

            String[] tSunFileNames=backupDir.list();
            if(tSunFileNames!=null){
                for(String backupDayDir : tSunFileNames){
                    try{
                        this.mPlugin.getDataManager().getFormatOfDay().parse(backupDayDir);
                    }catch(ParseException psexp){
                        continue;
                    }
                    subs.add(backupDayDir);
                }
            }
        }
        return subs;
    }

}
