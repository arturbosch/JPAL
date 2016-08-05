package io.gitlab.arturbosch.jpal.internal

import groovy.transform.CompileStatic

/**
 * Took the base line from spring roo project and extended it.
 *
 * @author artur
 */
@CompileStatic
final class JdkHelper {

	private static final List<String> javaTypes = new ArrayList<String>();
	private static final List<String> javaCollections = new ArrayList<String>();

	static {
		javaTypes.add("Appendable");
		javaTypes.add("ArrayList");
		javaTypes.add("Array");
		javaTypes.add("Arrays");
		javaTypes.add("BigDecimal");
		javaTypes.add("BigInteger");
		javaTypes.add("Blob");
		javaTypes.add("ByteArrayInputStream");
		javaTypes.add("Calendar");
		javaTypes.add("CharSequence");
		javaTypes.add("Clob");
		javaTypes.add("Cloneable");
		javaTypes.add("Collection");
		javaTypes.add("Comparable");
		javaTypes.add("Iterable");
		javaTypes.add("Readable");
		javaTypes.add("Runnable");
		javaTypes.add("Boolean");
		javaTypes.add("Byte");
		javaTypes.add("Character");
		javaTypes.add("Class");
		javaTypes.add("ClassLoader");
		javaTypes.add("Compiler");
		javaTypes.add("Date");
		javaTypes.add("DateFormat");
		javaTypes.add("Double");
		javaTypes.add("Enum");
		javaTypes.add("Exception");
		javaTypes.add("Float");
		javaTypes.add("GregorianCalendar");
		javaTypes.add("HashSet");
		javaTypes.add("HashMap");
		javaTypes.add("InheritableThreadLocal");
		javaTypes.add("Integer");
		javaTypes.add("Iterator");
		javaTypes.add("Iterable");
		javaTypes.add("List");
		javaTypes.add("Long");
		javaTypes.add("Math");
		javaTypes.add("Map");
		javaTypes.add("Number");
		javaTypes.add("Object");
		javaTypes.add("Package");
		javaTypes.add("Path");
		javaTypes.add("File");
		javaTypes.add("Process");
		javaTypes.add("ProcessBuilder");
		javaTypes.add("Random");
		javaTypes.add("Runtime");
		javaTypes.add("RuntimePermission");
		javaTypes.add("Scanner");
		javaTypes.add("SecurityManager");
		javaTypes.add("SecureRandom");
		javaTypes.add("Serializable");
		javaTypes.add("Set");
		javaTypes.add("SimpleDateFormat");
		javaTypes.add("Short");
		javaTypes.add("StackTraceElement");
		javaTypes.add("StrictMath");
		javaTypes.add("String");
		javaTypes.add("StringBuilder");
		javaTypes.add("StringBuffer");
		javaTypes.add("SuppressWarnings");
		javaTypes.add("System");
		javaTypes.add("Timestamp");
		javaTypes.add("Thread");
		javaTypes.add("ThreadGroup");
		javaTypes.add("ThreadLocal");
		javaTypes.add("Throwable");
		javaTypes.add("Void");
		javaTypes.add("ArithmeticException");
		javaTypes.add("ArrayIndexOutOfBoundsException");
		javaTypes.add("ArrayStoreException");
		javaTypes.add("ClassCastException");
		javaTypes.add("ClassNotFoundException");
		javaTypes.add("CloneNotSupportedException");
		javaTypes.add("EnumConstantNotPresentException");
		javaTypes.add("Exception");
		javaTypes.add("IllegalAccessException");
		javaTypes.add("IllegalArgumentException");
		javaTypes.add("IllegalMonitorStateException");
		javaTypes.add("IllegalStateException");
		javaTypes.add("IllegalThreadStateException");
		javaTypes.add("IndexOutOfBoundsException");
		javaTypes.add("InstantiationException");
		javaTypes.add("InterruptedException");
		javaTypes.add("NegativeArraySizeException");
		javaTypes.add("NoSuchFieldException");
		javaTypes.add("NoSuchMethodException");
		javaTypes.add("NullPointerException");
		javaTypes.add("NumberFormatException");
		javaTypes.add("RuntimeException");
		javaTypes.add("SecurityException");
		javaTypes.add("StringIndexOutOfBoundsException");
		javaTypes.add("TypeNotPresentException");
		javaTypes.add("UnsupportedOperationException");
		javaTypes.add("AbstractMethodError");
		javaTypes.add("AssertionError");
		javaTypes.add("ClassCircularityError");
		javaTypes.add("ClassFormatError");
		javaTypes.add("Error");
		javaTypes.add("ExceptionInInitializerError");
		javaTypes.add("IllegalAccessError");
		javaTypes.add("IncompatibleClassChangeError");
		javaTypes.add("InstantiationError");
		javaTypes.add("InternalError");
		javaTypes.add("LinkageError");
		javaTypes.add("NoClassDefFoundError");
		javaTypes.add("NoSuchFieldError");
		javaTypes.add("NoSuchMethodError");
		javaTypes.add("OutOfMemoryError");
		javaTypes.add("StackOverflowError");
		javaTypes.add("ThreadDeath");
		javaTypes.add("UnknownError");
		javaTypes.add("UnsatisfiedLinkError");
		javaTypes.add("UnsupportedClassVersionError");
		javaTypes.add("VerifyError");
		javaTypes.add("VirtualMachineError");
		// java.time
		javaTypes.add("Clock");
		javaTypes.add("Duration");
		javaTypes.add("Instant");
		javaTypes.add("LocalDate");
		javaTypes.add("LocalTime");
		javaTypes.add("Month");
		javaTypes.add("Year");
		javaTypes.add("YearMonth");
		javaTypes.add("ZoneId");
		javaTypes.add("ZoneOffset");
		javaTypes.add("DayOfWeek");
		javaTypes.add("Period");
		javaTypes.add("OffsetDateTime");
		javaTypes.add("OffsetTime");
		javaTypes.add("MonthDay");
		javaTypes.add("LocalDateTime");
		javaTypes.add("ZonedDateTime");
	}

	static {
		javaCollections.add("List")
		javaCollections.add("Array")
		javaCollections.add("ArrayDeque")
		javaCollections.add("Comparator")
		javaCollections.add("Deque")
		javaCollections.add("Enumeration")
		javaCollections.add("Collection")
		javaCollections.add("Map")
		javaCollections.add("Set")
		javaCollections.add("TreeSet")
		javaCollections.add("HashMap")
		javaCollections.add("HashSet")
		javaCollections.add("Hashtable")
		javaCollections.add("Formattable")
		javaCollections.add("ArrayList")
		javaCollections.add("Iterable")
		javaCollections.add("Iterator")
		javaCollections.add("ListIterator")
		javaCollections.add("LinkedList")
		javaCollections.add("LinkedHashMap")
		javaCollections.add("LinkedHashSet")
		javaCollections.add("Observable")
		javaCollections.add("Observer")
		javaCollections.add("Optional")
		javaCollections.add("PrimitiveIterator")
		javaCollections.add("Queue")
		javaCollections.add("Stack")
		javaCollections.add("StringTokenizer")
		javaCollections.add("TreeMap")
		javaCollections.add("Vector")
		javaCollections.add("ConcurrentHashMap")
	}

	private JdkHelper() {}

	static boolean isPartOfJava(String simpleTypeName) {
		return javaTypes.contains(simpleTypeName) || javaCollections.any { simpleTypeName.startsWith(it) }
	}
}
