import javassist.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class TreeNode {
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());
    private JarFile jarFile;
    private JarEntry entry;
    private byte[] fileBytes;
    private boolean baseNode;
    private boolean isDirectory;
    private CtClass ctClass;
    private String realName;

    TreeNode(JarFile jarFile, JarEntry entry, boolean baseNode) {
        this.jarFile = jarFile;
        this.entry = entry;
        this.baseNode = baseNode;
        this.isDirectory = entry.isDirectory();
        this.realName = entry.getRealName();
        loadByteArray();
        createCtClass();
    }

    TreeNode(CtClass ctClass, String realName) {
        this.ctClass = ctClass;
        this.realName = realName;
        isDirectory = false;
        baseNode = false;
    }

    TreeNode(String realName) {
        this.realName = realName;
        isDirectory = true;
        baseNode = false;
    }

    byte[] getFileBytes() {
        return fileBytes;
    }

    boolean isDirectory() {
        return isDirectory;
    }

    String getRealName() {
        return realName;
    }

    CtClass getCtClass() {
        return ctClass;
    }

    boolean isBaseNode() {
        return baseNode;
    }

    void update() throws IOException, CannotCompileException {
        if (ctClass == null) return;
        fileBytes = ctClass.toBytecode();
    }

    private void loadByteArray() {
        byte[] classByteArray = null;
        try {
            InputStream in = jarFile.getInputStream(entry);
            classByteArray = in.readAllBytes();
        } catch (IOException e) {
            logger.warning("Couldn't create InputStream from JarEntry");
        }
        fileBytes = classByteArray;
    }

    private void createCtClass() {
        if (!getRealName().endsWith(".class")) return;
        ClassPool classPool = ClassPool.getDefault();
        String className = TreePathParser.getClassNameFromRealName(getRealName());
        classPool.insertClassPath(new ByteArrayClassPath(className, fileBytes));
        try {
            ctClass = classPool.get(className);
            logger.info(String.format("Created CtClass from %s", entry.getRealName()));
        } catch (NotFoundException e1) {
            logger.warning("Class not found!");
            ctClass = null;
        }
    }

    @Override
    public String toString() {
        return Paths.get(realName).getFileName().toString();
    }
}
