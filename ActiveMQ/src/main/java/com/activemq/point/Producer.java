package com.activemq.point;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Producer {


    private static final String URL = "tcp://localhost:61616";

    private static final String QUEQUE_NAME = "TestQueque";

    public static void main(String[] args) {

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);

        try {
            Connection connection = connectionFactory.createConnection("admin","admin");
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination queue = session.createQueue(QUEQUE_NAME);
            MessageProducer producer = session.createProducer(queue);
            for (int i = 0; i < 5;i++ ){
                TextMessage textMessage = session.createTextMessage("my queque massage" + i);
                System.out.println("send text massage is" +textMessage);
                producer.send(textMessage);
            }
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
