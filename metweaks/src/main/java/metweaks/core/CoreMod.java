package metweaks.core;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import metweaks.ASMConfig;

import java.util.Locale;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"metweaks.core"})
public class CoreMod implements IFMLLoadingPlugin{
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {ClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {
    	/*
    	// for time to inject profiler
    	try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
    	
    	ClassTransformer.obf = (boolean) data.get("runtimeDeobfuscationEnabled");
    	FMLLog.info(ClassTransformer.coreprefix+": CoreMod Startup - Obfuscation=" + ClassTransformer.obf);
    	ASMConfig.init();
    	ClassTransformer.setup();
    	
    	if(ASMConfig.fixTurkishLang && Locale.getDefault().getLanguage().equals("tr")) {
    		String old = Locale.getDefault().getLanguage();
    		Locale.setDefault(Locale.US);
    		FMLLog.info(ClassTransformer.coreprefix+": Language changed from "+old+" to "+Locale.US.getLanguage());
    	}
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}