package lu.kbra.modelizer_next.bootstrap.subapp;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public final class ChildFirstURLClassLoader extends URLClassLoader {

	private final List<String> parentFirstPackages;

	public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent, List<String> parentFirstPackages) {
		super(urls, parent);
		this.parentFirstPackages = parentFirstPackages;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			Class<?> loadedClass = findLoadedClass(name);

			if (loadedClass == null) {
				if (isParentFirst(name)) {
					loadedClass = loadParentFirst(name);
				} else {
					loadedClass = loadChildFirst(name);
				}
			}

			if (resolve) {
				resolveClass(loadedClass);
			}

			return loadedClass;
		}
	}

	private Class<?> loadChildFirst(String name) throws ClassNotFoundException {
		try {
			return findClass(name);
		} catch (ClassNotFoundException ignored) {
			return getParent().loadClass(name);
		}
	}

	private Class<?> loadParentFirst(String name) throws ClassNotFoundException {
		try {
			return getParent().loadClass(name);
		} catch (ClassNotFoundException ignored) {
			return findClass(name);
		}
	}

	private boolean isParentFirst(String className) {
		return className.startsWith("java.") || className.startsWith("jdk.") || className.startsWith("sun.")
				|| parentFirstPackages.stream().anyMatch(className::startsWith);
	}

	@Override
	public URL getResource(String name) {
		URL resource = findResource(name);
		if (resource != null) {
			return resource;
		}
		return getParent().getResource(name);
	}

}