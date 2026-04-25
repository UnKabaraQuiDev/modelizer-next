package lu.kbra.modelizer_next.bootstrap;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Queue;

final class JarApplicationLauncher {

	private URLClassLoader activeLoader;

	void launch(final String[] args, final Queue<File> toBeOpened, final InstalledApplication application) throws AppLaunchException {
		if (application == null) {
			throw new AppLaunchException("No installed application is available.");
		}
		try {
			this.activeLoader = new URLClassLoader(new URL[] { application.jarFile().toUri().toURL() },
					JarApplicationLauncher.class.getClassLoader());
			Thread.currentThread().setContextClassLoader(this.activeLoader);
			final Class<?> entryPointClass = Class.forName(application.entryPoint(), true, this.activeLoader);
			if (!AppMain.class.isAssignableFrom(entryPointClass)) {
				throw new AppLaunchException("Entry point '" + application.entryPoint() + "' does not implement AppMain.");
			}
			final AppMain appMain = (AppMain) entryPointClass.getDeclaredConstructor().newInstance();
			try {
				final Method legacyStart = appMain.getClass().getMethod("start", String[].class);
				legacyStart.invoke(appMain, new Object[] { (Object) new String[] { "" } });
			} catch (NoSuchMethodException e) {
				appMain.start(args, toBeOpened);
			}
		} catch (final AppLaunchException ex) {
			throw ex;
		} catch (final Exception ex) {
			throw new AppLaunchException("Failed to launch application from " + application.jarFile(), ex);
		}
	}

}
