package com.ensolvers.webfluxpoc.configuration;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class PageableHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String DEFAULT_PAGE = "0";
  private static final String DEFAULT_PAGE_SIZE = "50";
  private static final Integer MAX_SIZE = 100;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return Pageable.class.equals(parameter.getParameterType());
  }

  // TODO: Implement paging
  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    return null;
  }
}
