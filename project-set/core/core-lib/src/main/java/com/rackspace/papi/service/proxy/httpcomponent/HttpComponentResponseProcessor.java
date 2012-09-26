package com.rackspace.papi.service.proxy.httpcomponent;

import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import com.rackspace.papi.http.proxy.common.AbstractResponseProcessor;
import org.apache.http.util.EntityUtils;

public class HttpComponentResponseProcessor extends AbstractResponseProcessor {

    private final HttpResponse httpResponse;

    public HttpComponentResponseProcessor(String proxiedHostUrl, String requestHostPath, HttpResponse httpResponse, HttpServletResponse response, HttpComponentResponseCodeProcessor responseCode) {
        super(proxiedHostUrl, requestHostPath, response, responseCode.getCode());
        this.httpResponse = httpResponse;
    }

    @Override
    protected void setResponseHeaders() throws IOException {
        for (Header header : httpResponse.getAllHeaders()) {
            addHeader(header.getName(), header.getValue());
        }
    }

    @Override
    protected void setResponseBody() throws IOException {
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            if (getResponse() instanceof MutableHttpServletResponse) {
                MutableHttpServletResponse mutableResponse = (MutableHttpServletResponse) getResponse();
                mutableResponse.setInputStream(new HttpComponentInputStream(entity));
            } else {
                final OutputStream clientOut = getResponse().getOutputStream();
                entity.writeTo(clientOut);
                clientOut.flush();
                EntityUtils.consume(entity);
            }
        }

    }

    @Override
    protected String getResponseHeaderValue(String headerName) throws com.rackspace.papi.http.proxy.HttpException {
        final Header[] headerValues = httpResponse.getHeaders(headerName);
        if (headerValues == null || headerValues.length == 0) {
            return null;
        }

        return headerValues[0].getValue();
    }
}