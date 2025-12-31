package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.FormElement;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeUri;

public class FormPanel extends HTMLPanel implements HasFormSubmitHandlers, HasBeforeFormSubmitHandlers, HasCancelHandlers {

    public enum Method {
        GET,
        POST
    }

    public FormPanel(String str) {
        super("form", str);
        addFormHandler(FormElement.as(getElement()));
    }

    private native void addFormHandler(FormElement formElement) /*-{
        var self = this;
        formElement.addEventListener("submit", function(e){
            e.preventDefault();
            self.@com.google.gwt.user.client.ui.FormPanel::startSubmit()();
        });
    }-*/;


    public void setMethod(Method method) {
        asElement().setAttribute("method", method.name());
    }

    public Method getMethod() {
        return Method.valueOf(getElement().getAttribute("method"));
    }

    public void setAction(SafeUri action) {
        asElement().setAttribute("action", action.asString());
    }

    public String getAction() {
       return asElement().getAction();
    }

    public void reset() {
        asElement().reset();
    }

    public void submit(){
        asElement().submit();
    }

    private void startSubmit() {
       BeforeFormSubmitEvent evt =  BeforeFormSubmitEvent.fire(this);
       if(evt.isCanceled()) {
           fireEvent(new CancelEvent());
       }
       else {
           this.submit();
           FormSubmitEvent.fire(this);
       }
    }

    private FormElement asElement() {
        return FormElement.as(getElement());
    }

    @Override
    public HandlerRegistration addFormSubmitHandler(FormSubmitHandler handler) {
        return addHandler(handler, FormSubmitEvent.getType());
    }

    @Override
    public HandlerRegistration addBeforeFormSubmitHandler(BeforeFormSubmitHandler handler) {
        return addHandler(handler, BeforeFormSubmitEvent.getType());
    }

    @Override
    public HandlerRegistration addCancelHandler(CancelHandler handler) {
        return addHandler(handler, CancelEvent.getType());
    }

}
