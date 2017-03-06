package cc.bukkitPlugin.invback.listener;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import cc.bukkitPlugin.commons.plugin.AListener;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.invback.InvBack;
import cc.commons.commentedyaml.CommentedYamlConfig;

public class PlayerListner extends AListener<InvBack> implements IConfigModel{

    private boolean mClearMemoryDataWherPlayerQuit=true;

    public PlayerListner(InvBack pPlugin){
        super(pPlugin);

        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){
        pConfig.addDefault("RemoveDataWhenPlayerQuit",this.mClearMemoryDataWherPlayerQuit,"在玩家退出游戏时,清理该玩家在内存中的备份数据");
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        this.mClearMemoryDataWherPlayerQuit=pConfig.getBoolean("RemoveDataWhenPlayerQuit",this.mClearMemoryDataWherPlayerQuit);
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerQuit(PlayerQuitEvent pEvent){
        if(!this.mClearMemoryDataWherPlayerQuit)
            return;
        this.mPlugin.getDataManager().removePlayerDataFromMemony(pEvent.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent pEvent){

    }
}
