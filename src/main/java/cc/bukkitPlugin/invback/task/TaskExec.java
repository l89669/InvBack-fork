package cc.bukkitPlugin.invback.task;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.manager.DataManager;
import cc.bukkitPlugin.util.ClassUtil;
import cc.bukkitPlugin.util.config.CommentedYamlConfig;
import cc.bukkitPlugin.util.plugin.INeedClose;
import cc.bukkitPlugin.util.plugin.INeedConfig;
import cc.bukkitPlugin.util.plugin.INeedReload;

public class TaskExec extends TimerTask implements INeedConfig,INeedClose,INeedReload{

    private InvBack mPlugin;
    /**最后一次运行的时间*/
    private long mLastRunUpTime=0;
    /**备份时间间隔(秒)*/
    private int mBackupInterval=900;
    /**任务执行定时器*/
    private Timer mTimer=null;

    public TaskExec(InvBack pPlugin){
        this.mPlugin=pPlugin;

        this.mPlugin.registerConfigModel(this);
        this.mPlugin.registerCloseModel(this);
        this.mPlugin.registerReloadModel(this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this.mPlugin,new Runnable(){

            @Override
            public void run(){
                if(TaskExec.this.mTimer==null)
                    return;

                if(TaskExec.this.isTaskDelay())
                    TaskExec.this.rerunTask();
            }
        },1200,1200);
    }

    @Override
    public void disable(){
        this.stopTask();
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        synchronized(this){
            if(this.mTimer==null){
                this.mTimer=new Timer();
                this.mTimer.schedule(this,5000,5000); // 100tick检查一次任务是否需要进行
            }
        }
        return true;
    }
    
    @Override
    public void setConfig(CommandSender pSender){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        this.mBackupInterval=tConfig.getInt("BackupInterval",this.mBackupInterval);
        if(this.mBackupInterval<=0)
            this.mBackupInterval=900;
    }

    public void stopTask(){
        if(this.mTimer==null)
            return;

        this.mTimer.cancel();
        this.mTimer=null;
        try{
            for(Field sField : TimerTask.class.getDeclaredFields()){
                if(((sField.getModifiers()&(Modifier.FINAL+Modifier.STATIC))==0)&&sField.getType()==int.class||sField.getType()==long.class){
                    ClassUtil.setFieldValue(this,sField,0);
                }
            }
        }catch(Throwable exp){
            InvBack.severe("重置任务状态时发生了错误: "+exp.getMessage(),exp);
        }
    }

    public boolean shouldRunTask(){
        synchronized(this){
            return (InvBack.getJVMTime()-this.mLastRunUpTime)/1000>=this.mBackupInterval;
        }
    }

    public boolean isTaskDelay(){
        synchronized(this){
            return (InvBack.getJVMStartTime()+InvBack.getJVMUpTime()-this.mLastRunUpTime)/1000>=this.mBackupInterval*1.5;
        }
    }

    public boolean isNewDay(){
        synchronized(this){
            return new Date(InvBack.getJVMTime()).getDate()!=new Date(this.mLastRunUpTime).getDate();
        }
    }

    public boolean rerunTask(){
        if(!this.isTaskDelay())
            return false;

        this.stopTask();
        this.backupPlayerData();
        this.mTimer=new Timer();
        this.mTimer.schedule(this,1000,5000); // 100tick检查一次任务是否需要进行
        return true;
    }

    @Override
    public void run(){
        if(!shouldRunTask())
            return;

        this.backupPlayerData();
    }

    public void backupPlayerData(){
        DataManager tDataMan=this.mPlugin.getManager(DataManager.class);
        boolean doClean=this.isNewDay();
        this.mLastRunUpTime=InvBack.getJVMTime();
        try{
            tDataMan.backup(null);
        }catch(Throwable exp){
            InvBack.severe(this.mPlugin.C("MsgErrorOnBackupPlayeData")+": "+exp.getMessage(),exp);
        }
        if(doClean){
            tDataMan.clearExpriedBackup();
        }
    }

}
