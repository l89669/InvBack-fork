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

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.util.ClassUtil;
import cc.bukkitPlugin.util.Log;
import cc.bukkitPlugin.util.NMSUtil;

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
            this.method_PlayerDataController_getPlayerData=ClassUtil.getMethod(tClazz,"getPlayerData",NMSUtil.clazz_EntityPlayer);
            Field tField=ClassUtil.getField(tClazz,tClazz,-1).get(0);
            this.value_PlayerDataController_instance=ClassUtil.getFieldValue(null,tField);
            if(this.value_PlayerDataController_instance==null){
                this.value_PlayerDataController_instance=ClassUtil.getInstance(tClazz);
                tField.set(null,this.value_PlayerDataController_instance);
            }
                
            tClazz=Class.forName("noppes.npcs.controllers.PlayerData");
            this.method_PlayerData_getNBT=ClassUtil.getMethod(tClazz,"getNBT");
            Method tMethod=null;
            if(ClassUtil.isMethodExist(tClazz,"readNBT",void.class,NMSUtil.clazz_NBTTagCompound)){
                this.method_PlayerData_readNBT=ClassUtil.getMethod(tClazz,"readNBT",NMSUtil.clazz_NBTTagCompound);
            }else{
                this.method_PlayerData_readNBT=ClassUtil.getMethod(tClazz,"setNBT",NMSUtil.clazz_NBTTagCompound);
            }
            
            for(Field sField : tClazz.getDeclaredFields()){
                if(ClassUtil.isMethodExist(sField.getType(),"loadNBTData")){
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
        return ClassUtil.invokeMethod(this.value_PlayerDataController_instance,this.method_PlayerDataController_getPlayerData,tNMSPlayer);
    }

    @Override
    protected Object saveDataToNBT(Player pFromPlayer){
        Object tPlayerData=this.getPlayerData(pFromPlayer);
        return ClassUtil.invokeMethod(tPlayerData,this.method_PlayerData_getNBT);
    }

    @Override
    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        this.reset(null,pToPlayer);
        Object tPlayerData=this.getPlayerData(pToPlayer);
        ClassUtil.invokeMethod(tPlayerData,this.method_PlayerData_readNBT,pNBT);
    }
    
    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        if(!this.mEnable)
            return false;
        
        Object tPlayerData=this.getPlayerData(pTargetPlayer);
        for(Map.Entry<Field,Collection<Field>> sEntry : this.mFields.entrySet()){
            Object tSubValue=ClassUtil.getFieldValue(tPlayerData,sEntry.getKey());
            for(Field sField : sEntry.getValue()){
                ((Collection<?>)ClassUtil.getFieldValue(tSubValue,sField)).clear();
            }
        }
        return true;
    }


}
