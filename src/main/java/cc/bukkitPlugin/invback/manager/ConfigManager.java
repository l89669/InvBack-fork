package cc.bukkitPlugin.invback.manager;

import java.io.File;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.util.plugin.ABukkitPlugin;
import cc.bukkitPlugin.util.plugin.manager.fileManager.AConfigManager;

public class ConfigManager extends AConfigManager<InvBack>{

    private File mBackupDir=new File("");
    private File mServerDir=new File("");
    private FileNameMode mFileNameMode=FileNameMode.UUID;
    private boolean mKeepGameModeWhenRollBack=true;
    private boolean mRemoveDataWhenPlayerQuit=true;

    public ConfigManager(InvBack pPlugin){
        super(pPlugin,"1.1");
        this.mBackupDir=new File(pPlugin.getDataFolder(),"back"+File.separator);
        File tPluginDataFolder=this.mPlugin.getDataFolder().getAbsoluteFile();
        this.mServerDir=tPluginDataFolder.getParentFile().getParentFile();
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            ABukkitPlugin.severe(pSender,C("MsgErrorHappendWhenReloadConfig"));
            return false;
        }
        this.checkUpdate();
        this.addDefaults();

        /**给变量赋值*/
        this.mKeepGameModeWhenRollBack=this.mConfig.getBoolean("KeepGameModeWhenRollBack");
        this.mRemoveDataWhenPlayerQuit=this.mConfig.getBoolean("RemoveDataWhenPlayerQuit");
        this.mFileNameMode=FileNameMode.getMode(this.mConfig.getString("FileNameMode"),this.mFileNameMode);
        String tPath=this.mConfig.getString("BackupDir","back"+File.separator);
        this.mBackupDir=new File(tPath);
        if(!this.mBackupDir.isAbsolute())
            this.mBackupDir=new File(this.mPlugin.getDataFolder(),tPath);
        tPath=this.mConfig.getString("ServerDir",this.mServerDir.getAbsolutePath());
        this.mServerDir=new File(tPath);

        ABukkitPlugin.info(pSender,C("MsgSetServerDir")+this.mServerDir.getAbsolutePath());
        ABukkitPlugin.info(pSender,C("MsgSetDataBackupDir")+this.mBackupDir.getAbsolutePath());
        InvBack.info(pSender,C("MsgConfigReloaded"));
        return this.saveConfig(null);
    }

    protected boolean checkUpdate(){
        if(!super.checkUpdate())
            return false;

        String tVersion=this.mConfig.getString(SEC_CFG_VERSION,"1.0");
        if(tVersion.compareTo("1.0")==0){
            tVersion="1.1";
            this.mConfig.remove("PlayerDataDir");
            this.mConfig.remove("FileNameMode");
            this.mConfig.remove("ClearMemoryDataWherPlayerQuit");
        }
        this.mConfig.set(SEC_CFG_VERSION,this.mVersion);
        return true;
    }

    public void addDefaults(){
        super.addDefaults();
        this.mConfig.addDefault("BackupDir","back"+File.separator,"玩家个人数据备份位置","相对路径默认在插件文件夹下,也可以设置绝对路径");
        this.mConfig.addDefault("BackupInterval",900,"多少秒备份一次玩家数据");
        this.mConfig.addDefault("BackupExpriedDays",7,"玩家备份数据最长保留天数");
        this.mConfig.addDefault("KeepGameModeWhenRollBack",true,"在使用copy,set,rollback命令时保持还原数据前的游戏模式");
        this.mConfig.addDefault("ReplaceFileDataWithOnlineData",true,"如果对应的玩家在线,使用内存中的数据进行备份,而非模块对应的存档文件","启用此项可以比较实时的备份玩家数据");
        this.mConfig.addDefault("RemoveDataWhenPlayerQuit",true,"在玩家退出游戏时,清理该玩家在内存中的备份数据");
    }

    public File getBackupDir(){
        return this.mBackupDir;
    }

    public File getServerDir(){
        return this.mServerDir;
    }

    public FileNameMode getFileNameMode(){
        return this.mFileNameMode;
    }

    public boolean isKeepGameModeWhenRollBack(){
        return this.mKeepGameModeWhenRollBack;
    }

    public boolean isRemoveDataWhenPlayerQuit(){
        return this.mRemoveDataWhenPlayerQuit;
    }

}
