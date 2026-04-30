package org.apache.fop.accessibility;

import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;

public class AccessibilityMissAlternateTextErrorEventListener implements EventListener {

    @Override
    public void processEvent(Event event) {
        if (event.getEventID().equals("org.apache.fop.accessibility.AccessibilityEventProducer.missingAlternateTextError")) {

            String msg = EventFormatter.format(event);

            throw new RuntimeException("Strict Accessibility Error: " + msg);
        }
    }
}