package org.vaadin7.console;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin7.console.Console.Command;

import com.vaadin.ui.Component;

/**
 * Java object inspector.
 *
 * With object inspector you can simply wire methods from a class as commands to
 * the Console.
 *
 * @author Sami Ekblad
 *
 */
public class ObjectInspector implements Serializable, Console.CommandProvider {
    private static final long serialVersionUID = -5001688079827306472L;
    private static final List<String> OBJECT_BLACKLIST = Arrays
            .asList(new String[] { "equals", "class", "hashCode", "notify", "notifyAll", "toString", "wait" });
    private static final List<String> VAADIN_BLACKLIST = Arrays.asList(new String[] { "addListener", "attach", "application", "changeVariables",
            "childRequestedRepaint", "componentError", "detach", "handleError", "paint", "paintContent", "removeListener", "requestRepaint",
            "requestRepaintRequests", "style", "tag" });

    private Object theObject;
    private final Map<String, Caller> commands = new HashMap<String, Caller>();
    private Set<String> ignoredCommands = new HashSet<String>();

    public ObjectInspector(final Object obj) {

        // Default ignores
        ignoredCommands.addAll(OBJECT_BLACKLIST);
        if (obj instanceof Component) {
            ignoredCommands.addAll(VAADIN_BLACKLIST);
        }

        // Create bean information
        setObject(obj);

    }

    private void setObject(final Object c) {
        theObject = c;
        listBeanPropertyCommands();
        listMethodCommands();
    }

    public Set<String> getAvailableCommands() {
        final List<String> l = new ArrayList<String>();
        l.addAll(commands.keySet());
        Collections.sort(l);
        return new LinkedHashSet<String>(l);
    }

    private void listMethodCommands() {

        final Method[] methods = theObject.getClass().getMethods();
        for (final Method method : methods) {
            final Method m = method;
            if (!isBeanGetter(m) && !isBeanSetter(m)) {

                String un = m.getName();
                if (isIgnored(un) || !isParamTypesOkForConsole(m.getParameterTypes())) {
                    continue;
                }

                // Make unique. Try param names first.
                if (commands.containsKey(un)) {
                    un += paramsToShortString(m.getParameterTypes());
                }

                // Fallback to appending number
                int j = 1;
                while (commands.containsKey(un)) {
                    un = m.getName() + (j++);
                }

                final Caller cmd = m.getParameterTypes().length == 0 ? new Caller(theObject, m.getName(), null, m.getParameterTypes()) : new Caller(theObject,
                        null, m.getName(), m.getParameterTypes());
                commands.put(un, cmd);
            }
        }
    }

    private String paramsToShortString(final Class<?>[] parameterTypes) {
        StringBuilder s = new StringBuilder();
        if (parameterTypes != null) {
            for (final Class<?> c : parameterTypes) {
                s.append(c.getSimpleName().substring(0, 3));
            }
        }
        return s.toString();
    }

    private boolean isBeanSetter(final Method m) {
        if (m.getName().startsWith("set")) {
            Method setMethod = null;
            try {
                final String n = "g" + m.getName().substring(1);
                setMethod = m.getDeclaringClass().getMethod(n);
            } catch (final SecurityException e) {
            } catch (final NoSuchMethodException e) {
            }
            if (setMethod == null) {
                try {
                    final String n = "is" + m.getName().substring(3);
                    setMethod = m.getDeclaringClass().getMethod(n);
                } catch (final SecurityException e) {
                } catch (final NoSuchMethodException e) {
                }
            }
            return setMethod != null;
        }
        return false;
    }

    private boolean isBeanGetter(final Method m) {
        final String n = m.getName();
        return (n.startsWith("is") && n.length() > 2) || (n.startsWith("get") && n.length() > 2);
    }

    /**
     * <p>
     * Perform introspection on a Java Bean class to find its properties.
     * </p>
     *
     * <p>
     * Note : This version only supports introspectable bean properties and
     * their getter and setter methods. Stand-alone <code>is</code> and
     * <code>are</code> methods are not supported.
     * </p>
     *
     * @param beanClass
     *            the Java Bean class to get properties for.
     * @return an ordered map from property names to property descriptors
     */
    static LinkedHashMap<String, PropertyDescriptor> getPropertyDescriptors(final Class<?> beanClass) {
        final LinkedHashMap<String, PropertyDescriptor> pdMap = new LinkedHashMap<String, PropertyDescriptor>();

        // Try to introspect, if it fails, we just have an empty Item
        try {
            final BeanInfo info = Introspector.getBeanInfo(beanClass);
            final PropertyDescriptor[] pds = info.getPropertyDescriptors();

            // Add all the bean properties as MethodProperties to this Item
            for (final PropertyDescriptor pd : pds) {
                final Method getMethod = pd.getReadMethod();
                if ((getMethod != null) && getMethod.getDeclaringClass() != Object.class) {
                    pdMap.put(pd.getName(), pd);
                }
            }
        } catch (final java.beans.IntrospectionException ignored) {
        }

        return pdMap;
    }

    /**
     * List commands for the theObject.
     *
     * @return
     */
    private void listBeanPropertyCommands() {

        final LinkedHashMap<String, PropertyDescriptor> beanProperties = getPropertyDescriptors(theObject.getClass());
        // bean properties
        for (final PropertyDescriptor p : beanProperties.values()) {

            // Make unique
            final String name = p.getName();
            if (isIgnored(name)) {
                continue;
            }

            commands.put(name,
                    new Caller(theObject, p.getReadMethod().getName(), p.getWriteMethod() != null ? p.getWriteMethod().getName() : null,
                            p.getWriteMethod() != null ? p.getWriteMethod().getParameterTypes() : null));

            // beanCommands.put(un, p);
        }
    }

    public static boolean isParamTypesOkForConsole(final Class<?>[] cls) {
        if (cls == null) {
            return true;
        }
        for (int i = 0; i < cls.length; i++) {
            if (!isParamTypeOkForConsole(cls[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isParamTypeOkForConsole(final Class<?> cls) {
        return cls.isEnum() || cls == Boolean.class || cls == boolean.class || cls == long.class || cls == Long.class || cls == int.class
                || cls == Integer.class || cls == byte.class || cls == Byte.class || cls == float.class || cls == Float.class || cls == double.class
                || cls == Double.class || cls == String.class;
    }

    public boolean isIgnored(final String commandName) {
        return getIgnoredCommands().contains(commandName);
    }

    protected static Object[] argvToParams(final String[] argv, final Class<?>[] pt) {
        if (pt.length != argv.length - 1) {
            throw new IllegalArgumentException("Invalid number of parameters");
        }
        final Object[] args = new Object[argv.length - 1];
        for (int j = 1; j < argv.length; j++) {
            if (pt[j - 1] == String.class) {
                args[j - 1] = argv[j];
            } else if (pt[j - 1] == Byte.class || pt[j - 1] == byte.class) {
                args[j - 1] = Byte.parseByte(argv[j]);
            } else if (pt[j - 1] == Character.class || pt[j - 1] == char.class) {
                args[j - 1] = argv[j].charAt(0);
            } else if (pt[j - 1] == Long.class || pt[j - 1] == long.class) {
                args[j - 1] = Long.parseLong(argv[j]);
            } else if (pt[j - 1] == Integer.class || pt[j - 1] == int.class) {
                args[j - 1] = Integer.parseInt(argv[j]);
            } else if (pt[j - 1] == Boolean.class || pt[j - 1] == boolean.class) {
                args[j - 1] = Boolean.parseBoolean(argv[j]);
            } else if (pt[j - 1] == Float.class || pt[j - 1] == float.class) {
                args[j - 1] = Float.parseFloat(argv[j]);
            } else if (pt[j - 1] == Double.class || pt[j - 1] == double.class) {
                args[j - 1] = Float.parseFloat(argv[j]);
            } else if (pt[j - 1].isEnum()) {
                Field e;
                try {
                    e = pt[j - 1].getDeclaredField(argv[j].toUpperCase());
                    args[j - 1] = e.get(null);
                } catch (final Exception e1) {
                    throw new IllegalArgumentException("Enum not found: " + argv[j].toUpperCase(), e1);
                }
            }
        }
        return args;
    }

    public Class<?>[] getCommandParams(final String method) {
        if (commands.containsKey(method)) {
            final Caller m = commands.get(method);
            return m.getParameterTypes() != null ? m.getParameterTypes() : new Class<?>[] {};
        }
        return new Class<?>[] {};
    }

    public void setIgnoredCommands(final Set<String> ignoredCommands) {
        this.ignoredCommands = ignoredCommands;
    }

    public Set<String> getIgnoredCommands() {
        return ignoredCommands;
    }

    /**
     * Method wrapper that serializes nicely.
     *
     */
    private static class Caller implements Console.Command, Serializable {

        private static final long serialVersionUID = -810707579200844512L;

        private final Object theObject;
        private final String readMethod;
        private final String writeMethod;
        private final Class<?>[] paramTypes;

        transient private Method rm;
        transient private Method wm;

        private Caller(final Object obj, final String read, final String write, final Class<?>[] params) {
            theObject = obj;
            readMethod = read;
            writeMethod = write;
            paramTypes = params;
        }

        private boolean isWritable() {
            return writeMethod != null && paramTypes != null && paramTypes.length > 0;
        }

        public boolean isReadable() {
            return readMethod != null;
        }

        public Object write(final Object[] params) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
                InvocationTargetException {
            if (wm == null) {
                wm = theObject.getClass().getMethod(writeMethod, paramTypes);
            }
            return wm.invoke(theObject, params);
        }

        public Object read() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            if (rm == null) {
                rm = theObject.getClass().getMethod(readMethod, new Class<?>[] {});
            }
            return rm.invoke(theObject);
        }

        public Object write(final String[] argv) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
                InvocationTargetException {
            return write(parseWriteParams(argv));
        }

        private Class<?>[] getParameterTypes() {
            return paramTypes;
        }

        private Object[] parseWriteParams(final String[] argv) throws SecurityException, NoSuchMethodException {
            if (wm == null) {
                wm = theObject.getClass().getMethod(writeMethod, paramTypes);
            }
            return argvToParams(argv, wm.getParameterTypes());
        }

        public Object execute(final Console console, final String[] argv) throws Exception {
            // public Object execute(final String[] argv) throws Exception {

            if (argv == null || argv.length < 1) {
                throw new IllegalArgumentException("Missing command");
            }

            if (argv.length > 1 && !isParamTypesOkForConsole(getParameterTypes())) {
                throw new IllegalArgumentException("Unsupported parameter types");
            }

            if (argv.length == 1 && isReadable()) {
                // Read
                return read();
            } else if (isWritable()) {
                // Write
                if (isReadable()) {
                    write(argv);
                    return read(); // Return the read method instead
                } else {
                    return write(argv); // return whatever is returned
                }

            }

            return null;
        }

        public String getUsage(final Console console, final String[] argv) {
            return null;
        }
    }

    public String getCommandUsage(final String[] argv) {
        return argv[0] + " " + paramsToString(getCommandParams(argv[0]));
    }

    private static String paramsToString(final Class<?>[] paramTypes) {
        StringBuilder res = new StringBuilder();
        if (paramTypes != null) {
            for (final Class<?> paramType : paramTypes) {
                res.append("<" + paramType.getSimpleName() + "> ");
            }
        }
        return res.toString();
    }

    public Set<String> getAvailableCommands(final Console console) {
        return getAvailableCommands();
    }

    public Command getCommand(final Console console, final String commandName) {
        return commands.get(commandName);
    }

}
