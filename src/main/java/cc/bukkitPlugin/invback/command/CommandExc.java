package cc.bukkitPlugin.invback.command;

import cc.bukkitPlugin.commons.plugin.command.TCommandExc;
import cc.bukkitPlugin.invback.InvBack;

public class CommandExc extends TCommandExc<InvBack>{

    /**
     * 必须在配置文件启用后才能调用此方法
     * @param pPlugin
     */
    public CommandExc(InvBack pPlugin){
        super(pPlugin,"inv",true);
        this.registerSubCommand();
    }

    @Override
    protected void registerSubCommand(){
        super.registerSubCommand();
        this.register(new CommandBackup     (this));
        this.register(new CommandClearBackup(this));
        this.register(new CommandCopy       (this));
        this.register(new CommandList       (this));
        this.register(new CommandLoad       (this));
        this.register(new CommandReset      (this));
        this.register(new CommandRollBack   (this));
        this.register(new CommandRunTask    (this));
        this.register(new CommandSet        (this));
        this.register(new CommandSave       (this));
    }

    
}
