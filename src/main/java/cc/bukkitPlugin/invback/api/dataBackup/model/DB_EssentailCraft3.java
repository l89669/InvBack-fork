package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.lang.reflect.Method;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.invback.InvBack;
import cc.commons.util.ClassUtil;

public class DB_EssentailCraft3 extends ADB_CompressNBT{

    private Method method_ApiCore_getPlayerData;
    private Method method_IPlayerData_readFromNBTTagCompound;
    private Method method_IPlayerData_writeToNBTTagCompound;

    public DB_EssentailCraft3(InvBack pPlugin){
        super(pPlugin,"源质魔法数据备份");
    }

    @Override
    public boolean init(){
        Class<?> tClazz;
        try{
            Class.forName("ec3.common.mod.EssentialCraftCore");
            tClazz=Class.forName("ec3.api.ApiCore");
            this.method_ApiCore_getPlayerData=ClassUtil.getMethod(tClazz,"getPlayerData",NMSUtil.clazz_EntityPlayer);
            tClazz=Class.forName("ec3.api.IPlayerData");
            this.method_IPlayerData_readFromNBTTagCompound=ClassUtil.getMethod(tClazz,"readFromNBTTagCompound",NBTUtil.clazz_NBTTagCompound);
            this.method_IPlayerData_writeToNBTTagCompound=ClassUtil.getMethod(tClazz,"writeToNBTTagCompound",NBTUtil.clazz_NBTTagCompound);
        }catch(Throwable exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDescription()+" 初始化时发生了错误",exp);
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "EC3DataBackup";
    }

    @Override
    protected String getFileSuffix(){
        return "ecdat";
    }

    private Object getPlayerData(Player pPlayer){
        return ClassUtil.invokeMethod(this.method_ApiCore_getPlayerData,null,NMSUtil.getNMSPlayer(pPlayer));
    }

    @Override
    protected Object saveDataToNBT(Player pFromPlayer){
        Object tSaveTag=NBTUtil.newNBTTagCompound();
        ClassUtil.invokeMethod(this.method_IPlayerData_writeToNBTTagCompound,this.getPlayerData(pFromPlayer),tSaveTag);
        return tSaveTag;
    }

    @Override
    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        ClassUtil.invokeMethod(this.method_IPlayerData_readFromNBTTagCompound,this.getPlayerData(pToPlayer),pNBT);
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        this.loadDataFromNBT(pTargetPlayer,NBTUtil.newNBTTagCompound());
        return true;
    }

}
