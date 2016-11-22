package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.invback.util.IBNMSUtil;
import cc.bukkitPlugin.util.CCBukkit;
import cc.bukkitPlugin.util.FileUtil;
import cc.bukkitPlugin.util.IOUtil;
import cc.bukkitPlugin.util.ClassUtil;
import cc.bukkitPlugin.util.config.CommentedSection;
import cc.bukkitPlugin.util.nbt.NBTUtil;

/**
 * 此模块使用范围:<br>
 * 1.文件中的数据存储模式为压缩NBT<br>
 * 2.文件中读取出来的NBT可以还原到玩家身上
 * @author 聪聪
 *
 */
public abstract class ADB_CompressNBT extends ADataBackup{

    protected FileNameMode mFileNameMode=FileNameMode.NAME;

    public ADB_CompressNBT(InvBack pPlugin,String pDescription){
        super(pPlugin,pDescription);
    }

    @Override
    public boolean addDefaultConfig(CommentedSection pSection){
        super.addDefaultConfig(pSection);
        pSection.addDefault("FileNameMode",this.mFileNameMode.name(),"数据文件名字模式,可用的设置值为UUID,Name");
        return true;
    }

    @Override
    public void reloadConfig(CommandSender pSender,CommentedSection pSection){
        super.reloadConfig(pSender,pSection);
        if(pSection!=null){
            this.mFileNameMode=FileNameMode.getMode(pSection.getString("FileNameMode"),this.mFileNameMode);
        }
    }

    /**
     * 获取此模块玩家数据文件后缀
     * @return
     */
    protected abstract String getFileSuffix();

    protected String getPlayerFileName(OfflinePlayer pPlayer){
        String tFilePrefix=pPlayer.getUniqueId().toString();
        if(this.mFileNameMode==FileNameMode.NAME)
            tFilePrefix=pPlayer.getName();

        return tFilePrefix+"."+this.getFileSuffix();
    }

    protected void loadDataFromStream(CommandSender pSender,InputStream pIStream,Player pToPlayer) throws IOException{
        try{
            Object tNBTTag=ClassUtil.invokeStaticMethod(IBNMSUtil.method_NBTCompressedStreamTools_readCompressed,pIStream);
            this.loadDataFromNBT(pToPlayer,tNBTTag);
        }finally{
            pIStream.close();
        }
    }

    /**
     * 保存玩家数据到NBT
     * @param pFromPlayer   要保存数据的玩家
     * @return              玩家此模块的NBT数据
     */
    protected abstract Object saveDataToNBT(Player pFromPlayer);

    /**
     * 从NBT中载入玩家此模块的数据
     * @param pToPlayer     要载入数据的玩家
     * @param pNBT          此模块的NBT数据
     */
    protected abstract void loadDataFromNBT(Player pToPlayer,Object pNBT);

    @Override
    public boolean backup(CommandSender pSender,File pTargetDir,boolean pEnableReplace) throws IOException{
        if(!(this instanceof DB_VanillaData)||!this.mEnable)
            return false;
        
        HashSet<String> tBackedFile=new HashSet<>();
        if(pEnableReplace){
            for(Player sPlayer : CCBukkit.getOnlinePlayers()){
                if(!sPlayer.isOnline())
                    continue;

                try{
                    if(this.saveTo(pSender,pTargetDir,sPlayer,sPlayer)){
                        tBackedFile.add(this.getPlayerFileName(sPlayer).toLowerCase());
                    }
                }catch(Throwable exp){
                    InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelSavePlayerDataToFile",new String[]{"%model%","%player%"},this.getDescription(),sPlayer.getName())+": "+exp.getMessage(),exp);
                }
            }
        }

        File[] tListFile=this.mDataDir.listFiles();
        if(tListFile==null||tListFile.length==0)
            return true;
        for(File sPlayerDataFile : tListFile){
            String tFileName=sPlayerDataFile.getName().toLowerCase();
            if(!tFileName.endsWith("."+this.getFileSuffix())||tBackedFile.contains(tFileName))
                continue;

            try{
                FileUtil.copyFile(sPlayerDataFile,new File(pTargetDir,sPlayerDataFile.getName()));
            }catch(IOException exp){
                InvBack.severe(pSender,this.mPlugin.C("MsgErrorOnModelBackupCopyFile","%model%","this.getDescription()")+": "+exp.getMessage(),exp);
            }
        }
        return true;
    }

    @Override
    public boolean restore(CommandSender pSender,ZipFile pBackupData,OfflinePlayer pFromPlayer,Player pToPlayer) throws IOException{
        if(!(this instanceof DB_VanillaData)||!this.mEnable)
            return false;
        
        String tZipEntrySuffix=this.getPlayerFileName(pFromPlayer);
        ZipEntry tEntry=pBackupData.getEntry(this.getName()+File.separator+tZipEntrySuffix);
        if(tEntry==null&&this instanceof DB_VanillaData){ // 使用旧的保存方式
            tEntry=pBackupData.getEntry(tZipEntrySuffix);
        }
        if(tEntry==null){
            InvBack.warn(pSender,this.mPlugin.C("MsgModelBackupDataNotFoundPlayer",new String[]{"%model%","%player%"},this.getDescription(),pFromPlayer.getName()));
        }else{
            this.loadDataFromStream(pSender,pBackupData.getInputStream(tEntry),pToPlayer);
        }
        return false;
    }

    @Override
    public boolean saveTo(CommandSender pSender,File pSaveDir,Player pFromPlayer,OfflinePlayer pToPlayer) throws IOException{
        if(!(this instanceof DB_VanillaData)||!this.mEnable)
            return false;
        
        File tSaveDir=pSaveDir==null?this.mDataDir:pSaveDir;
        Object tNBTTag=this.saveDataToNBT(pFromPlayer);
        String tFileName=this.getPlayerFileName(pToPlayer);
        File tDataFile=new File(tSaveDir,tFileName);
        File tDataFileTmp=new File(tSaveDir,tFileName+".tmp");
        FileOutputStream tFOStream=null;
        try{
            tFOStream=FileUtil.openOutputStream(tDataFileTmp,false);
            ClassUtil.invokeStaticMethod(IBNMSUtil.method_NBTCompressedStreamTools_writeCompressed,new Object[]{tNBTTag,tFOStream});
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
    public boolean loadFrom(CommandSender pSender,File pLoadDir,Player pToPlayer,OfflinePlayer pFromPlayer) throws IOException{
        if(!(this instanceof DB_VanillaData)||!this.mEnable)
            return false;
        
        File tLoadDir=pLoadDir==null?this.mDataDir:pLoadDir;
        File tLoadFile=new File(tLoadDir,this.getPlayerFileName(pFromPlayer));
        if(!tLoadFile.isFile()){
            InvBack.warn(pSender,this.mPlugin.C("MsgModelBackupDataNotFoundPlayer",new String[]{"%model%","%player%"},this.getDescription(),pFromPlayer.getName()));
        }else{
            this.loadDataFromStream(pSender,new FileInputStream(tLoadFile),pToPlayer);
        }
        return true;
    }

    @Override
    public boolean saveToMemoryMap(CommandSender pSender,Player pFromPlayer,Map<Object,Object> pMemoryData){
        pMemoryData.put(this.getClass(),this.saveDataToNBT(pFromPlayer));
        return true;
    }

    @Override
    public boolean loadFromMemoryMap(CommandSender pSender,Player pToPlayer,Map<Object,Object> pMemoryData){
        Object tNBTTag=pMemoryData.get(this.getClass());
        if(tNBTTag!=null){
            this.loadDataFromNBT(pToPlayer,tNBTTag);
        }
        return true;
    }

    @Override
    public boolean copy(CommandSender pSender,Player pFromPlayer,Player pToPlayer){
        if(!(this instanceof DB_VanillaData)||!this.mEnable)
            return false;
        
        Object tNBTTag=this.saveDataToNBT(pFromPlayer);
        if(tNBTTag==null){
            tNBTTag=NBTUtil.newNBTTagCompound();
        }
        this.loadDataFromNBT(pToPlayer,tNBTTag);
        return true;
    }

}
