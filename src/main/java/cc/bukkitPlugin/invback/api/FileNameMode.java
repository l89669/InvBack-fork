package cc.bukkitPlugin.invback.api;

import cc.commons.util.StringUtil;

public enum FileNameMode{
    UUID,NAME;
    
    public static FileNameMode getMode(String pModeName,FileNameMode pDefVal){
        if(StringUtil.isEmpty(pModeName)) return pDefVal;
        pModeName=pModeName.toLowerCase();
        for(FileNameMode sMode : FileNameMode.values()){
            if(sMode.name().toLowerCase().equals(pModeName))
                return sMode;
        }
        return pDefVal;
        
    }
    
}
