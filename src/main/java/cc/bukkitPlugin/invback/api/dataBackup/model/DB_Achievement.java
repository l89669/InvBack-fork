package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.CollUtil;
import cc.commons.util.FileUtil;
import cc.commons.util.IOUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class DB_Achievement extends ADB_CompressNBT{

    private Method method_EntityPlayerMP_getStatisticMan;
    private Method method_StatisticsFile_loadStatistic;
    private Method method_StatisticsFile_saveStatistic;
    private Field field_StatFileWriter_stats;

    public DB_Achievement(InvBack pPlugin){
        super(pPlugin,"成就数据备份");

        this.mDataPath="world"+File.separator+"stats"+File.separator;
        this.mFileNameMode=FileNameMode.UUID;
    }

    @Override
    public boolean init(){
        try{
            for(Method sMethod : NMSUtil.clazz_EntityPlayerMP.getDeclaredMethods()){
                if(CollUtil.isEmpty(sMethod.getParameterTypes())&&sMethod.getReturnType().getSimpleName().toLowerCase().contains("statistic")){
                    this.method_EntityPlayerMP_getStatisticMan=sMethod;
                    break;
                }
            }
            if(this.method_EntityPlayerMP_getStatisticMan==null)
                return false;

            Class<?> tClazz=this.method_EntityPlayerMP_getStatisticMan.getReturnType();
            this.method_StatisticsFile_loadStatistic=MethodUtil.getUnknowMethod(tClazz,Map.class,String.class,true).get(0);
            this.method_StatisticsFile_saveStatistic=MethodUtil.getUnknowMethod(tClazz,String.class,Map.class,true).get(0);
            this.field_StatFileWriter_stats=FieldUtil.getField(tClazz.getSuperclass(),Map.class,-1,true).get(0);

        }catch(Throwable exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDescription()+" 初始化时发生了错误",exp);
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "AchievementDataBackup";
    }

    @Override
    protected String getFileSuffix(){
        return "json";
    }

    @Override
    public boolean addDefaultConfig(CommentedSection pSection){
        pSection.getParent().addDefaultComments(pSection.getName(),"成就系统由于客户端的更新机制(只会完成而不会消失)","在服务器重置成就后,客户端的成就显示会不及时","可通过重新登陆服务器解决");
        return super.addDefaultConfig(pSection);
    }

    @Override
    protected Object saveDataToNBT(Player pFromPlayer){
        throw new UnsupportedOperationException();
    }

    @Override
    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        throw new UnsupportedOperationException();
    }

    @Override
    protected void loadDataFromStream(CommandSender pSender,InputStream pIStream,Player pToPlayer) throws IOException{
        this.loadDataFromString(pToPlayer,IOUtil.readContent(pIStream,"UTF-8"));
    }

    private Object getStatMan(Player pPlayer){
        return MethodUtil.invokeMethod(this.method_EntityPlayerMP_getStatisticMan,NMSUtil.getNMSPlayer(pPlayer));
    }

    private Map<Object,Object> getManStatValue(Object pStatMan){
        return (Map<Object,Object>)FieldUtil.getFieldValue(this.field_StatFileWriter_stats,pStatMan);
    }

    protected String saveDataToString(Player pFromPlayer){
        Object tStatMan=this.getStatMan(pFromPlayer);
        return (String)MethodUtil.invokeMethod(this.method_StatisticsFile_saveStatistic,tStatMan,this.getManStatValue(tStatMan));
    }

    protected void loadDataFromString(Player pToPlayer,String pData){
        Object tStatMan=this.getStatMan(pToPlayer);
        Map<Object,Object> tPlayerStatValue=this.getManStatValue(tStatMan);
        tPlayerStatValue.clear();
        tPlayerStatValue.putAll((Map<Object,Object>)MethodUtil.invokeMethod(this.method_StatisticsFile_loadStatistic,tStatMan,pData));
    }

    @Override
    public boolean restore(CommandSender pSender,ZipFile pBackupData,OfflinePlayer pFromPlayer,Player pToPlayer) throws IOException{
        if(!this.mEnable)
            return false;

        String tZipEntrySuffix=this.getPlayerFileName(pFromPlayer);
        ZipEntry tEntry=pBackupData.getEntry(this.getName()+File.separator+tZipEntrySuffix);
        if(tEntry==null){
            Log.warn(pSender,this.mPlugin.C("MsgModelBackupDataNotFoundPlayer",new String[]{"%model%","%player%"},this.getDescription(),pFromPlayer.getName()));
        }else{
            this.loadDataFromStream(pSender,pBackupData.getInputStream(tEntry),pToPlayer);
        }
        return false;
    }

    @Override
    public boolean loadFrom(CommandSender pSender,File pLoadDir,Player pToPlayer,OfflinePlayer pFromPlayer) throws IOException{
        if(!this.mEnable)
            return false;

        File tLoadDir=pLoadDir==null?this.mDataDir:pLoadDir;
        File tLoadFile=new File(tLoadDir,this.getPlayerFileName(pFromPlayer));
        if(!tLoadFile.isFile()){
            Log.warn(pSender,this.mPlugin.C("MsgModelBackupDataNotFoundPlayer",new String[]{"%model%","%player%"},this.getDescription(),pFromPlayer.getName()));
        }else{
            this.loadDataFromStream(pSender,new FileInputStream(tLoadFile),pToPlayer);
        }
        return true;
    }

    @Override
    public boolean saveTo(CommandSender pSender,File pSaveDir,Player pFromPlayer,OfflinePlayer pToPlayer) throws IOException{
        if(!this.mEnable)
            return false;

        File tSaveDir=pSaveDir==null?this.mDataDir:pSaveDir;
        String tData=this.saveDataToString(pFromPlayer);
        String tFileName=this.getPlayerFileName(pToPlayer);
        File tDataFile=new File(tSaveDir,tFileName);
        File tDataFileTmp=new File(tSaveDir,tFileName+".tmp");
        FileOutputStream tFOStream=null;
        try{
            tFOStream=FileUtil.openOutputStream(tDataFileTmp,false);
            tFOStream.write(tData.getBytes("UTF-8"));
            tFOStream.flush();
            if(tDataFile.isFile())
                tDataFile.delete();
            tDataFileTmp.renameTo(tDataFile);
        }finally{
            IOUtil.closeStream(tFOStream);
        }
        return true;
    }

    @Override
    public boolean loadFromMemoryMap(CommandSender pSender,Player pToPlayer,Map<Object,Object> pMemoryData){
        Object tObj=pMemoryData.get(this.getClass());
        if(tObj instanceof String){
            this.loadDataFromString(pToPlayer,(String)tObj);
        }
        return true;
    }

    @Override
    public boolean saveToMemoryMap(CommandSender pSender,Player pFromPlayer,Map<Object,Object> pMemoryData){
        pMemoryData.put(this.getClass(),this.saveDataToString(pFromPlayer));
        return true;
    }

    @Override
    public boolean copy(CommandSender pSender,Player pFromPlayer,Player pToPlayer){
        if(!this.mEnable)
            return false;

        this.loadDataFromString(pToPlayer,this.saveDataToString(pFromPlayer));
        return true;
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        if(!this.mEnable)
            return false;

        this.loadDataFromString(pTargetPlayer,"{}");
        return true;
    }

}
