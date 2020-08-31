package com.bruin.config;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

//@Component
public class FastJsonMessageConverter extends AbstractMessageConverter {
    private static Logger LOGGER = LoggerFactory.getLogger(FastJsonMessageConverter.class);

    public static final String DEFAULT_CHARSET = "UTF-8";

    private volatile String defaultCharset = DEFAULT_CHARSET;

    public FastJsonMessageConverter() {
        super();
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = (defaultCharset != null) ? defaultCharset : DEFAULT_CHARSET;
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        try {
            LOGGER.debug("FastJsonMessageConverter.fromMessage");
            return new String(message.getBody(), defaultCharset);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("FastJsonMessageConverter.fromMessage occur error",e);
            return message.getBody();
        }
    }

    public <T> T fromMessage(Message message, T t) {
        String json = "";
        try {
            json = new String(message.getBody(),defaultCharset);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("FastJsonMessageConverter.fromMessage occur error",e);
        }
        return (T) JSONObject.parseObject(json, t.getClass());
    }

    @Override
    protected Message createMessage(Object objectToConvert, MessageProperties messageProperties)
        throws MessageConversionException {
        byte[] bytes = null;
        try {
            String jsonString;
            if(!(objectToConvert instanceof String)) {
                jsonString = JSONObject.toJSONString(objectToConvert);
            }else{
                jsonString=(String)objectToConvert;
            }
            bytes = jsonString.getBytes(this.defaultCharset);
        } catch (UnsupportedEncodingException e) {
            throw new MessageConversionException(
                "Failed to convert Message content", e);
        }
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(this.defaultCharset);
        if (bytes != null) {
            messageProperties.setContentLength(bytes.length);
        }
        return new Message(bytes, messageProperties);
    }
}
