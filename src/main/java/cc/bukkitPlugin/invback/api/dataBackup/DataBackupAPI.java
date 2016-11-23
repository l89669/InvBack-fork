package cc.bukkitPlugin.invback.api.dataBackup;

import java.io.File;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.invback.api.FileNameMode;
import cc.bukkitPlugin.invback.api.dataBackup.model.DB_Achievement;
import cc.bukkitPlugin.invback.api.dataBackup.model.DB_Baubles;
import cc.bukkitPlugin.invback.api.dataBackup.model.DB_CustomNPC;
import cc.bukkitPlugin.invback.api.dataBackup.model.DB_EssentailCraft3;
import cc.bukkitPlugin.invback.api.dataBackup.model.DB_TConstruct;
import cc.bukkitPlugin.invback.api.dataBackup.model.DB_ThaumCraft;
import cc.bukkitPlugin.invback.api.dataBackup.model.DB_TravellersGear;
import cc.bukkitPlugin.invback.api.dataBackup.model.DB_VanillaData;
import cc.bukkitPlugin.invback.manager.ConfigManager;
import cc.bukkitPlugin.util.config.CommentedSection;
import cc.bukkitPlugin.util.config.CommentedYamlConfig;
import cc.bukkitPlugin.util.plugin.INeedConfig;
import cc.bukkitPlugin.util.plugin.manager.apiManager.AAPIRegisterManager;
import cc.bukkitPlugin.util.plugin.manager.fileManager.IConfigModel;

public class DataBackupAPI extends AAPIRegisterManager<InvBack,IDataBackup> implements INeedConfig,IConfigModel{

    public static final String SEC_CFG_MAIN="Models";
    private static DataBackupAPI mInstance;
    /**API初始化阶段 0=为初始化 1=已经添加默认配置 2=已经重载配置*/
    private int mInitStatus=0;
    /**文件名字模式*/
    private FileNameMode mFileNameMode=FileNameMode.UUID;
    /**服务器路径*/
    private File mServerDir;
    /**备份文件存放处*/
    private File mBackupDir;
    /**备份过期天数(天)*/
    private int mBackupExpriedDays=7;
    /**是否启用在线数据替换*/
    private boolean mReplaceFileDataWithOnlineData=true;

    private DataBackupAPI(InvBack pPlugin){
        super(pPlugin);

        this.mPlugin.registerConfigModel(this);
        this.mPlugin.getConfigManager().registerConfigModel(this);
        
        this.register(new DB_VanillaData(this.mPlugin));
        this.register(new DB_Achievement(this.mPlugin));
        this.register(new DB_Baubles(this.mPlugin));
        this.register(new DB_CustomNPC(this.mPlugin));
        this.register(new DB_EssentailCraft3(this.mPlugin));
        this.register(new DB_TConstruct(this.mPlugin));
        this.register(new DB_ThaumCraft(this.mPlugin));
        this.register(new DB_TravellersGear(this.mPlugin));
        // 太空
    }

    @Override
    public String getAPIName(){
        return "数据备份API";
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        CommentedSection tModelSections=tConfig.getOrCreateSection(DataBackupAPI.SEC_CFG_MAIN,"各个数据备份模块配置");
        for(IDataBackup sModel : DataBackupAPI.getInstance().getAllModels()){
            CommentedSection tSection=tModelSections.getSection(sModel.getName());
            if(tSection==null){
                tSection=tModelSections.createSection(sModel.getName());
            }
            if(!sModel.addDefaultConfig(tSection)){
                tModelSections.set(sModel.getName(),null);
            }
        }
        synchronized(this){
            this.mInitStatus=1;
        }
    }

    @Override
    public void setConfig(CommandSender pSender){
        ConfigManager configMan=this.mPlugin.getConfigManager();
        this.mFileNameMode=configMan.getFileNameMode();
        this.mBackupDir=configMan.getBackupDir();
        this.mServerDir=configMan.getServerDir();

        CommentedYamlConfig tConfig=configMan.getConfig();
        this.mBackupExpriedDays=tConfig.getInt("BackupExpriedDays",this.mBackupExpriedDays);
        this.mReplaceFileDataWithOnlineData=tConfig.getBoolean("ReplaceFileDataWithOnlineData");

        if(this.mBackupExpriedDays<=0)
            this.mBackupExpriedDays=7;

        CommentedSection tModelSections=tConfig.getOrCreateSection(DataBackupAPI.SEC_CFG_MAIN,"各个数据备份模块配置");
        for(IDataBackup sModel : this.getAllModels()){
            sModel.reloadConfig(pSender,tModelSections.getSection(sModel.getName()));
        }
        synchronized(this){
            this.mInitStatus=1;
        }
    }

    @Override
    protected void onModelSuccessRegister(IDataBackup pModel){
        super.onModelSuccessRegister(pModel);
        synchronized(this){
            if(this.mInitStatus==0)
                return;

            CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
            CommentedSection tModelSections=tConfig.getOrCreateSection(DataBackupAPI.SEC_CFG_MAIN,"各个数据备份模块配置");
            CommentedSection tSection=null;
            if(this.mInitStatus>=1){
                tSection=tModelSections.getSection(pModel.getName());
                if(tSection==null){
                    tSection=tModelSections.createSection(pModel.getName());
                }
                if(!pModel.addDefaultConfig(tSection)){
                    tModelSections.set(pModel.getName(),null);
                }
            }
            if(this.mInitStatus==2){
                if(tSection==null){
                    tSection=tModelSections.getSection(pModel.getName());
                }
                pModel.reloadConfig(null,tSection);
            }

        }
    }

    public static DataBackupAPI getInstance(){
        synchronized(DataBackupAPI.class){
            if(DataBackupAPI.mInstance==null){
                DataBackupAPI.mInstance=new DataBackupAPI(InvBack.getInstance());
            }
        }
        return DataBackupAPI.mInstance;
    }

    /**获取玩家数据所在位置*/
    public static File getServerDir(){
        return DataBackupAPI.getInstance().mPlugin.getConfigManager().getServerDir();
    }

    /**获取备份数据存放位置*/
    public static File getBackupDir(){
        return DataBackupAPI.getInstance().mPlugin.getConfigManager().getBackupDir();
    }

    /**获取备份原版数据文件的名字模式*/
    public static FileNameMode getFileNameMode(){
        return DataBackupAPI.getInstance().mPlugin.getConfigManager().getFileNameMode();
    }

    /**是否将在线玩家的数据作为备份数据的来源而非直接复制玩家存档文件*/
    public static boolean isReplaceFileDataWithOnlineData(){
        return DataBackupAPI.getInstance().mReplaceFileDataWithOnlineData;
    }

    /**获取备份过期的天数*/
    public static int getBackupExpriedDays(){
        return DataBackupAPI.getInstance().mBackupExpriedDays;
    }

}
