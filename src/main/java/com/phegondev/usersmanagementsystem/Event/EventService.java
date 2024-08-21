package com.phegondev.usersmanagementsystem.Event;

import com.phegondev.usersmanagementsystem.Notifications.NotificationService;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private NotificationService notificationService;

    @Transactional
    public ResponseEntity<String> addEvent(Event event) {
        eventRepository.save(event);
        return new ResponseEntity<>("Event added successfully", HttpStatus.CREATED);
    }

    public List<Event> getUserEvents(Integer userId) {
        return eventRepository.findByUserId(userId);
    }

    @Transactional
    public ResponseEntity<String> updateEvent(Long id, Event updatedEvent, Integer userId) {
        Optional<Event> eventOptional = eventRepository.findByIdAndUserId(id, userId);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            event.setTitle(updatedEvent.getTitle());
            event.setLocation(updatedEvent.getLocation());
            event.setDateTime(updatedEvent.getDateTime());
            event.setLink(updatedEvent.getLink());
            event.setRemindMe(updatedEvent.isRemindMe());
            eventRepository.save(event);
            return new ResponseEntity<>("Event updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<String> deleteEvent(Long id, Integer userId) {
        Optional<Event> eventOptional = eventRepository.findByIdAndUserId(id, userId);
        if (eventOptional.isPresent()) {
            eventRepository.deleteById(id);
            return new ResponseEntity<>("Event deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
        }

    }
    @Scheduled(cron = "0 40 22 * * ?") // Run at 3:07 PM every day
    public void checkEventsForReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusDays(1);

        System.out.println("Scheduled task running at 3:07 PM. Current time: " + now);

        // Assuming events are stored in LocalDateTime format correctly in the DB
        List<Event> events = eventRepository.findAllByRemindMeTrueAndReminderSentFalseAndDateTimeBetween(now, reminderTime);

        System.out.println("Events found for reminder: " + events.size());

        for (Event event : events) {
            notificationService.sendReminder(event);
            event.setReminderSent(true); // Mark reminder as sent
            eventRepository.save(event);
            System.out.println("Reminder sent for event: " + event.getTitle());
        }
    }




}