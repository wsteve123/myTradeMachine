/*
 * AnyWrapper.java
 *
 */
package ibApiSrc;


public interface AnyWrapper {
    void error( Exception e);
    void error( String str);
    void error(int id, int errorCode, String errorMsg);
    void connectionClosed();
}

