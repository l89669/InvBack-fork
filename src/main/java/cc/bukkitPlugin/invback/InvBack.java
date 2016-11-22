package cc.bukkitPlugin.invback;

import cc.bukkitPlugin.invback.api.dataBackup.DataBackupAPI;
import cc.bukkitPlugin.invback.command.CommandExc;
import cc.bukkitPlugin.invback.listener.PlayerListner;
import cc.bukkitPlugin.invback.manager.ConfigManager;
import cc.bukkitPlugin.invback.manager.DataManager;
import cc.bukkitPlugin.invback.manager.LangManager;
import cc.bukkitPlugin.invback.task.TaskExec;
import cc.bukkitPlugin.util.plugin.ABukkitPlugin;

public class InvBack extends ABukkitPlugin<InvBack>{

    private CommandExc mCmdExc;
    private DataManager mDataManager;
    private TaskExec mTaskExec;
    
    public InvBack(){
        super("InvBack");
    }
    
    @Override
    public void onEnable(){
        //注册管理器
        this.setLangManager(new LangManager(this));
        this.registerManager(this.getLangManager());
        this.setConfigManager(new ConfigManager(this));
        this.registerManager(this.getConfigManager());
        this.mDataManager=new DataManager(this);
        this.registerManager(this.mDataManager);
        this.mTaskExec=new TaskExec(this);
        this.registerManager(DataBackupAPI.getInstance());
        //注册监听器
        new PlayerListner(this);
        //绑定命令执行器
        this.mCmdExc=new CommandExc(this);
        //初始化管理器并载入配置
        this.reloadPlugin(null);
    }
    
    public DataManager getDataManager(){
        return this.mDataManager;
    }
    
    public TaskExec getTaskExc(){
        return this.mTaskExec;
    }
    
    @Override
    public ConfigManager getConfigManager(){
        return (ConfigManager)super.getConfigManager();
    }

    @Override
    public LangManager getLangManager(){
        return (LangManager)super.getLangManager();
    }
    
    public static InvBack getInstance(){
        return ABukkitPlugin.getInstance(InvBack.class);
    }

}
