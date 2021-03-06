package fr.castor.web.client.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import fr.castor.web.app.enums.FeedbackMessageLevel;

public class FeedBackPanelEvent extends AbstractEvent {

    private String message;
    private FeedbackMessageLevel messageLevel;
    private boolean goToTop = true;

    public FeedBackPanelEvent(AjaxRequestTarget target) {
        super(target);
    }

    public FeedBackPanelEvent(AjaxRequestTarget target, String message, FeedbackMessageLevel messageLevel) {
        super(target);
        this.message = message;
        this.messageLevel = messageLevel;
    }

    public FeedBackPanelEvent(AjaxRequestTarget target, String message, FeedbackMessageLevel messageLevel, boolean goToTop) {
        super(target);
        this.message = message;
        this.messageLevel = messageLevel;
        this.goToTop = goToTop;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message == null ? "" : message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the messageLevel
     */
    public FeedbackMessageLevel getMessageLevel() {
        return messageLevel;
    }

    /**
     * @param messageLevel
     *            the messageLevel to set
     */
    public void setMessageLevel(FeedbackMessageLevel messageLevel) {
        this.messageLevel = messageLevel;
    }

    public boolean isGoToTop() {
        return goToTop;
    }

    public void setGoToTop(boolean goToTop) {
        this.goToTop = goToTop;
    }
}
