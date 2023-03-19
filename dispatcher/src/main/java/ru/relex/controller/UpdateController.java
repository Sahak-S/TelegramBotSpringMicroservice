package ru.relex.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.relex.service.UpdateProducer;
import ru.relex.utils.MessageUtils;

import static ru.relex.model.RabbitQueue.*;

@Component
@Slf4j
public class UpdateController {

    private TelegramBot telegramBot;
    private  MessageUtils messageUtils;
    private  UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }


    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Полученное обновление является нулевым");
        }
        if (update.getMessage() != null) {
            distributeMessagesByType(update);
            log.error("Получен неподдерживаемый тип сообщения");
        }
    }

    private void distributeMessagesByType(Update update) {
        var message = update.getMessage();
        if (message.getText() != null) {
            processTextMessage(update);
        } else if (message.getDocument() != null) {
            processDocMessages(update);
        } else if (message.getPhoto() != null) {
            processPhotoMessages(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Չաջակցվող հաղորդագրության տեսակը ստացվել է");
        setView(sendMessage);
    }

    private void setFileIsReceivedView(Update update){
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Ստացված ֆայլը մշակվում է");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processDocMessages(Update update) {
        updateProducer.product(DOC_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }

    private void processPhotoMessages(Update update) {
        updateProducer.product(PHOTO_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }

    private void processTextMessage(Update update) {
        updateProducer.product(TEXT_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }

}
