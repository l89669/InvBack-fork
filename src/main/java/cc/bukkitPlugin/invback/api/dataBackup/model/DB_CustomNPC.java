package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class DB_CustomNPC extends ADB_CompressNBT{

    private Object value_PlayerDataController_instance;
    private Method method_PlayerDataController_getPlayerData;
    private Method method_PlayerData_getNBT;
    private Method method_PlayerData_readNBT;
    private HashMap<Field,Collection<Field>> mFields=new HashMap<>();

    public DB_CustomNPC(InvBack pPlugin){
        super(pPlugin,"自定义NPC数据备份");

        this.mDataPath="world"+File.separator+"customnpcs"+File.separator+"playerdata"+File.separator;
        this.mFileNameMode=FileNameMode.NAME;
    }

    @Override
    public boolean init(){
        Class<?> tClazz;
        try{
            Class.forName("noppes.npcs.CustomNpcs");
            tClazz=Class.forName("noppes.npcs.controllers.PlayerDataController");
            this.method_PlayerDataController_getPlayerData=MethodUtil.getMethod(tClazz,"getPlayerData",NMSUtil.clazz_EntityPlayer,true);
            Field tField=FieldUtil.getField(tClazz,tClazz,-1,true).get(0);
            this.value_PlayerDataController_instance=FieldUtil.getStaticFieldValue(tField);
            if(this.value_PlayerDataController_instance==null){
                this.value_PlayerDataController_instance=ClassUtil.newInstance(tClazz);
                tField.set(null,this.value_PlayerDataController_instance);
            }

            tClazz=Class.forName("noppes.npcs.controllers.PlayerData");
            this.method_PlayerData_getNBT=MethodUtil.getMethod(tClazz,"getNBT",true);

            if(MethodUtil.isMethodExist(tClazz,"readNBT",NBTUtil.clazz_NBTTagCompound,true)){
                this.method_PlayerData_readNBT=MethodUtil.getMethod(tClazz,"readNBT",NBTUtil.clazz_NBTTagCompound,true);
            }else{
                this.method_PlayerData_readNBT=MethodUtil.getMethod(tClazz,"setNBT",NBTUtil.clazz_NBTTagCompound,true);
            }

            for(Field sField : tClazz.getDeclaredFields()){
                if(MethodUtil.isMethodExist(sField.getType(),"loadNBTData",true)){
                    ArrayList<Field> tFields=new ArrayList<>();
                    for(Field ssField : sField.getType().getDeclaredFields()){
                        if(ssField.getType().isAssignableFrom(Collection.class)){
                            tFields.add(ssField);
                        }
                    }
                    if(!tFields.isEmpty()){
                        this.mFields.put(sField,tFields);
                    }
                }
            }
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDescription()+" 初始化时发生了错误",exp);
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "CustomNPCDataBackup";
    }

    @Override
    protected String getFileSuffix(){
        return "dat";
    }

    private Object getPlayerData(Player pPlayer){
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        return MethodUtil.invokeMethod(this.method_PlayerDataController_getPlayerData,this.value_PlayerDataController_instance,tNMSPlayer);
    }

    @Override
    protected Object saveDataToNBT(Player pFromPlayer){
        Object tPlayerData=this.getPlayerData(pFromPlayer);
        return MethodUtil.invokeMethod(this.method_PlayerData_getNBT,tPlayerData);
    }

    @Override
    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        this.reset(null,pToPlayer);
        Object tPlayerData=this.getPlayerData(pToPlayer);
        MethodUtil.invokeMethod(this.method_PlayerData_readNBT,tPlayerData,pNBT);
    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        if(!this.mEnable)
            return false;

        Object tPlayerData=this.getPlayerData(pTargetPlayer);
        for(Map.Entry<Field,Collection<Field>> sEntry : this.mFields.entrySet()){
            Object tSubValue=FieldUtil.getFieldValue(sEntry.getKey(),tPlayerData);
            for(Field sField : sEntry.getValue()){
                ((Collection<?>)FieldUtil.getFieldValue(sField,tSubValue)).clear();
            }
        }
        return true;
    }

}
