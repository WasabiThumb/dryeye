package io.github.wasabithumb.dryeye.app;

import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class Bootstrap {

    public static void main(String[] args) {
        if (!isJava25()) {
            errorPopup("This application requires Java 25 or greater.");
            return;
        }

        try {
            appMain(args);
        } catch (IllegalStateException e) {
            e.printStackTrace(System.err);
            errorPopup("Failed to start application, see stderr for details.");
        }
    }

    private static void appMain(String[] args) throws IllegalStateException {
        Class<?> mainClass;
        try {
            mainClass = Class.forName("io.github.wasabithumb.dryeye.app.Main");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Main class not found", e);
        }

        Method mainMethod = null;
        for (Method m : mainClass.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) continue;
            if (!"main".equals(m.getName())) continue;
            mainMethod = m;
            break;
        }
        if (mainMethod == null) {
            throw new IllegalStateException("Main class has no main method");
        }

        int nparam = mainMethod.getParameterCount();
        Object[] mainMethodArgs;
        switch (nparam) {
            case 0:
                mainMethodArgs = new Object[0];
                break;
            case 1:
                mainMethodArgs = new Object[] { args };
                break;
            default:
                throw new IllegalStateException("Main method has too many parameters (got " + nparam + ")");
        }

        try {
            mainMethod.invoke(null, mainMethodArgs);
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IllegalStateException("Invoking main method generated an unexpected exception", cause);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid reflect call", e);
        }
    }

    private static void errorPopup(String message) {
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "DryEye UI",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        System.err.println(message);
        System.exit(1);
    }

    private static boolean isJava25() {
        try {
            Class<?> cRuntimeVersion = Class.forName("java.lang.Runtime$Version");
            Method mRuntimeVersionFeature = cRuntimeVersion.getDeclaredMethod("feature");
            Method mVersion = Runtime.class.getDeclaredMethod("version");
            Object version = mVersion.invoke(null);
            int feature = (Integer) mRuntimeVersionFeature.invoke(version);
            return feature >= 25;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
            return false;
        }
    }

}
