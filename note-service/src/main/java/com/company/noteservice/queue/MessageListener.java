package com.company.noteservice.queue;

import com.company.noteservice.NoteServiceApplication;
import com.company.noteservice.model.Note;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageListener {

    @RabbitListener(queues = NoteServiceApplication.QUEUE_NAME)
    public void receiveMessage(Note note) {
        System.out.println("RECEIVED NOTE: " + note.getNoteId());
    }
}
