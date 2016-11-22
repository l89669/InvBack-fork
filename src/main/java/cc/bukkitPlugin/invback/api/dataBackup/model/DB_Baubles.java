package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.lang.reflect.Method;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.util.ClassUtil;
import cc.bukkitPlugin.util.NMSUtil;
import cc.bukkitPlugin.util.nbt.NBTUtil;

public class DB_Baubles extends ADB_CompressNBT{

    private Method method_BaublesApi_getBaubles=null;
    private Method method_InventoryBaubles_readNBT=null;
    private Method method_InventoryBaubles_saveNBT=null;

    public DB_Baubles(InvBack pPlugin){
        super(pPlugin,"饰品数据备份");

        this.mFileNameMode=FileNameMode.NAME;
    }

    @Override
    public boolean init(){
        try{
            Class<?> tClazz=null;
            Class.forName("baubles.common.Baubles");
            tClazz=Class.forName("baubles.api.BaublesApi");
            this.method_BaublesApi_getBaubles=tClazz.getMethod("getBaubles",NMSUtil.clazz_EntityPlayer);
            tClazz=Class.forName("baubles.common.container.InventoryBaubles");
            this.method_InventoryBaubles_readNBT=ClassUtil.getMethod(tClazz,"readNBT",NMSUtil.clazz_NBTTagCompound);
            this.method_InventoryBaubles_saveNBT=ClassUtil.getMethod(tClazz,"saveNBT",NMSUtil.clazz_NBTTagCompound);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                InvBack.severe("模块 "+this.getDescription()+" 初始化时发生了错误",exp);
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "BaublesDataBackup";
    }

    @Override
    protected String getFileSuffix(){
        return "baub";
    }

    protected Object saveDataToNBT(Player pFromPlayer){
        Object tNMSInv=this.getBaublesNMSInv(pFromPlayer);
        Object tNBTTag=NBTUtil.newNBTTagCompound();
        ClassUtil.invokeMethod(tNMSInv,this.method_InventoryBaubles_saveNBT,tNBTTag);
        return tNBTTag;
    }

    protected void loadDataFromNBT(Player pToPlayer,Object pNBTTag){
        Object tNMSInv=this.getBaublesNMSInv(pToPlayer);
        this.clearInv(tNMSInv);
        ClassUtil.invokeMethod(tNMSInv,this.method_InventoryBaubles_readNBT,pNBTTag);
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        if(!this.mEnable)
            return false;
        
        this.clearInv(this.getBaublesNMSInv(pTargetPlayer));
        return true;
    }

    protected Object getBaublesNMSInv(Player pPlayer){
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        return ClassUtil.invokeStaticMethod(this.method_BaublesApi_getBaubles,tNMSPlayer);
    }
    
    private void clearInv(Object pInv){
        Inventory tInv=(Inventory)ClassUtil.getInstance(NMSUtil.clazz_CraftInventory,NMSUtil.clazz_IInventory,pInv);
        for(int i=0;i<tInv.getSize();i++){
            tInv.setItem(i,null);
        }
    }

}
