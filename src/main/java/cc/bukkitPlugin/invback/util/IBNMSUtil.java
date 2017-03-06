package cc.bukkitPlugin.invback.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.util.ClassUtil;

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

        method_NBTCompressedStreamTools_readCompressed=ClassUtil.getUnknowMethod(NBTCompressedStreamTools,clazz_NBTTagCompound,InputStream.class).get(0);
        method_NBTCompressedStreamTools_writeCompressed=ClassUtil.getUnknowMethod(NBTCompressedStreamTools,void.class,new Class<?>[]{clazz_NBTTagCompound,OutputStream.class}).get(0);

        // Entity readFromNBT>>InvBack
        Class<?> clazz_EntityZombie=null;
        if(ClassUtil.isClassLoaded(packetPath+"EntityZombie"))
            clazz_EntityZombie=ClassUtil.getClass(packetPath+"EntityZombie");
        else clazz_EntityZombie=ClassUtil.getClass("net.minecraft.entity.monster.EntityZombie");
        // 获取世界实例
        World tWorld=Bukkit.getWorlds().get(0);
        Object tNMSWorld=ClassUtil.invokeMethod(tWorld.getClass(),tWorld,"getHandle");
        Object tObj_EntityZombie=ClassUtil.getInstance(clazz_EntityZombie,NMSUtil.clazz_NMSWorld,tNMSWorld);
        Object tObj_NBTTagCompound=ClassUtil.getInstance(clazz_NBTTagCompound);
        ArrayList<Method> tms=ClassUtil.getUnknowMethod(clazz_EntityZombie,void.class,clazz_NBTTagCompound);
        int readMethodPos=0;
        ClassUtil.invokeMethod(tms.get(0),tObj_EntityZombie,tObj_NBTTagCompound);
        if(!NBTUtil.getNBTTagCompoundValue(tObj_NBTTagCompound).isEmpty())
            readMethodPos=1;
        else readMethodPos=0;
        method_EntityPlayer_readFromNBT=ClassUtil.getMethod(NMSUtil.clazz_EntityPlayer,tms.get(readMethodPos).getName(),clazz_NBTTagCompound);
        method_EntityPlayer_writeToNBT=ClassUtil.getMethod(NMSUtil.clazz_EntityPlayer,tms.get(1-readMethodPos).getName(),clazz_NBTTagCompound);
        // Entity readFromNBT-END
    }

}
