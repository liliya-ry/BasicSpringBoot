package SpringBoot.application;

public class ModulesInjector {
    private final ApplicationContext appContext;

    ModulesInjector(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    void loadModules() throws Exception {
        String modulesStr = appContext.appProperties.getProperty("spring.modules");
        if (modulesStr == null)
            return;

        String[] adapterClassesNames = modulesStr.split(",");
        for (String adapterClassName : adapterClassesNames) {
            Class<?> adapterClass = Class.forName(adapterClassName);
            Adapter adapter = (Adapter) adapterClass.getConstructor().newInstance();
            adapter.configure(appContext);
        }
    }
}
