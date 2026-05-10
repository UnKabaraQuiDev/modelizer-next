package lu.kbra.modelizer_next.bootstrap.subapp;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.Queue;

import lu.kbra.modelizer_next.bootstrap.AppMain;
import lu.kbra.modelizer_next.common.UnsupportedBootstrapVersionException;

/**
 * Launches an installed app jar through an isolated class loader.
 */
public final class JarApplicationLauncher {

	private ChildFirstURLClassLoader activeLoader;

	/**
	 * Launches the installed application.
	 *
	 * @param args        command-line arguments supplied by the launcher
	 * @param toBeOpened  to be opened value used by the operation
	 * @param application application value used by the operation
	 * @throws AppLaunchException if the operation cannot be completed
	 */
	public void launch(final String[] args, final Queue<File> toBeOpened, final InstalledApplication application)
			throws AppLaunchException {
		if (application == null) {
			throw new AppLaunchException("No installed application is available.");
		}
		try {
			this.activeLoader = new ChildFirstURLClassLoader(new URL[] { application.jarFile().toUri().toURL() },
					JarApplicationLauncher.class.getClassLoader(),
					Collections.singletonList("lu.kbra.modelizer_next.bootstrap"));
			Thread.currentThread().setContextClassLoader(this.activeLoader);
			final Class<?> entryPointClass = Class.forName(application.entryPoint(), true, this.activeLoader);
			if (!AppMain.class.isAssignableFrom(entryPointClass)) {
				throw new AppLaunchException("Entry point '" + application.entryPoint() + "' does not implement AppMain.");
			}
			final AppMain appMain = (AppMain) entryPointClass.getDeclaredConstructor().newInstance();
			try {
				final Method legacyStart = appMain.getClass().getMethod("start", String[].class, Queue.class);
				legacyStart.invoke(appMain, new Object[] { new String[] { "" }, null });
			} catch (NoSuchMethodException e) {
				appMain.start(args);
			}
		} catch (final AppLaunchException ex) {
			throw ex;
		} catch (final ClassNotFoundException | NoClassDefFoundError | UnsupportedBootstrapVersionException ex) {
			throw new AppLaunchException("Failed to launch application because the bootstrap launcher is outdated.", ex);
		} catch (final Exception ex) {
			throw new AppLaunchException("Failed to launch application from " + application.jarFile(), ex);
		}
	}

}
