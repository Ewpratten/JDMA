package ca.retrylife.jdma;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;

import ca.retrylife.jdma.annotations.Pointer;
import ca.retrylife.jdma.util.Values;
import sun.misc.Unsafe;

/**
 * A C-like Direct memory Access API
 */
public class DMA {

    // Access to Unsafe
    private static final Unsafe unsafe = UnsafeAPI.get();

    private DMA() {
    }

    /**
     * Allocate SIZE bytes off-heap
     * 
     * @param size Bytes to allocate
     * @return Address of the first byte
     */
    public static @Pointer long malloc(long size) {
        return unsafe.allocateMemory(size);
    }

    /**
     * Free an allocated portion of memory
     * 
     * @param address Base address
     */
    public static void free(@Pointer long address) {
        unsafe.freeMemory(address);
    }

    /**
     * Copy N bytes from src to dest
     * 
     * @param dest Destination base address
     * @param src  Source base address
     * @param n    Number of bytes
     */
    public static void memcpy(@Pointer long dest, @Pointer long src, long n) {
        unsafe.copyMemory(src, dest, n);
    }

    /**
     * Copies a value onto the first n bytes of the object pointed to by dest
     * 
     * @param dest  Base address
     * @param value Value
     * @param n     Number of bytes to write
     */
    public static void memset(@Pointer long dest, byte value, long n) {
        unsafe.setMemory(dest, n, value);
    }

    /**
     * Compares the first N bytes of memory pointed to by A and B
     * 
     * @param a Addr A
     * @param b Addr B
     * @param n Number of bytes
     * @return Negative if A&lt;B, positive if B&gt;A, else 0
     */
    public static int memcmp(@Pointer long a, @Pointer long b, long n) {
        for (int i = 0; i < n; i++, a++, b++) {
            if (unsafe.getByte(a) < unsafe.getByte(b)) {
                return -1;
            } else if (unsafe.getByte(a) > unsafe.getByte(b)) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * Get a portion of memory as a byte array
     * 
     * @param address Base address
     * @param size    Number of bytes to fetch
     * @return Byte array
     */
    public static byte[] getByteArray(@Pointer long address, int size) {

        // Allocate an array
        byte[] output = new byte[size];

        for (int i = 0; i < size; i++) {
            output[i] = peek(address + i);
        }

        return output;
    }

    /**
     * Get the byte at an address
     * 
     * @param address Address
     * @return Value
     */
    public static byte peek(@Pointer long address) {
        return unsafe.getByte(address);
    }

    /**
     * Write a byte to an address
     * 
     * @param address Address
     * @param val     Value
     */
    public static void poke(@Pointer long address, byte val) {
        unsafe.putByte(address, val);
    }

    /**
     * Get the shallow size of an object
     * 
     * @param obj Object
     * @return Size in bytes
     */
    public static long sizeof(Object obj) {
        HashSet<Field> fields = new HashSet<Field>();

        // Ref to the object's direct class
        Class<?> clazz = obj.getClass();

        // Walk up the superclass list and grab all fields
        while (clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        // Get offset
        long maxSize = 0;
        for (Field field : fields) {
            long offset = unsafe.objectFieldOffset(field);
            if (offset > maxSize) {
                maxSize = offset;
            }
        }

        // Return the padded size
        return ((maxSize / 8) + 1) * 8;
    }

    /**
     * Get the address of an object
     * 
     * @param obj Object
     * @return Address
     */
    public static @Pointer long addressOf(Object obj) {
        // Store the object in an array, then fetch the first element's base address
        Object[] array = new Object[] { obj };
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        return Values.intToULong(unsafe.getInt(array, baseOffset));
    }

    /**
     * Get an Object by it's base address
     * 
     * @param address Address of object
     * @return Object
     */
    public static Object getObjectByAddress(@Pointer long address) {
        // Uses array base address to grab an object
        Object[] array = new Object[] { null };
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        unsafe.putLong(array, baseOffset, address);
        return array[0];
    }

    /**
     * Get an Object by it's base address
     * 
     * @param <T>     Object type
     * @param address Address of object
     * @param clazz   Class of the object
     * @return Object
     */
    @SuppressWarnings("unchecked")
    public static <T> T getObjectByAddress(@Pointer long address, Class<T> clazz) {
        return (T) getObjectByAddress(address);
    }

    public static byte readByte(@Pointer long address) {
        return unsafe.getByte(address);
    }

    public static void writeByte(@Pointer long address, byte value) {
        unsafe.putByte(address, value);
    }

    public static short readShort(@Pointer long address) {
        return unsafe.getShort(address);
    }

    public static void writeShort(@Pointer long address, short value) {
        unsafe.putShort(address, value);
    }

    public static char readChar(@Pointer long address) {
        return unsafe.getChar(address);
    }

    public static void writeChar(@Pointer long address, char value) {
        unsafe.putChar(address, value);
    }

    public static int readInt(@Pointer long address) {
        return unsafe.getInt(address);
    }

    public static void writeInt(@Pointer long address, int value) {
        unsafe.putInt(address, value);
    }

    public static long readLong(@Pointer long address) {
        return unsafe.getLong(address);
    }

    public static void writeLong(@Pointer long address, long value) {
        unsafe.putLong(address, value);
    }

    public static float readFloat(@Pointer long address) {
        return unsafe.getFloat(address);
    }

    public static void writeFloat(@Pointer long address, float value) {
        unsafe.putFloat(address, value);
    }

    public static double readDouble(@Pointer long address) {
        return unsafe.getDouble(address);
    }

    public static void writeDouble(@Pointer long address, double value) {
        unsafe.putDouble(address, value);
    }

}