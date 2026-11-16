package metweaks.core;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import metweaks.ASMConfig;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import java.lang.reflect.Field;
import java.util.List;
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
    	
    	// for time to inject profiler in dev environment
    	// try { Thread.sleep(15000); } catch (InterruptedException e) {}
    	
    	ClassTransformer.obf = (boolean) data.get("runtimeDeobfuscationEnabled");
    	FMLLog.info(ClassTransformer.coreprefix+": CoreMod Startup - Obfuscation=" + ClassTransformer.obf);
    	ASMConfig.init();
    	
    	
    	checkCoreMods();
    	FMLLog.info("Optifine="+ClassTransformer.optifine+" LOTRCore="+ClassTransformer.lotrCore);
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
    
    public void checkCoreMods() {
    	try {
	    	Field field = Launch.classLoader.getClass().getDeclaredField("transformers");
	        field.setAccessible(true);
	        @SuppressWarnings("unchecked")
			List<IClassTransformer> transformers = (List<IClassTransformer>) field.get(Launch.classLoader);
	        for(IClassTransformer transformer : transformers) {
	        	String name = transformer.getClass().getName();
	        	if(name.startsWith("optifine")) {
	            	ClassTransformer.optifine = true;
	            }
	        }
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	try {
    		//CoreMod.class.getClassLoader().getResource("lotr.common.coremod.LOTRLoadingPlugin");
    		Class.forName("lotr.common.coremod.LOTRLoadingPlugin");
    		ClassTransformer.lotrCore = true;
    	}
    	catch(Exception e) {}
    }
    
    public static boolean hasKCauldron() {
    	if(ClassTransformer.kcauldron == 0) {
    		ClassTransformer.kcauldron = 
    			CoreMod.class.getClassLoader().getResource("kcauldron.KCauldronClassTransformer") == null && CoreMod.class.getClassLoader().getResource("thermos.ThermosClassTransformer") == null 
    			? 2 : 1;
    	}
    	return ClassTransformer.kcauldron == 1;
    }
}