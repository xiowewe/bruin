package com.bruin.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.util.Pool;
import de.javakaffee.kryoserializers.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.awt.print.Book;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationHandler;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: xiongwenwen   2019/12/7 18:10
 */
@Component
public class KryoSerializer<T> implements RedisSerializer<T> {
    private static final int BUFFER_SIZE = 2048;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, 8) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.register(Book.class);
            kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());
            kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
            kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
            kryo.register(InvocationHandler.class, new JdkProxySerializer());
            kryo.register(BigDecimal.class, new DefaultSerializers.BigDecimalSerializer());
            kryo.register(BigInteger.class, new DefaultSerializers.BigIntegerSerializer());
            kryo.register(Pattern.class, new RegexSerializer());
            kryo.register(BitSet.class, new BitSetSerializer());
            kryo.register(URI.class, new URISerializer());
            kryo.register(UUID.class, new UUIDSerializer());
            UnmodifiableCollectionsSerializer.registerSerializers(kryo);
            SynchronizedCollectionsSerializer.registerSerializers(kryo);
            kryo.register(HashMap.class);
            kryo.register(ArrayList.class);
            kryo.register(LinkedList.class);
            kryo.register(HashSet.class);
            kryo.register(TreeSet.class);
            kryo.register(Hashtable.class);
            kryo.register(Date.class);
            kryo.register(Calendar.class);
            kryo.register(ConcurrentHashMap.class);
            kryo.register(SimpleDateFormat.class);
            kryo.register(GregorianCalendar.class);
            kryo.register(Vector.class);
            kryo.register(BitSet.class);
            kryo.register(StringBuffer.class);
            kryo.register(StringBuilder.class);
            kryo.register(Object.class);
            kryo.register(Object[].class);
            kryo.register(String[].class);
            kryo.register(byte[].class);
            kryo.register(char[].class);
            kryo.register(int[].class);
            kryo.register(float[].class);
            kryo.register(double[].class);
            return kryo;
        }
    };
    private static Pool<Output> outputPool = new Pool<Output>(true, false, 16) {
        @Override
        protected Output create() {
            return new Output(BUFFER_SIZE, -1);
        }
    };
    private static Pool<Input> inputPool = new Pool<Input>(true, false, 16) {
        @Override
        protected Input create() {
            return new ByteBufferInput(BUFFER_SIZE);
        }
    };
    private Class<T> clazz;

    public KryoSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (null == t) {
            return EMPTY_BYTE_ARRAY;
        }
        Kryo kryo = null;
        Output output = null;
        byte[] bytes;
        try {
            output = outputPool.obtain();
            kryo = kryoPool.obtain();
            kryo.writeClassAndObject(output, t);
            output.flush();
            return output.toBytes();
        } finally {
            if (output != null) {
                outputPool.free(output);
            }
            if (kryo != null) {
                kryoPool.free(kryo);
            }
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        Kryo kryo = null;
        Input input = null;
        try {
            input = inputPool.obtain();
            input.setInputStream(new ByteArrayInputStream(bytes));
            kryo = kryoPool.obtain();
            return (T) kryo.readClassAndObject(input);
        } finally {
            if (input != null) {
                inputPool.free(input);
            }
            if (kryo != null) {
                kryoPool.free(kryo);
            }
        }
    }
}
