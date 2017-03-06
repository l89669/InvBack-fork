package cc.bukkitPlugin.invback.manager;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TLangManager;
import cc.bukkitPlugin.invback.InvBack;

public class LangManager extends TLangManager<InvBack>{

    /**
     * 语言翻译系统
     * @param plugin
     */
    public LangManager(InvBack pPlugin){
        super(pPlugin,"lang.yml","1.0");
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            Log.warn(pSender,C("MsgErrorHappendWhenReloadLang"));
            return false;
        }
        this.checkUpdate();
        this.addDefaults();
        boolean result=this.saveConfig(null);
        Log.info(pSender,C("MsgLangReloaded"));
        return result;
    }

    public boolean checkUpdate(){
        return super.checkUpdate();
    }

    @Override
    public void addDefaults(){
        super.addDefaults();
        this.mConfig.addDefault("HelpBackup","备份指定玩家的数据到内存,如果未指定,则备份自己的");
        this.mConfig.addDefault("HelpClearBackup","清理内存中的玩家数据备份");
        this.mConfig.addDefault("HelpCopy","复制前者的数据到后者上");
        this.mConfig.addDefault("HelpCopy1","如果后者未指定,则复制到自己身上");
        this.mConfig.addDefault("HelpList","列出指定一天的背包备份(序号-备份名字)");
        this.mConfig.addDefault("HelpList1","时间格式必须是'yyyy-MM-dd',例如\"2016-01-01\",2016年6月12号");
        this.mConfig.addDefault("HelpLoad","为后者载入前者的非备份存档");
        this.mConfig.addDefault("HelpLoad1","如果后者未指定,则载入存档到自己身上");
        this.mConfig.addDefault("HelpReset","重置一个玩家的数据,如果未指定,则重置自己的");
        this.mConfig.addDefault("HelpRollBack","从内存中恢复指定玩家的个人数据,如果未指定,则恢复自己的");
        this.mConfig.addDefault("HelpRunTask","立刻备份背包一次");
        this.mConfig.addDefault("HelpSave","将前者的个人数据保存到后者的存档中");
        this.mConfig.addDefault("HelpSave1","如果后者未指定,则保存个人数据到自己的存档");
        this.mConfig.addDefault("HelpSave2","此命令会强制设置保存的存档中的玩家游戏模式为生存模式");
        this.mConfig.addDefault("HelpSet","复制指定时间背包备份中指定玩家的数据复制到目标玩家");
        this.mConfig.addDefault("HelpSet1","目标玩家: 要设置背包的玩家,必须在线");
        this.mConfig.addDefault("HelpSet2","日期 : 指定哪天");
        this.mConfig.addDefault("HelpSet3","    日期格式必须是'yyyy-MM-dd',例如\"2016-06-12\",2016年6月12号");
        this.mConfig.addDefault("HelpSet4","时间:  指定哪个备份,可以是背包序号(查看/inv list),或是指定时间");
        this.mConfig.addDefault("HelpSet5","    时间格式必须是'HH-mm-ss',例如\"12-00-00\",12点0分0秒");
        this.mConfig.addDefault("HelpSet6","离线玩家:  指定要复制哪个玩家的,&c区分大小写");
        this.mConfig.addDefault("MsgAlreadyClearPlayerData","已经重置 %player% 的数据");
        this.mConfig.addDefault("MsgAlreadyCopyed","已经复制 %player% 的数据");
        this.mConfig.addDefault("MsgAlreadyCopyedForPlayer","已经复制 %fromplayer% 的数据到 %toplayer%");
        this.mConfig.addDefault("MsgAlreadyLoadPlayerDataFromFile","已经为 %toplayer% 载入%fromplayer%的存档数据");
        this.mConfig.addDefault("MsgAlreadyLoadPlayerDataFromMemory","已经从内存还原 %player% 的备份数据");
        this.mConfig.addDefault("MsgAlreadyResetPlayerData","已经重置 %player% 的数据");
        this.mConfig.addDefault("MsgAlreadySaveOtherPlayerDataToPlayerFile","已经将 %fromplayer% 的数据保存到 %toplayer% 的存档中");
        this.mConfig.addDefault("MsgAlreadySavePlayerDataToFile","已经将 %player% 的数据保存到存档中");
        this.mConfig.addDefault("MsgAlreadySavePlayerDataToMemory","已经将 %player% 的数据保存到内存中");
        this.mConfig.addDefault("MsgBackupCompleteAndTookTime","备份完成,此次备份耗时 %time%");
        this.mConfig.addDefault("MsgBackupDataNoFoundInDay","在 %day% 没有发现备份文件 %file%");
        this.mConfig.addDefault("MsgBackupFileNotFound","未找到备份文件 %file%");
        this.mConfig.addDefault("MsgBackupTaskHasRun","已经进行了一次玩家数据备份");
        this.mConfig.addDefault("MsgClickToGenerateCmd","点击生成命令");
        this.mConfig.addDefault("MsgConsoleShouldInputPlayer","§c控制台必须输入用户");
        this.mConfig.addDefault("MsgErrorOnBackupPlayeData","插件备份玩家数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnClearExpriedBackup","在清理过期备份时发生了错误");
        this.mConfig.addDefault("MsgErrorOnClearFileDir","清空临时文件夹 %dir% 的文件时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelBackupCopyFile","模块 %model% 在备份数据复制文件时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelBackupFile","模块 %model% 在备份数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelCopyPlayerData","模块 %model% 在复制玩家 %player% 的数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelLoadPlayerDataFromFile","模块 %model% 在从文件还原玩家 %player% 的数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelLoadPlayerDataFromMemory","模块 %model% 在从内存还原玩家 %player% 的数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelResetPlayerData","模块 %model% 在重置 %player% 的数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelRestoreData","模块 %model% 在还原数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelSavePlayerDataToFile","模块 %model% 在保存玩家 %player% 的数据到文件时发生了错误");
        this.mConfig.addDefault("MsgErrorOnModelSavePlayerDataToMemory","模块 %model% 在保存玩家 %player% 的数据到内存时发生了错误");
        this.mConfig.addDefault("MsgErrorOnOpenZipFile","打开压缩文件 %file% 时发生了错误");
        this.mConfig.addDefault("MsgErrorOnZipTempFileToBackupFile","压缩临时文件到备份文件时发生了错误");
        this.mConfig.addDefault("MsgErrorTimeFormat","错误的时间格式,必须是 %format%");
        this.mConfig.addDefault("MsgNoFileCopyToTempDir","备份临时文件夹下无任何文件,取消此次备份");
        this.mConfig.addDefault("MsgNoMemoryBackupDataClear","§c内存中未备份数据");
        this.mConfig.addDefault("MsgNotCorrectBackupDataNumb","§c%string%不是正确的备份数据编号,请使用/inv list来查看编号");
        this.mConfig.addDefault("MsgBackupDataNumbNotInRange","§c数据备份编号 %numb% 不在范围");
        this.mConfig.addDefault("MsgMemoryBackupDataCleared","清理了 %numb% 个玩家的内存数据备份");
        this.mConfig.addDefault("MsgModelBackupDataNotFoundPlayer","模块 %model% 未找到玩家 %player% 的备份数据");
        this.mConfig.addDefault("MsgModelBackupZipDataNoEntry","模块 %model% 未在找到备份压缩包中找到 %file%");
        this.mConfig.addDefault("MsgModelDataFileNotExist","模块 %model% 的数据文件 %file% 不存在");
        this.mConfig.addDefault("MsgNoBackupDataInDay","在 %day% 没有任何备份数据");
        this.mConfig.addDefault("MsgNoMemoryDataForPlayer","内存中没有 %player% 的备份数据");
        this.mConfig.addDefault("MsgNoSenseToCopyDataFromSelf","§c自己复制自己的数据是没意义的");
        this.mConfig.addDefault("MsgPlayerDataRestore","已经使用 %fromplayer% 的备份数据还原 %toplayer%");
        this.mConfig.addDefault("MsgPlayerNotOnlineOrExist","§c玩家 %player% 未在线或不存在");
        this.mConfig.addDefault("MsgSetServerDir","设置服务器位置: ");
        this.mConfig.addDefault("MsgSetDataBackupDir","设置备份位置: ");
        this.mConfig.addDefault("MsgStartBackup","开始备份玩家数据");
        this.mConfig.addDefault("WordDate","日期");
        this.mConfig.addDefault("WordOfflinePlayer","离线玩家");
        this.mConfig.addDefault("WordPage","页码");
        this.mConfig.addDefault("WordPlayer","玩家");
        this.mConfig.addDefault("WordTime","时间");
        this.mConfig.addDefault("WordYou","你");
    }
}
