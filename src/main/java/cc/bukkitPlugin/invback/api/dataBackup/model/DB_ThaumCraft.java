package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.invback.util.IBNMSUtil;
import cc.bukkitPlugin.util.ClassUtil;
import cc.bukkitPlugin.util.NMSUtil;
import cc.bukkitPlugin.util.nbt.NBTUtil;
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
            this.method_ResearchManager_loadAspectNBT=tClazz.getDeclaredMethod("loadAspectNBT",NMSUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_loadResearchNBT=tClazz.getDeclaredMethod("loadResearchNBT",NMSUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_loadScannedNBT=tClazz.getDeclaredMethod("loadScannedNBT",NMSUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveAspectNBT=tClazz.getDeclaredMethod("saveAspectNBT",NMSUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveResearchNBT=tClazz.getDeclaredMethod("saveResearchNBT",NMSUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveScannedNBT=tClazz.getDeclaredMethod("saveScannedNBT",NMSUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_completeResearch=tClazz.getDeclaredMethod("completeResearch",NMSUtil.clazz_EntityPlayer,String.class);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException)&&!(exp instanceof NoSuchMethodException))
                InvBack.severe("模块 "+this.getDescription()+" 初始化时发生了错误",exp);
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
        Object tNBTTagCompound=ClassUtil.getInstance(IBNMSUtil.clazz_NBTTagCompound);
        Object tNMSPlayer=ClassUtil.invokeMethod(pFromPlayer,IBNMSUtil.method_CraftPlayer_getHandle);
        ClassUtil.invokeStaticMethod(this.method_ResearchManager_saveAspectNBT,new Object[]{tNBTTagCompound,tNMSPlayer});
        ClassUtil.invokeStaticMethod(this.method_ResearchManager_saveResearchNBT,new Object[]{tNBTTagCompound,tNMSPlayer});
        ClassUtil.invokeStaticMethod(this.method_ResearchManager_saveScannedNBT,new Object[]{tNBTTagCompound,tNMSPlayer});

        String tPlayerName=pFromPlayer.getName();
        Map<String,Object> tTagMap=NBTUtil.getNBTTagMapFromTag(tNBTTagCompound);
        tTagMap.put(TAG_Eldritch,ClassUtil.getInstance(NMSUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Counter,ClassUtil.getInstance(NMSUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpCounter(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Sticky,ClassUtil.getInstance(NMSUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Temp,ClassUtil.getInstance(NMSUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpTemp(tPlayerName)));

        return tNBTTagCompound;
    }

    @Override
    protected void loadDataFromNBT(Player pToPlayer,Object pNBT){
        this.wipePlayerData(pToPlayer);
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pToPlayer);
        ClassUtil.invokeStaticMethod(this.method_ResearchManager_loadAspectNBT,new Object[]{pNBT,tNMSPlayer});
        ClassUtil.invokeStaticMethod(this.method_ResearchManager_loadResearchNBT,new Object[]{pNBT,tNMSPlayer});
        ClassUtil.invokeStaticMethod(this.method_ResearchManager_loadScannedNBT,new Object[]{pNBT,tNMSPlayer});

        String tPlayerName=pToPlayer.getName();
        Map<String,Object> tTagMap=NBTUtil.getNBTTagMapFromTag(pNBT);

        Object tValue=tTagMap.get(TAG_Eldritch);
        if(NMSUtil.clazz_NBTTagInt.isInstance(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(tPlayerName,(int)ClassUtil.getFieldValue(tValue,NMSUtil.field_NBTTagInt_value));
        }
        tValue=tTagMap.get(TAG_Eldritch_Counter);
        if(NMSUtil.clazz_NBTTagInt.isInstance(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpCounter(tPlayerName,(int)ClassUtil.getFieldValue(tValue,NMSUtil.field_NBTTagInt_value));
        }
        tValue=tTagMap.get(TAG_Eldritch_Sticky);
        if(NMSUtil.clazz_NBTTagInt.isInstance(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpSticky(tPlayerName,(int)ClassUtil.getFieldValue(tValue,NMSUtil.field_NBTTagInt_value));
        }
        tValue=tTagMap.get(TAG_Eldritch_Temp);
        if(NMSUtil.clazz_NBTTagInt.isInstance(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpTemp(tPlayerName,(int)ClassUtil.getFieldValue(tValue,NMSUtil.field_NBTTagInt_value));
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
                        ClassUtil.invokeMethod(tMan,this.method_ResearchManager_completeResearch,new Object[]{tNMSPlayer,sRI.key});
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
