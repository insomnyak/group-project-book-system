package com.company.noteservice.queue;

import com.company.noteservice.NoteServiceApplication;
import com.company.noteservice.service.ServiceLayer;
import com.company.queue.shared.viewmodel.NoteViewModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.nio.charset.Charset;

@Service
public class MessageListener {

    @Autowired
    ServiceLayer sl;

    @Autowired
    ObjectMapper mapper;

    @RabbitListener(queues = NoteServiceApplication.QUEUE_NAME)
    public NoteViewModel receiveMessageToAddUpdateNote(Message message,
                                                       @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String key)
            throws IOException {

        String body = new String(message.getBody(), Charset.defaultCharset().name());
        System.out.println("RECEIVED || key: " + key + " | body: " + body);
        if (key.matches("^note[.]add[.].*$")) {
            NoteViewModel nvm = mapper.readValue(body, NoteViewModel.class);
            return sl.save(nvm);
        } else if (key.matches("^note[.]update[.].*$")) {
            NoteViewModel nvm = mapper.readValue(body, NoteViewModel.class);
            sl.update(nvm);
            return null;
        } else if (key.matches("^note[.]delete[.].*$")) {
            sl.remove(Integer.parseInt(body));
            return null;
        } else if (key.matches("^note[.]deleteByBookId[.].*$")) {
            sl.removeByBookId(Integer.parseInt(body));
            return null;
        } else {
            return null;
        }
    }
}
