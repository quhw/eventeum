package net.consensys.eventeum.utils;

import net.consensys.eventeum.integration.RocketSettings;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.List;

public class RocketTemplate<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(RocketTemplate.class);

    private DefaultMQProducer defaultMQProducer;

    public RocketTemplate(RocketSettings rocketSettings) {
        this.defaultMQProducer = new DefaultMQProducer(rocketSettings.getGroupId());
        this.defaultMQProducer.setNamesrvAddr(rocketSettings.getNameAddresses());
    }

    public void init() throws Exception {
        defaultMQProducer.start();
    }

    public void destroy() {
        defaultMQProducer.shutdown();
    }

    public void send(String topic, K key, @Nullable V data) {
        try {
            final String msgStr = JSON.stringify(data);
            Message message = new Message(topic, msgStr.getBytes("UTF-8"));
            defaultMQProducer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    if (mqs.size() > 0) {
                        if (arg != null) {
                            return mqs.get(Math.abs(arg.hashCode()) % mqs.size());
                        } else {
                            return mqs.get(0);
                        }
                    } else {
                        return null;
                    }
                }
            }, key);
        } catch (Exception e) {
            LOG.error("Failed to message", e);
        }
    }
}
