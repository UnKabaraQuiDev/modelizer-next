package lu.kbra.modelizer_next.bootstrap.subapp;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * URL class loader that prefers application jars before parent classes for app isolation.
 */
public final class ChildFirstURLClassLoader extends URLClassLoader {

	private final List<String> parentFirstPackages;

	/**
	 * Creates a child first URL class loader instance during bootstrap/update processing.
	 *
	 * @param urls                values for urls
	 * @param parent              parent component used for dialog ownership
	 * @param parentFirstPackages values for parent first packages
	 */
	public ChildFirstURLClassLoader(final URL[] urls, final ClassLoader parent, final List<String> parentFirstPackages) {
		super(urls, parent);
		this.parentFirstPackages = parentFirstPackages;
	}

	/**
	 * Returns the resource during bootstrap/update processing.
	 *
	 * @param name name value to read, write, or display
	 * @return the resource
	 */
	@Override
	public URL getResource(final String name) {
		final URL resource = this.findResource(name);
		if (resource != null) {
			return resource;
		}
		return this.getParent().getResource(name);
	}

	/**
	 * Checks whether parent first is enabled or applies during bootstrap/update processing.
	 *
	 * @param className name value to use
	 * @return {@code true} if parent first is enabled or applies; otherwise {@code false}
	 */
	private boolean isParentFirst(final String className) {
		return className.startsWith("java.") || className.startsWith("jdk.") || className.startsWith("sun.")
				|| this.parentFirstPackages.stream().anyMatch(className::startsWith);
	}

	/**
	 * Loads the child first during bootstrap/update processing.
	 *
	 * @param name name value to read, write, or display
	 * @return the load child first result
	 * @throws ClassNotFoundException if the operation cannot be completed
	 */
	private Class<?> loadChildFirst(final String name) throws ClassNotFoundException {
		try {
			return this.findClass(name);
		} catch (final ClassNotFoundException ignored) {
			return this.getParent().loadClass(name);
		}
	}

	/**
	 * Loads the class during bootstrap/update processing.
	 *
	 * @param name    name value to read, write, or display
	 * @param resolve whether resolve is enabled
	 * @return the load class result
	 * @throws ClassNotFoundException if the operation cannot be completed
	 */
	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		synchronized (this.getClassLoadingLock(name)) {
			Class<?> loadedClass = this.findLoadedClass(name);

			if (loadedClass == null) {
				if (this.isParentFirst(name)) {
					loadedClass = this.loadParentFirst(name);
				} else {
					loadedClass = this.loadChildFirst(name);
				}
			}

			if (resolve) {
				this.resolveClass(loadedClass);
			}

			return loadedClass;
		}
	}

	/**
	 * Loads the parent first during bootstrap/update processing.
	 *
	 * @param name name value to read, write, or display
	 * @return the load parent first result
	 * @throws ClassNotFoundException if the operation cannot be completed
	 */
	private Class<?> loadParentFirst(final String name) throws ClassNotFoundException {
		try {
			return this.getParent().loadClass(name);
		} catch (final ClassNotFoundException ignored) {
			return this.findClass(name);
		}
	}

}
