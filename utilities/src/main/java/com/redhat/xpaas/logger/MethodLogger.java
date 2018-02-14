package com.redhat.xpaas.logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.Method;

@Aspect
@SuppressWarnings
  (
    {
      "PMD.AvoidCatchingThrowable",
      "PMD.TooManyMethods",
      "PMD.CyclomaticComplexity"
    }
  )
public final class MethodLogger {

  @Around("execution(* *(..)) && @annotation(com.redhat.xpaas.logger.Loggable)")
  public Object wrapMethod(ProceedingJoinPoint point) throws Throwable {
    final Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
    Loggable annotation = method.getAnnotation(Loggable.class);
    return this.wrap(point, method, annotation);
  }

  @Around
    (
      "execution(public * (@com.redhat.xpaas.logger.Loggable *).*(..))"
        + " && !execution(String *.toString())"
        + " && !execution(int *.hashCode())"
        + " && !execution(boolean *.canEqual(Object))"
        + " && !execution(boolean *.equals(Object))"
    )
  public Object wrapClass(final ProceedingJoinPoint point) throws Throwable {
    final Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
    Object output;
    if (method.isAnnotationPresent(Loggable.class)) {
      output = point.proceed();
    } else {
      output = this.wrap(
        point,
        method,
        method.getDeclaringClass().getAnnotation(Loggable.class)
      );
    }
    return output;
  }

  private Object wrap(final ProceedingJoinPoint point, final Method method, final Loggable annotation) throws Throwable {

    if (Thread.interrupted()) {
      throw new IllegalStateException(
        String.format(
          "thread '%s' in group '%s' interrupted",
          Thread.currentThread().getName(),
          Thread.currentThread().getThreadGroup().getName()
        )
      );
    }

    String projectName = annotation.project().isEmpty() ? "radtests" : annotation.project();

    LogWrapper log = new LogWrapper(method.getDeclaringClass(), projectName);
    long start = System.currentTimeMillis();

    // Log start
    String message = annotation.message().isEmpty() ? method.getName() : annotation.message();

    Object result;
    log.start(message);
    try {
      result = point.proceed();
      log.finish(message, System.currentTimeMillis() - start);
    } catch(Throwable throwable) {
      String e = throwable.getMessage();
      if (method.getName().startsWith("test")){
        log.failed(message, "\"" + e.substring(0, Math.min(e.length(), 100)) + "...\"", System.currentTimeMillis() - start);
      } else {
        log.error(message, "\"" + e.substring(0, Math.min(e.length(), 100)) + "...\"");
      }
      throw throwable;
    }

    return result;
  }

}