package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.invback.InvBack;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.ClassUtil;

public class DB_TConstruct extends ADB_CompressNBT{

    private Method method_TConstructAPI_getInventoryWrapper;
    private Method method_IPlayerExtendedInventoryWrapper_getKnapsackInventory;
    private Method method_IPlayerExtendedInventoryWrapper_getAccessoryInventory;
    private Method method_TPlayerStats_loadNBTData;
    private Method method_TPlayerStats_saveNBTData;

    public DB_TConstruct(InvBack pPlugin){
        super(pPlugin,"匠魂数据备份");
    }

    @Override
    public boolean init(){
        Class<?> tClazz=null;
        try{
            Class.forName("tconstruct.TConstruct");
            tClazz=Class.forName("tconstruct.api.TConstructAPI");
            method_TConstructAPI_getInventoryWrapper=tClazz.getMethod("getInventoryWrapper",NMSUtil.clazz_EntityPlayer);

            tClazz=method_TConstructAPI_getInventoryWrapper.getReturnType();
            method_IPlayerExtendedInventoryWrapper_getKnapsackInventory=tClazz.getMethod("getKnapsackInventory",NMSUtil.clazz_EntityPlayer);
            method_IPlayerExtendedInventoryWrapper_getAccessoryInventory=tClazz.getMethod("getAccessoryInventory",NMSUtil.clazz_EntityPlayer);

            tClazz=Class.forName("tconstruct.armor.player.TPlayerStats");
            ArrayList<Method> tMethods=ClassUtil.getUnknowMethod(tClazz,void.class,NBTUtil.clazz_NBTTagCompound);
            Object tObj=ClassUtil.getInstance(tClazz);
            Object tNBTTag=NBTUtil.newNBTTagCompound();
            int writeMethod=0;
            ClassUtil.invokeMethod(tMethods.get(writeMethod),tObj,tNBTTag);
            if(NBTUtil.getNBTTagCompoundValue(tNBTTag).isEmpty()){
                writeMethod=1;
            }
            this.method_TPlayerStats_saveNBTData=tMethods.get(writeMethod);
            this.method_TPlayerStats_loadNBTData=tMethods.get(1-writeMethod);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDescription()+" 初始化时发生了错误",exp);
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "TConstructDataBackup";
    }

    @Override
    public boolean addDefaultConfig(CommentedSection pSection){
        pSection.getParent().addDefaultComments(pSection.getName(),"此模块的数据由于是和玩家数据存储到一起的,所以无法禁用");
        pSection.addDefault("Description",this.mDescription,"模块描述,显示用");
        return true;
    }

    @Override
    public void reloadConfig(CommandSender pSender,CommentedSection pSection){
        if(pSection!=null){
            this.mDescription=pSection.getString("Description",this.mDescription);
        }
    }

    @Override
    protected String getFileSuffix(){
        return "dat";
    }

    private Object getPlayerData(Player pPlayer){
        return ClassUtil.invokeMethod(this.method_TConstructAPI_getInventoryWrapper,null,NMSUtil.getNMSPlayer(pPlayer));
    }

    @Override
    protected Object saveDataToNBT(Player pFromPlayer){
        return ClassUtil.invokeMethod(this.method_TPlayerStats_saveNBTData,this.getPlayerData(pFromPlayer),NBTUtil.newNBTTagCompound());
    }

    @Override
    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        this.reset(null,pToPlayer);
        ClassUtil.invokeMethod(this.method_TPlayerStats_loadNBTData,pNBT,this.getPlayerData(pToPlayer));
    }

    @Override
    public boolean backup(CommandSender pSender,File pTargetDir,boolean pEnableReplace) throws IOException{
        return false;
    }

    @Override
    public boolean saveTo(CommandSender pSender,File pSaveDir,Player pFromPlayer,OfflinePlayer pToPlayer) throws IOException{
        return false;
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pTargetPlayer);
        Object tPlayerData=ClassUtil.invokeMethod(this.method_TConstructAPI_getInventoryWrapper,null,tNMSPlayer);
        ClassUtil.invokeMethod(this.method_TPlayerStats_loadNBTData,tPlayerData,NBTUtil.newNBTTagCompound());
        HashSet<Object> NMSInvs=new HashSet<>();
        NMSInvs.add(ClassUtil.invokeMethod(method_IPlayerExtendedInventoryWrapper_getKnapsackInventory,tPlayerData,tNMSPlayer));
        NMSInvs.add(ClassUtil.invokeMethod(method_IPlayerExtendedInventoryWrapper_getAccessoryInventory,tPlayerData,tNMSPlayer));
        for(Object sNMSInv : NMSInvs){
            if(sNMSInv==null)
                continue;
            Inventory tInv=(Inventory)ClassUtil.getInstance(NMSUtil.clazz_CraftInventory,NMSUtil.clazz_IInventory,sNMSInv);
            for(int i=0;i<tInv.getSize();i++){
                tInv.setItem(i,null);
            }
        }
        return true;
    }

}
