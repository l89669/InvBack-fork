package cc.bukkitPlugin.invback.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import cc.bukkitPlugin.util.ClassUtil;
import cc.bukkitPlugin.util.NMSUtil;
import cc.bukkitPlugin.util.nbt.NBTUtil;

public class IBNMSUtil extends NMSUtil{

    public static final Method method_NBTCompressedStreamTools_readCompressed;
    public static final Method method_NBTCompressedStreamTools_writeCompressed;

    public static final Method method_EntityPlayer_readFromNBT;
    public static final Method method_EntityPlayer_writeToNBT;

    static{
        String packetPath=NMSUtil.getClassPrefix(clazz_NBTTagCompound.getName());
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
        Object nmsWorld=ClassUtil.getFieldValue(Bukkit.getWorlds().get(0),"world");
        Object tObj_EntityZombie=ClassUtil.getInstance(clazz_EntityZombie,clazz_NMSWorld,nmsWorld);
        Object tObj_NBTTagCompound=ClassUtil.getInstance(clazz_NBTTagCompound);
        ArrayList<Method> tms=ClassUtil.getUnknowMethod(clazz_EntityZombie,void.class,clazz_NBTTagCompound);
        int readMethodPos=0;
        ClassUtil.invokeMethod(tObj_EntityZombie,tms.get(0),tObj_NBTTagCompound);
        if(!NBTUtil.getNBTTagMapFromTag(tObj_NBTTagCompound).isEmpty())
            readMethodPos=1;
        else readMethodPos=0;
        method_EntityPlayer_readFromNBT=ClassUtil.getMethod(clazz_EntityPlayer,tms.get(readMethodPos).getName(),clazz_NBTTagCompound);
        method_EntityPlayer_writeToNBT=ClassUtil.getMethod(clazz_EntityPlayer,tms.get(1-readMethodPos).getName(),clazz_NBTTagCompound);
        // Entity readFromNBT-END
    }

}
