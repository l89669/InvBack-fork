package cc.bukkitPlugin.invback.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;

public class IBNMSUtil extends NBTUtil{

    public static final Method method_NBTCompressedStreamTools_readCompressed;
    public static final Method method_NBTCompressedStreamTools_writeCompressed;

    public static final Method method_EntityPlayer_readFromNBT;
    public static final Method method_EntityPlayer_writeToNBT;

    static{
        String packetPath=ClassUtil.getClassPacket(clazz_NBTTagCompound.getName());
        Class<?> NBTCompressedStreamTools=null;
        if(ClassUtil.isClassLoaded(packetPath+"CompressedStreamTools")) // kc
            NBTCompressedStreamTools=ClassUtil.getClass(packetPath+"CompressedStreamTools");
        else NBTCompressedStreamTools=ClassUtil.getClass(packetPath+"NBTCompressedStreamTools"); // bukkit
        // NBT-END

        method_NBTCompressedStreamTools_readCompressed=MethodUtil.getUnknowMethod(NBTCompressedStreamTools,
                clazz_NBTTagCompound,
                InputStream.class,
                true).get(0);
        method_NBTCompressedStreamTools_writeCompressed=MethodUtil.getUnknowMethod(NBTCompressedStreamTools,
                void.class,
                new Class<?>[]{clazz_NBTTagCompound,OutputStream.class},
                true).get(0);

        // Entity readFromNBT>>InvBack
        Class<?> clazz_EntityZombie=null;
        if(ClassUtil.isClassLoaded(packetPath+"EntityZombie")){
            clazz_EntityZombie=ClassUtil.getClass(packetPath+"EntityZombie");
        }else{
            clazz_EntityZombie=ClassUtil.getClass("net.minecraft.entity.monster.EntityZombie");
        }
        // 获取世界实例
        World tWorld=Bukkit.getWorlds().get(0);
        Method tMethod=MethodUtil.getMethod(tWorld.getClass(),"getHandle",true);
        Object tNMSWorld=MethodUtil.invokeMethod(tMethod,tWorld);
        Object tObj_EntityZombie=ClassUtil.newInstance(clazz_EntityZombie,tMethod.getReturnType(),tNMSWorld);
        Object tObj_NBTTagCompound=NBTUtil.newNBTTagCompound();
        ArrayList<Method> tms=MethodUtil.getUnknowMethod(clazz_EntityZombie,void.class,clazz_NBTTagCompound,true);
        int readMethodPos=0;
        MethodUtil.invokeMethod(tms.get(0),tObj_EntityZombie,tObj_NBTTagCompound);
        if(!NBTUtil.getNBTTagCompoundValue(tObj_NBTTagCompound).isEmpty()){
            readMethodPos=1;
        }else{
            readMethodPos=0;
        }
        method_EntityPlayer_readFromNBT=MethodUtil.getMethod(NMSUtil.clazz_EntityPlayer,tms.get(readMethodPos).getName(),clazz_NBTTagCompound,false);
        method_EntityPlayer_writeToNBT=MethodUtil.getMethod(NMSUtil.clazz_EntityPlayer,tms.get(1-readMethodPos).getName(),clazz_NBTTagCompound,false);
        // Entity readFromNBT-END
    }

}
