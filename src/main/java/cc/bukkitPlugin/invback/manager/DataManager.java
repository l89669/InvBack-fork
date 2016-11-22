package cc.bukkitPlugin.invback.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.dataBackup.DataBackupAPI;
import cc.bukkitPlugin.invback.api.dataBackup.IDataBackup;
import cc.bukkitPlugin.util.FileUtil;
import cc.bukkitPlugin.util.Function;
import cc.bukkitPlugin.util.IOUtil;
import cc.bukkitPlugin.util.plugin.manager.AManager;

public class DataManager extends AManager<InvBack>{

    public final SimpleDateFormat mFormatOfDay=new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat mFormatOfTime=new SimpleDateFormat("HH-mm-ss");
    /**备份文件匹配模式*/
    public final Pattern backFileName=Pattern.compile("(\\d{2}-\\d{2}-\\d{2})\\.zip",2);
    /**内存中的玩家备份数据*/
    private final HashMap<UUID,Map<Object,Object>> mMemoryInvBackup=new HashMap<>();

    public DataManager(InvBack pPlugin){
        super(pPlugin);
    }

    public SimpleDateFormat getFormatOfDay(){
        return this.mFormatOfDay;
    }

    public SimpleDateFormat getFormatOfTime(){
        return this.mFormatOfTime;
    }

    /**
     * 根据提供的时间获取对应的备份文件名字
     * @param pTime     时间
     * @return          对应时间的文件
     */
    protected String getBackupFileNameFromDate(Date pTime){
        return this.mFormatOfDay.format(pTime)+File.separator+this.mFormatOfTime.format(pTime)+".zip";
    }

    /**
     * 备份玩家数据
     * @param pSender       请求发送者
     */
    synchronized public void backup(CommandSender pSender){
        InvBack.send(pSender,this.mPlugin.C("MsgStartBackup"));
        long tStartTime=InvBack.getJVMTime();
        File tTempDir=new File(DataBackupAPI.getBackupDir(),"temp"+File.separator);

        try{
            FileUtil.clearDir(tTempDir);
        }catch(IOException exp){
            InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnClearFileDir","%dir%",tTempDir.getAbsolutePath())+": "+exp.getMessage(),exp);
        }
        if(!tTempDir.isDirectory())
            tTempDir.mkdirs();

        Date tBackTime=new Date(InvBack.getJVMTime());
        boolean tEnableReplace=DataBackupAPI.isReplaceFileDataWithOnlineData();
        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            try{
                File tModleTempDir=new File(tTempDir,sModel.getName());
                tModleTempDir.mkdir();
                sModel.backup(pSender,tModleTempDir,tEnableReplace);
            }catch(Throwable exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelBackupFile","%modle%",sModel.getDescription())+": "+exp.getMessage(),exp);
            }
        }

        if(Function.isEmpty(tTempDir.list())){
            InvBack.info(pSender,this.mPlugin.C("MsgNoFileCopyToTempDir"));
            return;
        }
        String tTimedBackupFileName=this.getBackupFileNameFromDate(tBackTime);
        File tTimedBackupFile=new File(DataBackupAPI.getBackupDir(),tTimedBackupFileName);
        File tTimedTempZipFile=new File(DataBackupAPI.getBackupDir(),tTimedBackupFileName+".tmp");
        ZipOutputStream tZOStream=null;
        try{
            FileUtil.createNewFile(tTimedTempZipFile,true);
            tZOStream=new ZipOutputStream(new FileOutputStream(tTimedTempZipFile));
            FileUtil.zipFileAndDir(tZOStream,tTempDir,false);
        }catch(IOException exp){
            InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnZipTempFileToBackupFile")+": "+exp.getMessage(),exp);
            return;
        }finally{
            IOUtil.closeStream(tZOStream);
        }

        tTimedTempZipFile.renameTo(tTimedBackupFile);
        if(!ConfigManager.isDebug()){
            tTimedTempZipFile.delete();
            try{
                FileUtil.clearDir(tTempDir);
            }catch(IOException ignore){}
        }
        InvBack.send(pSender,this.mPlugin.C("MsgBackupCompleteAndTookTime","%time%",InvBack.getJVMTime()-tStartTime+"ms"));
    }

    /**
     * 使用指定的玩家存档还原在线玩家数据
     * @param pSender       请求发送者
     * @param pTime         还原的时间
     * @param pFromPlayer   数据来源玩家
     * @param pToPlayer     要还原数据的玩家
     */
    public void restorePlayerData(CommandSender pSender,File pRestoreFile,OfflinePlayer pFromPlayer,Player pToPlayer){
        ZipFile tZipFile=null;
        try{
            tZipFile=new ZipFile(pRestoreFile);
        }catch(IOException exp){
            InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnOpenZipFile","%file%",pRestoreFile.getAbsolutePath())+": "+exp.getMessage(),exp);
            return;
        }
        GameMode tGameMode=pToPlayer.getGameMode();
        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            try{
                sModel.restore(pSender,tZipFile,pFromPlayer,pToPlayer);
            }catch(Throwable exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelRestoreData","%modle%",sModel.getDescription())+": "+exp.getMessage(),exp);
            }
        }

        if(this.mPlugin.getConfigManager().isKeepGameModeWhenRollBack()&&tGameMode!=pToPlayer.getGameMode())
            pToPlayer.setGameMode(tGameMode);
        InvBack.send(pSender,this.mPlugin.C("MsgPlayerDataRestore",new String[]{"%fromplayer%","%toplayer%"},pFromPlayer.getName(),pToPlayer.getName()));
    }

    /**
     * 使用指定的玩家存档还原在线玩家数据
     * @param pSender       请求发送者
     * @param pTime         还原的时间
     * @param pFromPlayer   数据来源玩家
     * @param pToPlayer     要还原数据的玩家
     */
    public void restorePlayerData(CommandSender pSender,Date pTime,OfflinePlayer pFromPlayer,Player pToPlayer){
        File tRestoreFile=new File(DataBackupAPI.getBackupDir(),this.getBackupFileNameFromDate(pTime));
        if(!tRestoreFile.isFile()){
            InvBack.warn(pSender,this.mPlugin.C("MsgBackupFileNotFound","%file%",tRestoreFile.getAbsolutePath()));
            return;
        }
        this.restorePlayerData(pSender,tRestoreFile,pFromPlayer,pToPlayer);
    }

    /**
     * 将在线玩家的数据保存到指定的玩家存档
     * @param pSender       请求发送者
     * @param pFromPlayer   数据来源
     * @param pToPlayer     要保存到的玩家
     */
    public void savePlayerData(CommandSender pSender,Player pFromPlayer,OfflinePlayer pToPlayer){
        String tFromName=pSender==pFromPlayer?this.mPlugin.C("WordYou"):pFromPlayer.getName();
        GameMode tGameMode=pFromPlayer.getGameMode();
        if(tGameMode!=GameMode.SURVIVAL)
            pFromPlayer.setGameMode(GameMode.SURVIVAL);

        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            try{
                sModel.saveTo(pSender,null,pFromPlayer,pToPlayer);
            }catch(Throwable exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelSavePlayerDataToFile",new String[]{"%modle%","%player%"},sModel.getDescription(),pFromPlayer.getName())+": "+exp.getMessage(),exp);
            }
        }

        if(tGameMode!=pFromPlayer.getGameMode())
            pFromPlayer.setGameMode(tGameMode);
        if(pFromPlayer==pToPlayer){
            InvBack.send(pSender,this.mPlugin.C("MsgAlreadySavePlayerDataToFile","%toplayer%",tFromName));
        }else{
            InvBack.send(pSender,this.mPlugin.C("MsgAlreadySaveOtherPlayerDataToPlayerFile",new String[]{"%fromplayer%","%toplayer%"},tFromName,pToPlayer.getName()));
        }
    }

    /**
     * 从指定玩家存档载入玩家说
     * @param pSender       请求发送者
     * @param pToPlayer     要还原数据的玩家
     * @param pFromPlayer   数据来源
     */
    public void loadPlayerData(CommandSender pSender,Player pToPlayer,OfflinePlayer pFromPlayer){
        String tToName=pSender==pToPlayer?this.mPlugin.C("WordYou"):pToPlayer.getName();
        String tFromName=pSender==pFromPlayer?this.mPlugin.C("WordYou"):pFromPlayer.getName();
        GameMode tGameMode=pToPlayer.getGameMode();

        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            try{
                sModel.loadFrom(pSender,null,pToPlayer,pFromPlayer);
            }catch(Throwable exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelLoadPlayerDataFromFile",new String[]{"%modle%","%player%"},sModel.getDescription(),tFromName)+": "+exp.getMessage(),exp);
            }
        }

        if(this.mPlugin.getConfigManager().isKeepGameModeWhenRollBack()&&tGameMode!=pToPlayer.getGameMode())
            pToPlayer.setGameMode(tGameMode);
        InvBack.send(pSender,this.mPlugin.C("MsgAlreadyLoadPlayerDataFromFile",new String[]{"%fromplayer%","%toplayer%"},tFromName,tToName));
    }

    /**
     * 保存玩家数据到内存中
     * @param pSender       请求发送者
     * @param pFromPlayer   数据来源
     * @param pNotify       是否在成功时发送通知
     */
    public void saveToMemoryMap(CommandSender pSender,Player pFromPlayer,boolean pNotify){
        String tPlayerName=pSender==pFromPlayer?this.mPlugin.C("WordYou"):pFromPlayer.getName();
        Map<Object,Object> tMemoryData=new HashMap<>();

        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            try{
                sModel.saveToMemoryMap(pSender,pFromPlayer,tMemoryData);
            }catch(Throwable exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelSavePlayerDataToMemory",new String[]{"%modle%","%player%"},sModel.getDescription(),pFromPlayer.getName())+": "+exp.getMessage(),exp);
            }
        }
        this.mMemoryInvBackup.put(pFromPlayer.getUniqueId(),tMemoryData);

        if(pNotify){
            InvBack.send(pSender,this.mPlugin.C("MsgAlreadySavePlayerDataToMemory","%player%",tPlayerName));
        }
    }

    /**
     * 从内存中载入玩家数据
     * @param pSender       请求发送者
     * @param pToPlayer     要载入的玩家
     * @param pNotify       是否在成功时发送通知
     */
    public void loadFromMemoryMap(CommandSender pSender,Player pToPlayer,boolean pNotify){
        String tPlayerName=pSender==pToPlayer?this.mPlugin.C("WordYou"):pToPlayer.getName();
        Map<Object,Object> tMemoryData=this.mMemoryInvBackup.get(pToPlayer.getUniqueId());
        if(tMemoryData==null){
            InvBack.warn(pSender,this.mPlugin.C("MsgNoMemoryDataForPlayer","%player%",tPlayerName));
            return;
        }

        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            try{
                sModel.loadFromMemoryMap(pSender,pToPlayer,tMemoryData);
            }catch(Throwable exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelLoadPlayerDataFromMemory",new String[]{"%modle%","%player%"},sModel.getDescription(),pToPlayer.getName())+": "+exp.getMessage(),exp);
            }
        }

        if(pNotify){
            InvBack.send(pSender,this.mPlugin.C("MsgAlreadyLoadPlayerDataFromMemory","%player%",tPlayerName));
        }
    }

    /**
     * 在线玩家数据复制
     * @param pSender       请求发送者
     * @param pFromPlayer   数据来源
     * @param pToPlayer     数据复制到的玩家
     */
    public void copyPlayerData(CommandSender pSender,Player pFromPlayer,Player pToPlayer){
        String tToName=pSender==pToPlayer?this.mPlugin.C("WordYou"):pToPlayer.getName();
        String tFromName=pSender==pFromPlayer?this.mPlugin.C("WordYou"):pFromPlayer.getName();
        GameMode tGameMode=pToPlayer.getGameMode();

        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            try{
                sModel.copy(pSender,pFromPlayer,pToPlayer);
            }catch(Throwable exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelCopyPlayerData",new String[]{"%modle%","%player%"},sModel.getDescription(),pFromPlayer.getName())+": "+exp.getMessage(),exp);
            }
        }

        if(this.mPlugin.getConfigManager().isKeepGameModeWhenRollBack()&&tGameMode!=pToPlayer.getGameMode())
            pToPlayer.setGameMode(tGameMode);
        if(pSender==pToPlayer){
            InvBack.send(pSender,this.mPlugin.C("MsgAlreadyCopyed","%player%",pFromPlayer.getName()));
        }else{
            InvBack.send(pSender,this.mPlugin.C("MsgAlreadyCopyedForPlayer",new String[]{"%fromplayer%","%toplayer%"},tFromName,tToName));
        }
    }

    /**
     * 重置一个玩家的数据
     * @param pSender       请求发送者
     * @param pTargetPlayer 要重置的玩家
     */
    public void resetPlayerData(CommandSender pSender,Player pTargetPlayer){
        String tPlayerName=pSender==pTargetPlayer?this.mPlugin.C("WordYou"):pTargetPlayer.getName();
        GameMode tGameMode=pTargetPlayer.getGameMode();

        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            try{
                sModel.reset(pSender,pTargetPlayer);
            }catch(Throwable exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelResetPlayerData",new String[]{"%modle%","%player%"},sModel.getDescription(),pTargetPlayer.getName())+": "+exp.getMessage(),exp);
            }
        }

        if(this.mPlugin.getConfigManager().isKeepGameModeWhenRollBack()&&tGameMode!=pTargetPlayer.getGameMode())
            pTargetPlayer.setGameMode(tGameMode);
        InvBack.send(pSender,this.mPlugin.C("MsgAlreadyResetPlayerData","%player%",tPlayerName));
    }

    public void clearExpriedBackup(){
        File tBackupDir=DataBackupAPI.getBackupDir();
        File[] tSubDirs=tBackupDir.listFiles();
        if(tSubDirs==null||tSubDirs.length==0)
            return;

        List<String> dirNames=new ArrayList<>();
        for(File sDayDir : tSubDirs){
            if(!sDayDir.isDirectory())
                continue;
            try{
                this.mFormatOfDay.parse(sDayDir.getName());
            }catch(ParseException psexp){
                continue;
            }
            dirNames.add(sDayDir.getName());
        }

        int tBackupExpriedDays=DataBackupAPI.getBackupExpriedDays();
        if(dirNames.size()<=tBackupExpriedDays)
            return;

        Collections.sort(dirNames);
        int deleteDirCount=dirNames.size()-tBackupExpriedDays;
        for(String sDirName : dirNames){
            if(deleteDirCount<=0)
                break;

            try{
                FileUtil.deleteFile(new File(tBackupDir,sDirName));
            }catch(IOException exp){
                InvBack.severe(this.mPlugin.C("MsgErrorOnClearExpriedBackup")+": "+exp.getMessage(),exp);
            }
            deleteDirCount--;
        }
    }

    /**
     * 获取指定时间的备份背包
     * @param pDate 时间
     * @return 备份短文件名和文件HashMap,非null
     */
    public ArrayList<File> getInvBackupFile(Date pDate){
        ArrayList<File> allFiles=new ArrayList<>();
        String targetDay=this.mFormatOfDay.format(pDate);

        File tTargetDayDir=new File(this.mPlugin.getConfigManager().getBackupDir(),targetDay);
        File[] tListFile=tTargetDayDir.listFiles();
        if(tListFile==null||tListFile.length==0)
            return allFiles;

        for(File backFile : tListFile){
            if(!backFile.isFile())
                continue;
            if(!backFileName.matcher(backFile.getName()).find())
                continue;
            allFiles.add(backFile);
        }
        Collections.sort(allFiles,new Comparator<File>(){

            @Override
            public int compare(File o1,File o2){
                return o1.getName().compareTo(o2.getName());
            }

        });
        return allFiles;
    }

    /**
     * 移除内存中备份的玩家数据
     * @param pPlayer 玩家
     * @return NBTTagCompound的实例或null
     */
    public Map<Object,Object> removePlayerDataFromMemony(Player pPlayer){
        if(pPlayer==null)
            return null;
        return this.mMemoryInvBackup.remove(pPlayer.getUniqueId());
    }

    /**
     * 清理内存中备份的玩家数据
     * @param pPlayer 玩家
     * @return 清理的数量
     */
    public void clearPlayerData(CommandSender pSender){
        int tNumb=this.mMemoryInvBackup.size();
        this.mMemoryInvBackup.clear();
        if(tNumb==0){
            InvBack.send(pSender,this.mPlugin.C("MsgNoMemoryBackupDataClear"));
        }else{
            InvBack.send(pSender,this.mPlugin.C("MsgMemoryBackupDataCleared","%numb%",tNumb));
        }
    }

}
