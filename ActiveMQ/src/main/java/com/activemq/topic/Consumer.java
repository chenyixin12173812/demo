package com.activemq.topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;

public class Consumer {
    private static final String URL = "tcp://localhost:61616";

    private static final String TOPIC_NAME = "TestTopic";

    public static void main(String[] args) {

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);

        try {
            Connection connection = connectionFactory.createConnection("admin","admin");
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(TOPIC_NAME);
            MessageConsumer consumer = session.createConsumer(topic);
            // method1
//            while (true) {
//                System.out.println("consumer massage" + ((TextMessage) consumer.receive()).getText());
//            }
            //method2
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        System.out.println("consumer massage" + ((TextMessage) message).getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
            System.in.read();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
