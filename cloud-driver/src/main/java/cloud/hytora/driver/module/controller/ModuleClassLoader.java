package cloud.hytora.driver.module.controller;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.module.Module;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class ModuleClassLoader extends URLClassLoader {

	private static final Collection<ModuleClassLoader> loaders = new CopyOnWriteArrayList<>();

	private final ClassLoader parent;

	private final File jarFile;

	private Module module;

	public ModuleClassLoader(@Nonnull URL[] jarFileUrl, @Nonnull ClassLoader parent, File jarFile) {
		super(jarFileUrl);
		this.parent = parent;
		this.jarFile = jarFile;
		loaders.add(this);
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return this.loadClass0(name, resolve);
		} catch (ClassNotFoundException ex) {
		}

		for (ModuleClassLoader loader : loaders) {
			if (loader == this) continue;
			try {
				return loader.loadClass0(name, resolve);
			} catch (ClassNotFoundException ex) {
			}
		}

		try {
			return parent.loadClass(name);
		} catch (ClassNotFoundException ex) {
		}

		throw new ClassNotFoundException(name);
	}

	private Class<?> loadClass0(@Nonnull String name, boolean resolve) throws ClassNotFoundException {
		return super.loadClass(name, resolve);
	}

	@Override
	public void close() throws IOException {
		loaders.remove(this);
		super.close();
	}


	public String loadJson(String filename) {
		try {
			JarFile jf = new JarFile(jarFile);
			JarEntry je = jf.getJarEntry(filename);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je)))) {
				StringBuilder builder = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					builder.append(line);
				}
				jf.close();
				br.close();
				return builder.toString();
			} catch (Exception e) {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	@Nullable
	public Document loadDocument(String filename) {
		String jsonInput = this.loadJson(filename);
		if (jsonInput == null) {
			return null;
		}
		return DocumentFactory.newJsonDocument(jsonInput);
	}

	@SneakyThrows
	public Class<?> findClassWithJarEntry(String name) {
		try {
			JarFile jarFile = new JarFile(this.jarFile);
			Enumeration<JarEntry> e = jarFile.entries();

			URL[] urls = {new URL("jar:file:" + this.jarFile.getAbsolutePath())};
			URLClassLoader cl = URLClassLoader.newInstance(urls);

			while (e.hasMoreElements()) {
				JarEntry je = e.nextElement();
				if(je.isDirectory() || !je.getName().endsWith(".class")){
					continue;
				}
				if (je.getName().contains(name)){
					String className = je.getName().substring(0,je.getName().length()-6);
					className = className.replace('/', '.');
					Class<?> c = cl.loadClass(className);
					return c;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
		return null;
	}

	public void setModule(@Nonnull Module module) {
		this.module = module;
	}

	@Nonnull
	public Module getModule() {
		return module;
	}
}
