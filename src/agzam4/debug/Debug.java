package agzam4.debug;

import mindustry.Vars;

public class Debug {
	
//	public static void main(String[] args) throws Exception {
//		TestDrive testDrive = new TestDrive();
//
//		Lookup lookup = MethodHandles.lookup();
//
////        Field lookupImplField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
////        lookupImplField.setAccessible(true);
//        
//		Method method = testDrive.getClass().getDeclaredMethod("print");
//		System.out.println("ReturnType: " + method.getReturnType().getPackageName());
//		
//		boolean a = method.canAccess(testDrive);
//		
////		MethodType.methodType(null)
//		method.setAccessible(true);
//		
//        MethodHandle privateMethodHandle = lookup.findSpecial(
//        		TestDrive.class, method.getName(),
//                MethodType.methodType(method.getReturnType()),
//                TestDrive.class);
//		
//		
//		method.invoke(testDrive);
//		
////        method.
//		
//		method.setAccessible(a);
//	}
	

	public static void init() {
		Vars.mods.getScripts().context.evaluateString(Vars.mods.getScripts().scope,
				"var mod = Vars.mods.getMod(\"agzam4mod\");\n"
				+ "var get = (pkg) => mod.loader.loadClass(pkg).newInstance();\n"
				+ "const AgzamDebug = get(\"agzam4.debug.Debug\")\n"
				+ "const AgzamUI = get(\"agzam4.MobileUI\")", "main.js", 0);
	}
	
	
}
