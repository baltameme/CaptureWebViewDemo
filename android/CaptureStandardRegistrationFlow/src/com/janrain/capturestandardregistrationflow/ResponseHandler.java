/**
 * 
 */
package com.janrain.capturestandardregistrationflow;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;

/**
 * @author mitch
 * 
 */
public class ResponseHandler implements Observer {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable obj, Object arg) {
        if (arg instanceof JSONArray) {
            final JSONArray jArray = (JSONArray) arg;
            if (jArray != null) {
                // TODO: Do something with the jArray...
            }
        }
    }
}
