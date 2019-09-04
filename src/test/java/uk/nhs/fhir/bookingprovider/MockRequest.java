/*
 * Copyright 2018 NHS Digital.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.bookingprovider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

/**
 * Mock Class used for testing.
 *
 * @author tim.coates@nhs.net
 */
public class MockRequest implements HttpServletRequest {

    String queryString = null;
    String authHeader = null;
    String ifMatchHeader = null;
    HashMap<String, Object> attrs;

    /**
     * Main constructor...
     *
     * @param query
     * @param header
     */
    public MockRequest(String query, String header) {
        this.attrs = new HashMap();
        queryString = query;
        authHeader = header;
    }

    /**
     * Constructor for use where we're not checking the Auth token etc
     *
     */
    public MockRequest() {
        this.attrs = new HashMap();
        queryString = "";
        authHeader = "";
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        if (name.equals("If-Match")) {
            ifMatchHeader = value;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getQueryString() {
        return queryString;
    }

    /**
     *
     * @param name
     * @return
     */
    @Override
    public String getHeader(String name) {
        if (name.equals("Authorization")) {
            return authHeader;
        }
        if (name.equals("If-Match")) {
            return ifMatchHeader;
        }
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public StringBuffer getRequestURL() {
        StringBuffer strb = new StringBuffer(queryString + "/Slot");
        return strb;
    }

    /**
     * Allows us to set Attributes of the Request
     * @param name
     * @param o
     */
    @Override
    public void setAttribute(String name, Object o) {
        attrs.put(name, o);
        return;
    }

    /**
     * Gets a given attribute (which we've set as above)
     * @param name Name of the attribute we're looking for.
     * @return The value of that attribute.
     */
    @Override
    public Object getAttribute(String name) {
        return attrs.get(name);
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param env
     * @throws UnsupportedEncodingException
     */
    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public int getContentLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public long getContentLengthLong() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getContentType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     * @throws IOException
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param name
     * @return
     */
    @Override
    public String getParameter(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public Enumeration<String> getParameterNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param name
     * @return
     */
    @Override
    public String[] getParameterValues(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getScheme() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getServerName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     * @throws IOException
     */
    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param name
     */
    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param path
     * @return
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param path
     * @return
     */
    @Override
    public String getRealPath(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     * @throws IllegalStateException
     */
    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws IllegalStateException
     */
    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public Cookie[] getCookies() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param name
     * @return
     */
    @Override
    public long getDateHeader(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param name
     * @return
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param name
     * @return
     */
    @Override
    public int getIntHeader(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getMethod() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param role
     * @return
     */
    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getRequestURI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param create
     * @return
     */
    @Override
    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     */
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param username
     * @param password
     * @throws ServletException
     */
    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @throws ServletException
     */
    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param name
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Empty implementations here just to allow us to claim we implement the
     * interface
     *
     * @param <T>
     * @param handlerClass
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
