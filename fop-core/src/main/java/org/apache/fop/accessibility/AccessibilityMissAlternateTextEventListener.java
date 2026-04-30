package org.apache.fop.accessibility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;

public class AccessibilityMissAlternateTextEventListener implements EventListener {

    private static final Log log = LogFactory.getLog(AccessibilityMissAlternateTextEventListener.class);

    @Override
    public void processEvent(Event event) {
        if (event.getEventID().equals("org.apache.fop.accessibility.AccessibilityEventProducer.missingAlternateText")) {

            String msg = EventFormatter.format(event);

            log.warn(msg);
        }
    }
}