package tree;

import javassist.*;
import utils.LoggerFormatter;
import utils.TreePathParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class TreeNode {
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());
    private byte[] fileBytes;
    private boolean baseNode;
    private boolean isDirectory;
    private CtClass ctClass = null;
    private String realName;

    public TreeNode(JarFile jarFile, JarEntry entry, boolean baseNode) {
        this.baseNode = baseNode;
        this.isDirectory = entry.isDirectory();
        this.realName = entry.getRealName();
        try {
            InputStream in = jarFile.getInputStream(entry);
            fileBytes = in.readAllBytes();
            in.close();
        } catch (IOException e) {
            logger.warning(String.format("Couldn't create InputStream from JarEntry: %s", e.getMessage()));
        }
        createCtClass();
    }

    public TreeNode(CtClass ctClass, String realName) {
        this.ctClass = ctClass;
        this.realName = realName;
        isDirectory = false;
        baseNode = false;
    }

    public TreeNode(String realName) {
        this.realName = realName;
        isDirectory = true;
        baseNode = false;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getRealName() {
        return realName;
    }

    public CtClass getCtClass() {
        return ctClass;
    }

    public boolean isBaseNode() {
        return baseNode;
    }

    public void update() throws IOException, CannotCompileException {
        if (ctClass == null) return;
        fileBytes = ctClass.toBytecode();
    }

    private void createCtClass() {
        if (!getRealName().endsWith(".class")) return;
        ClassPool classPool = ClassPool.getDefault();
        String className = TreePathParser.getClassNameFromRealName(getRealName());
        classPool.insertClassPath(new ByteArrayClassPath(className, fileBytes));
        try {
            ctClass = classPool.get(className);
            logger.info(String.format("Created CtClass %s", className));
        } catch (NotFoundException e1) {
            logger.warning("Class not found!");
        }
    }

    @Override
    public String toString() {
        if (realName.equals("")) return "Tree Root";
        return Paths.get(realName).getFileName().toString();
    }
}
