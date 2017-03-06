package cc.bukkitPlugin.invback.api.dataBackup.model;

import java.io.File;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.dataBackup.DataBackupAPI;
import cc.bukkitPlugin.invback.api.dataBackup.IDataBackup;
import cc.commons.commentedyaml.CommentedSection;

public abstract class ADataBackup implements IDataBackup{

    protected InvBack mPlugin;
    protected String mDescription=this.getClass().getSimpleName();
    protected String mDataPath="world"+File.separator+"playerdata"+File.separator;
    protected File mDataDir;

    protected boolean mEnable=true;

    public ADataBackup(InvBack pPlugin){
        this.mPlugin=pPlugin;
    }

    public ADataBackup(InvBack pPlugin,String pDescription){
        this.mPlugin=pPlugin;
        this.mDescription=pDescription;
    }

    @Override
    public String getDescription(){
        return this.mDescription;
    }

    @Override
    public InvBack getPlugin(){
        return this.mPlugin;
    }

    @Override
    public boolean addDefaultConfig(CommentedSection pSection){
        pSection.addDefault("Description",this.mDescription,"模块描述,显示用");
        pSection.addDefault("DataDir",this.mDataPath,"此模块的玩家数据文件位置,相对路径将相对服务器路径");
        if(!(this instanceof DB_VanillaData)){
            pSection.addDefault("Enable",this.mEnable,"是否启用该模块");
        }
        return true;
    }

    @Override
    public void reloadConfig(CommandSender pSender,CommentedSection pSection){
        if(pSection!=null){
            this.mDescription=pSection.getString("Description",this.mDescription);
            this.mDataPath=pSection.getString("DataDir",this.mDataPath);
            this.mDataDir=new File(this.mDataPath);
            if(!this.mDataDir.isAbsolute()){
                this.mDataDir=new File(DataBackupAPI.getServerDir(),this.mDataPath);
            }
            if(!(this instanceof DB_VanillaData)){
                this.mEnable=pSection.getBoolean("Enable",this.mEnable);
            }
        }else{
            this.mDataDir=new File(DataBackupAPI.getServerDir(),this.mDataPath);
        }
    }

}
