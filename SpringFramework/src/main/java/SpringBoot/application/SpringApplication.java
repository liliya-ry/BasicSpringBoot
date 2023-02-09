package SpringBoot.application;

public class SpringApplication {
    private static ApplicationContext appContext;
    public static void run(Class<?> configurationClass, String[] arguments) throws Exception {
        appContext = new ApplicationContext(configurationClass);

        SpringInjector springInjector = new SpringInjector(appContext);
        springInjector.injectSpring();

        ModulesInjector modulesInjector = new ModulesInjector(appContext);
        modulesInjector.loadModules();
    }

    public static ApplicationContext getAppContext() {
        return appContext;
    }
}
