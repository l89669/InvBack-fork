package cc.bukkitPlugin.invback.api.dataBackup.model;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.invback.util.IBNMSUtil;
import cc.commons.util.ClassUtil;

public class DB_VanillaData extends ADB_CompressNBT{

    public DB_VanillaData(InvBack pPlugin){
        super(pPlugin,"原版数据备份");
        
        this.mFileNameMode=FileNameMode.UUID;
    }

    @Override
    public boolean init(){
        return true;
    }

    @Override
    public String getName(){
        return "VanillaDataBackup";
    }
    
    @Override
    protected String getFileSuffix(){
        return "dat";
    }

    protected Object saveDataToNBT(Player pFromPlayer){
        Object tNBTTagCompound=ClassUtil.getInstance(NBTUtil.clazz_NBTTagCompound);
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pFromPlayer);
        ClassUtil.invokeMethod(IBNMSUtil.method_EntityPlayer_writeToNBT,tNMSPlayer,tNBTTagCompound);
        return tNBTTagCompound;
    }

    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pToPlayer);
        ClassUtil.invokeMethod(IBNMSUtil.method_EntityPlayer_readFromNBT,tNMSPlayer,pNBT);
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        this.loadDataFromNBT(pTargetPlayer,NBTUtil.newNBTTagCompound());
        return true;
    }

}
