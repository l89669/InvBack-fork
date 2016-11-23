package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.dataBackup.DataBackupAPI;
import cc.bukkitPlugin.invback.util.IBNMSUtil;
import cc.bukkitPlugin.util.ClassUtil;
import cc.bukkitPlugin.util.FileUtil;
import cc.bukkitPlugin.util.NMSUtil;
import cc.bukkitPlugin.util.config.CommentedSection;
import cc.bukkitPlugin.util.nbt.NBTUtil;
import travellersgear.api.TGSaveData;
import travellersgear.api.TravellersGearAPI;

public class DB_TravellersGear extends ADataBackup{

    private String mFileName="TG-SaveData.dat";

    private Method method_TravellersGearAPI_getTravellersNBTData; //static
    private Method method_TGSaveData_setPlayerData; // static
    private Method method_TGSaveData_readFromNBT;
    private Method method_TGSaveData_writeToNBT;

    private TGSaveData mTGSDInstance;
    private HashMap<UUID,Object> mTGSDMap=new HashMap<>();
    private TGSaveData mTempTGSD;
    private HashMap<UUID,Object> mTempTGSDMap=new HashMap<>();

    public DB_TravellersGear(InvBack pPlugin){
        super(pPlugin,"旅行者装备备份");

        this.mDataPath="world"+File.separator+"data"+File.separator;
    }

    @Override
    public boolean init(){
        try{
            Class.forName("travellersgear.TravellersGear");
            Class.forName("travellersgear.api.TravellersGearAPI");
            this.method_TravellersGearAPI_getTravellersNBTData=ClassUtil.getMethod(TravellersGearAPI.class,"getTravellersNBTData",NMSUtil.clazz_EntityPlayer);

            Class.forName("travellersgear.api.TGSaveData");
            this.method_TGSaveData_setPlayerData=ClassUtil.getMethod(TGSaveData.class,"setPlayerData",new Class<?>[]{NMSUtil.clazz_EntityPlayer,NMSUtil.clazz_NBTTagCompound});
            this.mTGSDInstance=ClassUtil.getFieldValue(TGSaveData.class,TGSaveData.class,-1).get(0);
            this.mTGSDMap=(HashMap<UUID,Object>)ClassUtil.getFieldValue(this.mTGSDInstance,"playerData");
            this.mTempTGSD=ClassUtil.getInstance(TGSaveData.class,String.class,"TestData");
            this.mTempTGSDMap=(HashMap<UUID,Object>)ClassUtil.getFieldValue(this.mTempTGSD,"playerData");
            Object tNBTTag=ClassUtil.getInstance(NMSUtil.clazz_NBTTagCompound);
            ArrayList<Method> tMethods=ClassUtil.getUnknowMethod(TGSaveData.class,void.class,NMSUtil.clazz_NBTTagCompound);
            ClassUtil.invokeMethod(this.mTempTGSD,tMethods.get(0),tNBTTag);
            int writeMethod=0;
            if(NBTUtil.getNBTTagMapFromTag(tNBTTag).isEmpty())
                writeMethod=1;
            this.method_TGSaveData_readFromNBT=tMethods.get(1-writeMethod);
            this.method_TGSaveData_writeToNBT=tMethods.get(writeMethod);

        }catch(Throwable exp){
            if(!(exp instanceof ClassNotFoundException)&&!(exp instanceof NoSuchMethodException))
                InvBack.severe("模块 "+this.getDescription()+" 初始化时发生了错误",exp);
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "TravellersGearDataBackup";
    }

    @Override
    public boolean addDefaultConfig(CommentedSection pSection){
        super.addDefaultConfig(pSection);
        pSection.addDefault("FileName",this.mFileName,"旅行者背包数据文件名");
        return true;
    }

    @Override
    public void reloadConfig(CommandSender pSender,CommentedSection pSection){
        super.reloadConfig(pSender,pSection);
        if(pSection!=null){
            this.mFileName=pSection.getString("FileName",this.mFileName);
        }
    }

    @Override
    public boolean backup(CommandSender pSender,File pTargetDir,boolean pEnableReplace) throws IOException{
        if(!this.mEnable)
            return false;

        File tTargetFile=new File(pTargetDir,this.mFileName);
        if(pEnableReplace){
            Object tNBTTag=ClassUtil.getInstance(NMSUtil.clazz_NBTTagCompound);
            ClassUtil.invokeMethod(this.mTGSDInstance,this.method_TGSaveData_writeToNBT,tNBTTag);
            FileOutputStream tFOStream=null;
            try{
                ClassUtil.invokeStaticMethod(IBNMSUtil.method_NBTCompressedStreamTools_writeCompressed,new Object[]{tNBTTag,tFOStream=new FileOutputStream(tTargetFile,false)});
            }finally{
                if(tFOStream!=null)
                    try{
                        tFOStream.close();
                    }catch(Throwable exp){}
            }
        }else{
            File tCopySource=new File(this.mDataDir,this.mFileName);
            if(!tCopySource.isFile()){
                InvBack.warn(pSender,this.mPlugin.C("MsgModelDataFileNotExist",new String[]{"%model%","%file%"},this.getDescription(),tCopySource.getAbsolutePath()));
                return false;
            }
            FileUtil.copyFile(tCopySource,tTargetFile);
        }
        return true;
    }
    
    @Override
    public boolean backup(CommandSender pSender,File pTargetDir,OfflinePlayer pTargetPlayer) throws IOException{
        return this.backup(pSender,pTargetDir,DataBackupAPI.isReplaceFileDataWithOnlineData());
    }

    /**
     * 复制NBTTag数据
     * @param pNBTTagFrom   可以为null
     * @param pNBTTagTo     不能为null
     */
    protected void copyNBTTag(Object pNBTTagFrom,Object pNBTTagTo){
        if(pNBTTagTo==null)
            return;

        Map<String,Object> tMapValue=NBTUtil.getNBTTagMapFromTag(pNBTTagTo);
        tMapValue.clear();
        if(pNBTTagFrom!=null){
            pNBTTagFrom=ClassUtil.invokeMethod(pNBTTagFrom,NMSUtil.method_NBTTagCompound_clone);
            tMapValue.putAll(NBTUtil.getNBTTagMapFromTag(pNBTTagFrom));
        }
    }

    @Override
    public boolean restore(CommandSender pSender,ZipFile pBackupData,OfflinePlayer pFromPlayer,Player pToPlayer) throws IOException{
        if(!this.mEnable)
            return false;

        ZipEntry tEntry=pBackupData.getEntry(this.mFileName);
        if(tEntry==null){
            InvBack.warn(pSender,this.mPlugin.C("MsgModelBackupZipDataNoEntry",new String[]{"%modle%","%file%"},this.getDescription(),this.mFileName));
        }else{
            InputStream tIStream=null;
            try{
                tIStream=pBackupData.getInputStream(tEntry);
                Object tNBTTag=ClassUtil.invokeStaticMethod(IBNMSUtil.method_NBTCompressedStreamTools_readCompressed,tIStream);

                this.mTempTGSDMap.clear();
                ClassUtil.invokeMethod(this.mTempTGSD,this.method_TGSaveData_readFromNBT,tNBTTag);
                Object tPlayerNBTTag=this.mTGSDMap.get(pFromPlayer.getUniqueId());
                Object tNMSPlayer=ClassUtil.invokeMethod(pToPlayer,IBNMSUtil.method_CraftPlayer_getHandle);
                this.copyNBTTag(tPlayerNBTTag,ClassUtil.invokeStaticMethod(this.method_TravellersGearAPI_getTravellersNBTData,tNMSPlayer));
                TGSaveData.setDirty();
            }finally{
                if(tIStream!=null)
                    try{
                        tIStream.close();
                    }catch(Throwable exp){}
            }
        }
        return false;
    }

    @Override
    public boolean saveTo(CommandSender pSender,File pSaveDir,Player pFromPlayer,OfflinePlayer pToPlayer){
        if(!this.mEnable)
            return false;

        if(pFromPlayer==pToPlayer)
            return true;

        Object tNMSPlayer=ClassUtil.invokeMethod(pFromPlayer,IBNMSUtil.method_CraftPlayer_getHandle);
        Object tNBTTagFrom=ClassUtil.invokeStaticMethod(this.method_TravellersGearAPI_getTravellersNBTData,tNMSPlayer);
        Object tNBTTagTo=this.mTGSDMap.get(pToPlayer.getUniqueId());
        if(tNBTTagTo==null){
            tNBTTagTo=ClassUtil.getInstance(NMSUtil.clazz_NBTTagCompound);
            this.mTGSDMap.put(pToPlayer.getUniqueId(),pToPlayer);
        }
        this.copyNBTTag(tNBTTagFrom,tNBTTagTo);
        TGSaveData.setDirty();
        return true;
    }

    @Override
    public boolean loadFrom(CommandSender pSender,File pLoadDir,Player pToPlayer,OfflinePlayer pFromPlayer) throws IOException{
        if(!this.mEnable)
            return false;

        Object tNBTTagFrom=this.mTGSDMap.get(pFromPlayer.getUniqueId());
        Object tNMSPlayer=ClassUtil.invokeMethod(pToPlayer,IBNMSUtil.method_CraftPlayer_getHandle);
        Object tNBTTagTo=ClassUtil.invokeStaticMethod(this.method_TravellersGearAPI_getTravellersNBTData,tNMSPlayer);
        this.copyNBTTag(tNBTTagFrom,tNBTTagTo);
        TGSaveData.setDirty();
        return true;
    }

    @Override
    public boolean saveToMemoryMap(CommandSender pSender,Player pFromPlayer,Map<Object,Object> pMemoryData){
        Object tNMSPlayer=ClassUtil.invokeMethod(pFromPlayer,IBNMSUtil.method_CraftPlayer_getHandle);
        Object tNBTTagFrom=ClassUtil.invokeStaticMethod(this.method_TravellersGearAPI_getTravellersNBTData,tNMSPlayer);
        pMemoryData.put(this.getClass(),ClassUtil.invokeMethod(tNBTTagFrom,NMSUtil.method_NBTTagCompound_clone));
        return true;
    }

    @Override
    public boolean loadFromMemoryMap(CommandSender pSender,Player pToPlayer,Map<Object,Object> pMemoryData){
        Object tNBTTagFrom=pMemoryData.get(this.getClass());
        if(!NMSUtil.clazz_NBTTagCompound.isInstance(tNBTTagFrom))
            return true;

        Object tNBTTagTo=this.mTempTGSDMap.get(pToPlayer.getUniqueId());
        if(tNBTTagTo==null){
            tNBTTagTo=ClassUtil.getInstance(NMSUtil.clazz_NBTTagCompound);
            this.mTGSDMap.put(pToPlayer.getUniqueId(),pToPlayer);
        }
        this.copyNBTTag(tNBTTagFrom,tNBTTagTo);
        TGSaveData.setDirty();
        return true;
    }

    @Override
    public boolean copy(CommandSender pSender,Player pFromPlayer,Player pToPlayer){
        if(!this.mEnable)
            return false;

        return this.saveTo(pSender,null,pFromPlayer,pToPlayer);
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        if(!this.mEnable)
            return false;

        Object tNBTTagFrom=ClassUtil.invokeStaticMethod(this.method_TravellersGearAPI_getTravellersNBTData,NMSUtil.getNMSPlayer(pTargetPlayer));
        NBTUtil.getNBTTagMapFromTag(tNBTTagFrom).clear();
        TGSaveData.setDirty();
        return true;
    }

}
