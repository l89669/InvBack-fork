package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.invback.util.IBNMSUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ResearchManager;

public class DB_ThaumCraft extends ADB_CompressNBT{

    public static final String TAG_Eldritch="Thaumcraft.eldritch";
    public static final String TAG_Eldritch_Counter="Thaumcraft.eldritch.counter";
    public static final String TAG_Eldritch_Sticky="Thaumcraft.eldritch.sticky";
    public static final String TAG_Eldritch_Temp="Thaumcraft.eldritch.temp";

    private Method method_ResearchManager_saveAspectNBT;
    private Method method_ResearchManager_saveResearchNBT;
    private Method method_ResearchManager_saveScannedNBT;
    private Method method_ResearchManager_loadAspectNBT;
    private Method method_ResearchManager_loadResearchNBT;
    private Method method_ResearchManager_loadScannedNBT;
    private Method method_ResearchManager_completeResearch;

    public DB_ThaumCraft(InvBack pPlugin){
        super(pPlugin,"神秘时代数据备份");

        this.mFileNameMode=FileNameMode.NAME;
    }

    private void checkMethod(Class<?> pClazz,String[] pMethods,Class<?>...pArgsClazz) throws NoSuchMethodException{
        for(String sMethod : pMethods)
            pClazz.getDeclaredMethod(sMethod,pArgsClazz);
    }

    @Override
    public boolean init(){
        try{
            Class<?> tClazz=null;
            Class.forName("thaumcraft.common.Thaumcraft");
            Class.forName("thaumcraft.common.CommonProxy");

            tClazz=Class.forName("thaumcraft.common.lib.research.PlayerKnowledge");
            this.checkMethod(tClazz,new String[]{"getWarpCounter","getWarpPerm","getWarpSticky","getWarpTemp","wipePlayerKnowledge"},String.class);

            tClazz=Class.forName("thaumcraft.common.lib.research.ResearchManager");
            this.method_ResearchManager_loadAspectNBT=tClazz.getDeclaredMethod("loadAspectNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_loadResearchNBT=tClazz.getDeclaredMethod("loadResearchNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_loadScannedNBT=tClazz.getDeclaredMethod("loadScannedNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveAspectNBT=tClazz.getDeclaredMethod("saveAspectNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveResearchNBT=tClazz.getDeclaredMethod("saveResearchNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveScannedNBT=tClazz.getDeclaredMethod("saveScannedNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_completeResearch=tClazz.getDeclaredMethod("completeResearch",NMSUtil.clazz_EntityPlayer,String.class);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException)&&!(exp instanceof NoSuchMethodException))
                Log.severe("模块 "+this.getDescription()+" 初始化时发生了错误",exp);
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "ThaumDataBackup";
    }

    @Override
    protected String getFileSuffix(){
        return "thaum";
    }

    @Override
    protected Object saveDataToNBT(Player pFromPlayer){
        Object tNBTTagCompound=ClassUtil.newInstance(IBNMSUtil.clazz_NBTTagCompound);
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pFromPlayer);
        MethodUtil.invokeMethod(this.method_ResearchManager_saveAspectNBT,null,new Object[]{tNBTTagCompound,tNMSPlayer});
        MethodUtil.invokeMethod(this.method_ResearchManager_saveResearchNBT,null,new Object[]{tNBTTagCompound,tNMSPlayer});
        MethodUtil.invokeMethod(this.method_ResearchManager_saveScannedNBT,null,new Object[]{tNBTTagCompound,tNMSPlayer});

        String tPlayerName=pFromPlayer.getName();
        Map<String,Object> tTagMap=NBTUtil.getNBTTagCompoundValue(tNBTTagCompound);
        tTagMap.put(TAG_Eldritch,ClassUtil.newInstance(NBTUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Counter,ClassUtil.newInstance(NBTUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpCounter(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Sticky,ClassUtil.newInstance(NBTUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Temp,ClassUtil.newInstance(NBTUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpTemp(tPlayerName)));

        return tNBTTagCompound;
    }

    @Override
    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        this.wipePlayerData(pToPlayer);
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pToPlayer);
        MethodUtil.invokeMethod(this.method_ResearchManager_loadAspectNBT,null,new Object[]{pNBT,tNMSPlayer});
        MethodUtil.invokeMethod(this.method_ResearchManager_loadResearchNBT,null,new Object[]{pNBT,tNMSPlayer});
        MethodUtil.invokeMethod(this.method_ResearchManager_loadScannedNBT,null,new Object[]{pNBT,tNMSPlayer});

        String tPlayerName=pToPlayer.getName();
        Map<String,Object> tTagMap=NBTUtil.getNBTTagCompoundValue(pNBT);

        Object tValue=tTagMap.get(TAG_Eldritch);
        if(NBTUtil.clazz_NBTTagInt.isInstance(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(tPlayerName,(int)FieldUtil.getFieldValue(NBTUtil.field_NBTTagInt_value,tValue));
        }
        tValue=tTagMap.get(TAG_Eldritch_Counter);
        if(NBTUtil.clazz_NBTTagInt.isInstance(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpCounter(tPlayerName,(int)FieldUtil.getFieldValue(NBTUtil.field_NBTTagInt_value,tValue));
        }
        tValue=tTagMap.get(TAG_Eldritch_Sticky);
        if(NBTUtil.clazz_NBTTagInt.isInstance(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpSticky(tPlayerName,(int)FieldUtil.getFieldValue(NBTUtil.field_NBTTagInt_value,tValue));
        }
        tValue=tTagMap.get(TAG_Eldritch_Temp);
        if(NBTUtil.clazz_NBTTagInt.isInstance(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpTemp(tPlayerName,(int)FieldUtil.getFieldValue(NBTUtil.field_NBTTagInt_value,tValue));
        }

    }

    @Override
    public boolean reset(CommandSender pSender,Player pTargetPlayer){
        if(!this.mEnable)
            return false;

        this.wipePlayerData(pTargetPlayer);

        StackTraceElement[] tStack=new RuntimeException().getStackTrace();
        if(!tStack[1].getClassName().equals(this.getClass().getName())){
            ResearchManager tMan=Thaumcraft.proxy.getResearchManager();
            Object tNMSPlayer=NMSUtil.getNMSPlayer(pTargetPlayer);
            Collection<ResearchCategoryList> tRCs=ResearchCategories.researchCategories.values();
            for(ResearchCategoryList sRCL : tRCs){
                Collection<ResearchItem> tRIs=sRCL.research.values();
                for(ResearchItem sRI : tRIs){
                    if(sRI.isAutoUnlock()){
                        MethodUtil.invokeMethod(this.method_ResearchManager_completeResearch,tMan,new Object[]{tNMSPlayer,sRI.key});
                    }
                }
            }
        }
        return true;
    }

    protected void wipePlayerData(Player pPlayer){
        Thaumcraft.proxy.getPlayerKnowledge().wipePlayerKnowledge(pPlayer.getName());
    }

}
