package ru.relex.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {
    void product(String rabbitQueue, Update update);
}
