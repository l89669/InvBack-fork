package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.lang.reflect.Method;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.invback.InvBack;
import cc.commons.util.reflect.MethodUtil;

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
            this.method_ApiCore_getPlayerData=MethodUtil.getMethod(tClazz,"getPlayerData",NMSUtil.clazz_EntityPlayer,true);
            tClazz=Class.forName("ec3.api.IPlayerData");
            this.method_IPlayerData_readFromNBTTagCompound=MethodUtil.getMethod(tClazz,"readFromNBTTagCompound",NBTUtil.clazz_NBTTagCompound,true);
            this.method_IPlayerData_writeToNBTTagCompound=MethodUtil.getMethod(tClazz,"writeToNBTTagCompound",NBTUtil.clazz_NBTTagCompound,true);
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
        return MethodUtil.invokeMethod(this.method_ApiCore_getPlayerData,null,NMSUtil.getNMSPlayer(pPlayer));
    }

    @Override
    protected Object saveDataToNBT(Player pFromPlayer){
        Object tSaveTag=NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(this.method_IPlayerData_writeToNBTTagCompound,this.getPlayerData(pFromPlayer),tSaveTag);
        return tSaveTag;
    }

    @Override
    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        MethodUtil.invokeMethod(this.method_IPlayerData_readFromNBTTagCompound,this.getPlayerData(pToPlayer),pNBT);
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        this.loadDataFromNBT(pTargetPlayer,NBTUtil.newNBTTagCompound());
        return true;
    }

}
