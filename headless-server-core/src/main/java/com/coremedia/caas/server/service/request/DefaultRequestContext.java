package com.coremedia.caas.server.service.request;

import com.coremedia.caas.service.request.RequestContext;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import javax.validation.constraints.NotNull;

public class DefaultRequestContext implements RequestContext {

  private final boolean isPreview;
  private ZonedDateTime requestTime;
  private ZonedDateTime nextDateTimeChange;
  private final Object nextDateTimeChangeLock = new Object();
  private final Map<String, Object> properties = new ConcurrentHashMap<>();


  public DefaultRequestContext(boolean isPreview) {
    this.isPreview = isPreview;
  }


  @Override
  public boolean isPreview() {
    return isPreview;
  }


  @Override
  public ZonedDateTime getRequestTime() {
    return requestTime;
  }

  @Override
  public void setRequestTime(ZonedDateTime time) {
    if (requestTime != null) {
      throw new IllegalStateException("request time already set");
    }
    this.requestTime = time;
  }

  @Override
  public ZonedDateTime getNextDateTimeChange() {
    return nextDateTimeChange;
  }

  @Override
  public void updateNextDateTimeChange(ZonedDateTime time) {
    synchronized (nextDateTimeChangeLock) {
      if (nextDateTimeChange == null || time.isBefore(nextDateTimeChange)) {
        nextDateTimeChange = time;
      }
    }
  }


  @Override
  public <T> T getProperty(@NotNull String propertyName, @NotNull Class<T> targetClass) {
    Object value = properties.get(propertyName);
    if (targetClass.isInstance(value)) {
      return targetClass.cast(value);
    }
    return null;
  }

  @Override
  public void setProperty(@NotNull String propertyName, @NotNull Object value) {
    properties.put(propertyName, value);
  }

  @Override
  public <T> void testAndSetProperty(@NotNull String propertyName, @NotNull UnaryOperator<T> operator, @NotNull Class<T> targetClass) {
    synchronized (properties) {
      setProperty(propertyName, operator.apply(getProperty(propertyName, targetClass)));
    }
  }
}
