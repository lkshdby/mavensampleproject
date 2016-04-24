
package com.ibm.scas.analytics.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This is a hack of com.google.inject.persist.PersistFilter where the IllegalStateException caused by
 * double-starting the PersistService is ignored.  This works around a bug in the guice-servlet package.
 * The problem is that the ContextListener needs to start the PersistService to do some
 * initialization work, but if you start it twice, the impl throws IllegalStateException.  Here we just
 * toss the exception away and continue.
 * 
 * The rest of the class is a copy of the original PersistFilter.
 * 
 * @author jkwong
 *
 */
@Singleton
public final class CPEPersistFilter implements Filter {
  private final UnitOfWork unitOfWork;
  private final PersistService persistService;

  @Inject
  public CPEPersistFilter(UnitOfWork unitOfWork, PersistService persistService) {
    this.unitOfWork = unitOfWork;
    this.persistService = persistService;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
	  try {
	    persistService.start();
	  } catch (IllegalStateException e) {
		  // ignore this
	  }
  }

  @Override
  public void destroy() {
    persistService.stop();
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
      final FilterChain filterChain) throws IOException, ServletException {

    unitOfWork.begin();
    try {
      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      unitOfWork.end();
    }
  }
}
