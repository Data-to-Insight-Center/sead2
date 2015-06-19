package org.sead.workflow.exception;


public class SeadWorkflowException extends RuntimeException{

    private String message = null;

    public SeadWorkflowException(){
        super();
    }

    public SeadWorkflowException(String msg){
        super(msg);
        this.message = msg;
    }

    public SeadWorkflowException(Exception e){
        super(e);
    }

    public SeadWorkflowException(String msg, Exception e){
        super(msg, e);
        this.message = msg;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
