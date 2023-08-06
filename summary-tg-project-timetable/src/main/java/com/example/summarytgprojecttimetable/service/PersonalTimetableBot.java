package com.example.summarytgprojecttimetable.service;

import com.example.summarytgprojecttimetable.config.BotConfig;
import com.example.summarytgprojecttimetable.model.AddTaskProcess;
import com.example.summarytgprojecttimetable.model.TaskDto;
import com.example.summarytgprojecttimetable.model.UserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PersonalTimetableBot extends TelegramLongPollingBot {
    private BotConfig botConfig;
    private TaskOutboundService taskOutboundService;
    private List<AddTaskProcess> addTaskProcesses = new ArrayList<>();

    public PersonalTimetableBot(BotConfig botConfig, TaskOutboundService taskOutboundService) {
        this.botConfig = botConfig;
        this.taskOutboundService = taskOutboundService;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать"));
        listOfCommands.add(new BotCommand("/help", "Помощь по использованию бота"));
        listOfCommands.add(new BotCommand("/add_task", "Добавить задачу"));
        listOfCommands.add(new BotCommand("/get_tasks_for_today", "Список задач на сегодня"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {

        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (message.startsWith("/")) {
                switch (message) {
                    case "/start": {
                        String answer = "Бот запущен. Выберите команду из списка в боковом меню";
                        sendMessage(chatId, answer);
                        break;
                    }
                    case "/help": {
                        String answer = "Этот бот создан для создания напоминаний о необходимости выполнить " +
                                "поставленную задачу. Воспользуйтесь командой /add_task для добавления новой задачи, " +
                                "воспользуйтесь командой /get_tasks_for_day для получения списка задач на сегодня";
                        sendMessage(chatId, answer);
                        break;
                    }
                    case "/add_task": {
                        killAddTaskProcess(chatId);
                        addTaskProcesses.add(new AddTaskProcess(chatId));
                        sendMessage(chatId, "Введите задачу, напоминание о которой хотите сохранить");

                        log.info(String.format("Starting saving task process (chat id: %d)", chatId));

                        break;
                    }
                    case "/get_tasks_for_today": {
                        List<TaskDto> tasksForDay = taskOutboundService.getTasksForDay(chatId).getBody();
                        sendTasksForDay(chatId, tasksForDay);

                        log.info(String.format("Get all tasks for day command from user (chat id: %d)", chatId));

                        break;
                    }
                    default: {
                        sendMessage(chatId, "Неизвестная команда. Воспользуйтесь боковым меню для получения списка доступных команд");

                        log.warn(String.format("Unknown command from user (chat id: %d)", chatId));
                    }
                }
            } else if (isAddTaskProcessStarted(chatId)) {
                AddTaskProcess addTaskProcess = getAddTaskProcess(chatId);
                TaskDto taskDto = addTaskProcess.getTaskDto();

                if (!addTaskProcess.isTaskSaved()) {
                    taskDto.setTask(message);
                    addTaskProcess.setTaskSaved(true);
                    sendMessage(chatId, "Введите время, когда необходимо будет выполнить задачу в формате ДД.ММ.ГГГГ чч:мм");

                    log.info(String.format("Task text saved (chat id: %d)", chatId));
                } else if (!addTaskProcess.isTimeSaved()) {
                    try {
                        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                        LocalDateTime localDateTime = LocalDateTime.parse(message, format);

                        taskDto.setHaveToDoTime(localDateTime);
                        addTaskProcess.setTimeSaved(true);

                        UserData userData = new UserData(chatId, update.getMessage().getChat().getUserName());
                        taskDto.setUser(userData);

                        String answer = taskOutboundService.saveNewTask(taskDto).getBody();
                        sendMessage(chatId, answer);

                        log.info(String.format("Task time saved (chat id: %d)", chatId));

                        killAddTaskProcess(chatId);
                    } catch (RuntimeException e) {
                        sendMessage(chatId, "Неверный формат даты. Повторите ввод в формате ДД.ММ.ГГГГ чч:мм");

                        log.warn(String.format("Wrong date/time format (chat id: %d)", chatId));
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            Long taskId = Long.parseLong(callbackData);

            String answer = taskOutboundService.completeTask(taskId).getBody();

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            answerCallbackQuery.setText(answer);

            Message message = callbackQuery.getMessage();
            int messageId = message.getMessageId();

            DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), messageId);

            try {
                execute(answerCallbackQuery);
                execute(deleteMessage);

                log.info(String.format("Task (id: %d) completed (chat id: %d)", taskId, message.getChatId()));
            } catch (TelegramApiException e) {
                log.warn("Callback exception", e);
            }
        }
    }

    public void sendReminder(Long chatId, TaskDto taskDto) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton completedButton = new InlineKeyboardButton();
        completedButton.setText("Выполнено✅");
        completedButton.setCallbackData(String.valueOf(taskDto.getId()));

        List<InlineKeyboardButton> row = new ArrayList<>() {{
            add(completedButton);
        }};

        List<List<InlineKeyboardButton>> rows = new ArrayList<>() {{
            add(row);
        }};

        inlineKeyboardMarkup.setKeyboard(rows);

        String message = "Пора приступить к выполнению задачи:\n" + taskDto.getTask();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);

            log.info(String.format("Task (id: %d) reminder sent", taskDto.getId()));
        } catch (TelegramApiException e) {
            e.printStackTrace();

            log.warn(String.format("Exception in sending reminder (Task id: %d)", chatId));
        }
    }

    public void sendStatistics(Long chatId, List<TaskDto> completedTaskDtos) {
        StringBuilder sb = new StringBuilder();
        sb.append("За сегодня вы выполнили следующие задачи:\n\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (TaskDto taskDto : completedTaskDtos) {
            sb.append(taskDto.getTask() + "\nВыполнена в " + formatter.format(taskDto.getCompletedTime().toLocalTime()) + "\n\n");
        }

        sendMessage(chatId, sb.toString());

        log.info(String.format("Statistics sent (chat id: %d)", chatId));
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.warn(String.format("Some problems with sending message (chat id: %d message: %s)", chatId, textToSend));
        }

        log.info(String.format("Message sent (chat id: %d)", chatId));
    }

    private void sendTasksForDay(long chatId, List<TaskDto> taskDtos) {
        if (taskDtos.isEmpty()) {
            sendMessage(chatId, "Задач на сегодня не найдено");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Задачи на сегодня:\n\n");

            for (TaskDto taskDto : taskDtos) {
                sb.append(taskDto.getTask() + " - " + taskDto.getHaveToDoTime().toLocalTime() + "\n");
            }

            sendMessage(chatId, sb.toString());
        }

        log.info(String.format("Tasks for day sent (chat id: %d)", chatId));
    }

    private boolean isAddTaskProcessStarted(long chatId) {
        for (AddTaskProcess addTaskProcess : addTaskProcesses) {
            if (addTaskProcess.getChatId() == chatId) {
                return true;
            }
        }
        return false;
    }

    private AddTaskProcess getAddTaskProcess(long chatId) {
        for (AddTaskProcess addTaskProcess : addTaskProcesses) {
            if (addTaskProcess.getChatId() == chatId) {
                return addTaskProcess;
            }
        }
        return null;
    }

    private void killAddTaskProcess(long chatId) {
        if (isAddTaskProcessStarted(chatId)) {
            addTaskProcesses.remove(getAddTaskProcess(chatId));

            log.info(String.format("Adding task process killed (chat id: %d)", chatId));
        }
    }
}
