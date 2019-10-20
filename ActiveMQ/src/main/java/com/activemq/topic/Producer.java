package com.activemq.topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Producer {

    private static final String URL = "tcp://localhost:61616";

    private static final String TOPIC_NAME = "TestTopic";

    public static void main(String[] args) {

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);

        try {
            Connection connection = connectionFactory.createConnection("admin","admin");
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(TOPIC_NAME);
            MessageProducer producer = session.createProducer(topic);
            for (int i = 0; i < 5;i++ ){
                TextMessage textMessage = session.createTextMessage("my topic massage" + i);
                System.out.println("send topic text massage is" +textMessage);
                producer.send(textMessage);
            }
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
