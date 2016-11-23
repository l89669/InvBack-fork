package cc.bukkitPlugin.invback.api.dataBackup;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.util.config.CommentedSection;
import cc.bukkitPlugin.util.plugin.manager.apiManager.IModel;

public interface IDataBackup extends IModel{

    /**
     * 初始化一个模块
     * @return  是否初始化成功
     */
    boolean init();

    /**获取模块的名字*/
    String getName();

    @Override
    public String getDescription();

    /**获取模块所属插件*/
    Plugin getPlugin();

    /**
     * 添加模块的默认配置数据
     * @param pSection  配置节点,非null
     * @return          是否添加过配置
     */
    public boolean addDefaultConfig(CommentedSection pSection);

    /**
     * 加载模块的配置
     * @param pSender   请求发起者
     * @param pSection  配置节点,可能为null
     */
    public void reloadConfig(CommandSender pSender,CommentedSection pSection);

    /**
     * 备份服务器此模块的所有玩家数据
     * <p>
     * 插件会先创建一个临时文件夹,然后再在该文件夹下以此模块名字创建一个文件夹,
     * 最后创建的那个文件夹就是pTargetDir
     * </p>
     * @param pTargetDir        数据要备份到的地方
     * @param pEnableReplace    是否用在线玩家数据替换文件复制
     * @return                  是否操作成功
     */
    public boolean backup(CommandSender pSender,File pTargetDir,boolean pEnableReplace) throws IOException;

    /**
     * 备份服务器此模块指定玩家的数据到指定文件夹
     * <p>
     * 插件会先创建一个临时文件夹,然后再在该文件夹下以此模块名字创建一个文件夹,
     * 最后创建的那个文件夹就是pTargetDir
     * </p>
     * @param pTargetDir        数据要备份到的地方,不能为null
     * @param pTargetPlayer     备份谁的数据
     * @return                  是否操作成功
     */
    public boolean backup(CommandSender pSender,File pTargetDir,OfflinePlayer pTargetPlayer) throws IOException;

    /**
     * 还原某个备份到指定玩家
     * <p>
     * 在备份数据的压缩文件,各个模块的文件的ZipEntry均以 {@link IModel#getName()}+{@link File#separator}为开头
     * </p>
     * @param pBackupData       数据要备份到的地方
     * @param pToPlayer         还原数据到哪个玩家
     * @return                  是否操作成功
     */
    public boolean restore(CommandSender pSender,ZipFile pBackupData,OfflinePlayer pFromPlayer,Player pToPlayer) throws IOException;

    /**
     * 保存在线玩家的数据到指定文件夹
     * <p>
     * 此函数常用于模拟服务器的自身的数据保存
     * </p>
     * @param pPlayerDataDir    服务器玩家数据位置
     * @param pSaveDir          数据要保存到的地方,留空代表使用Mod默认的位置
     * @param pFromPlayer       在线玩家数据来源
     * @param pToPlayer         保存到的哪个玩家
     * @return                  是否操作成功
     */
    public boolean saveTo(CommandSender pSender,File pSaveDir,Player pFromPlayer,OfflinePlayer pToPlayer) throws IOException;

    /**
     * 保存在线玩家的数据到指定文件夹
     * <p>
     * 此函数常用于模拟服务器的自身的数据读取
     * </p>
     * @param pPlayerDataDir    服务器玩家数据位置
     * @param pSaveDir          数据载入的地方,留空代表使用Mod默认的位置
     * @param pToPlayer         载入数据到谁
     * @param pFromPlayer       载入来自谁的数据
     * @return                  是否操作成功
     */
    public boolean loadFrom(CommandSender pSender,File pLoadDir,Player pToPlayer,OfflinePlayer pFromPlayer) throws IOException;

    /**
     * 保存在线玩家的数据到指定内存Map
     * <p>
     * 此函数用于临时备份玩家数据到内存中
     * </p>
     * @param pFromPlayer       在线玩家数据来源
     * @param pMemoryData       数据保存位置,一个模块占用一个key
     * @return                  是否操作成功
     */
    public boolean saveToMemoryMap(CommandSender pSender,Player pFromPlayer,Map<Object,Object> pMemoryData);

    /**
     * 从内存Map还原数据到指定的在线玩家
     * <p>
     * 此函数用于将内存临时备份数据还原到玩家
     * </p>
     * @param pToPlayer         要还原数据给谁
     * @param pMemoryData       数据保存位置,一个模块占用一个key
     * @return                  是否操作成功
     */
    public boolean loadFromMemoryMap(CommandSender pSender,Player pToPlayer,Map<Object,Object> pMemoryData);

    /**
     * 在线玩家数据复制
     * @param pFromPlayer       数据赖于
     * @param pToPlayer         要把数据复制给谁
     * @return                  是否操作成功
     */
    public boolean copy(CommandSender pSender,Player pFromPlayer,Player pToPlayer);

    /**
     * 重置/清理该玩家此模块的数据
     * @param pSender       请求发起者
     * @param pTargetPlayer 要重置/清理的玩家
     * @return              是否操作成功
     */
    public boolean reset(CommandSender pSender,Player pTargetPlayer);

}
