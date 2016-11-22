package cc.bukkitPlugin.invback.listener;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import cc.bukkitPlugin.invback.InvBack;
import cc.bukkitPlugin.util.config.CommentedYamlConfig;
import cc.bukkitPlugin.util.plugin.INeedConfig;


public class PlayerListner implements Listener,INeedConfig{

    private InvBack mPlugin;
    private boolean mClearMemoryDataWherPlayerQuit=true;
    
    public PlayerListner(InvBack pPlugin){
        this.mPlugin=pPlugin;
        this.mPlugin.getServer().getPluginManager().registerEvents(this,this.mPlugin);
    }
    
    @Override
    public void setConfig(CommandSender pSender){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        this.mClearMemoryDataWherPlayerQuit=tConfig.getBoolean("ClearMemoryDataWhenPlayerQuit");
    }
    
    @EventHandler(ignoreCancelled=true)
    public void onPlayerQuit(PlayerQuitEvent pEvent){
        if(!this.mClearMemoryDataWherPlayerQuit) return;
        this.mPlugin.getDataManager().removePlayerDataFromMemony(pEvent.getPlayer());
        
    }
    
}
