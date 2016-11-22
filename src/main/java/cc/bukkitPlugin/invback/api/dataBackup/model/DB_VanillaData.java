package cc.bukkitPlugin.invback.api.dataBackup.model;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.invback.util.IBNMSUtil;
import cc.bukkitPlugin.util.ClassUtil;
import cc.bukkitPlugin.util.nbt.NBTUtil;

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
        Object tNBTTagCompound=ClassUtil.getInstance(IBNMSUtil.clazz_NBTTagCompound);
        Object tNMSPlayer=ClassUtil.invokeMethod(pFromPlayer,IBNMSUtil.method_CraftPlayer_getHandle);
        ClassUtil.invokeMethod(tNMSPlayer,IBNMSUtil.method_EntityPlayer_writeToNBT,tNBTTagCompound);
        return tNBTTagCompound;
    }

    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        Object tNMSPlayer=ClassUtil.invokeMethod(pToPlayer,IBNMSUtil.method_CraftPlayer_getHandle);
        ClassUtil.invokeMethod(tNMSPlayer,IBNMSUtil.method_EntityPlayer_readFromNBT,pNBT);
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        this.loadDataFromNBT(pTargetPlayer,NBTUtil.newNBTTagCompound());
        return true;
    }

}
