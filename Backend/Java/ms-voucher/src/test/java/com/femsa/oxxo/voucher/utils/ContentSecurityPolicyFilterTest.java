package com.femsa.oxxo.voucher.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ContentSecurityPolicyFilterTest {
	
    @Test
    public void shouldSetContentSecurityPolicyHeader() throws Exception {
        ContentSecurityPolicyFilter filter = new ContentSecurityPolicyFilter();

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletRequest request = mock(ServletRequest.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader("Content-Security-Policy","default-src 'none';");
        verify(chain).doFilter(request, response);
    }

}
