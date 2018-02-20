package com.redhat.xpaas.wait;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.openshift.api.model.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitUtil {

	protected static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
	public static final long DEFAULT_WAIT_INTERVAL = 1000L; // one second
  private static final Long TIMEOUT = RadConfiguration.timeout();
  private static final Logger LOGGER = LoggerFactory.getLogger(WaitUtil.class);

	public static boolean hasBuildFailed(Predicate<Build> filter) {
		final AtomicBoolean ret = new AtomicBoolean(false);

		openshift.getBuilds().stream().filter(filter).forEach(build -> {
			if ("Failed".equals(build.getStatus().getPhase())) {
				ret.set(true);
			}
		});

		return ret.get();
	}

	public static boolean hasAnyBuildFailed() {
		return hasBuildFailed(_x -> true);
	}

	public static boolean waitForPodsToReachRunningState(String labelKey, String labelValue, int podCount) throws TimeoutException, InterruptedException {
		BooleanSupplier successCondition = () -> openshift.podRunning(labelKey, labelValue);
		return WaitUtil.waitFor(successCondition);
	}

	public static boolean waitForActiveBuildsToComplete() throws TimeoutException, InterruptedException {
    BooleanSupplier successCondition = () -> openshift.getBuilds().stream().filter(
      build -> build.getStatus().getPhase().equals("Complete")).count() == openshift.getBuilds().size();

    BooleanSupplier failCondition = () -> openshift.getBuilds().stream().filter(
      build -> build.getStatus().getPhase().equals("Cancelled") || build.getStatus().getPhase().equals("Failed"))
      .count() > 0;

		return WaitUtil.waitFor(successCondition, failCondition, DEFAULT_WAIT_INTERVAL, TIMEOUT);
  }

  public static void waitForRoute(String routeName, Long timeOut) throws TimeoutException, InterruptedException {
    BooleanSupplier successCondition =  () -> openshift.getRouteStatus(routeName);
		WaitUtil.waitFor(successCondition, null, DEFAULT_WAIT_INTERVAL, timeOut);
  }

  public static boolean isPodReady(Pod pod) {
		if (pod.getStatus().getConditions() != null) {
			Optional<PodCondition> readyCondition = pod.getStatus().getConditions().stream().filter(condition -> "Ready".equals(condition.getType())).findFirst();
			if (readyCondition.isPresent()) {
				return "True".equals(readyCondition.get().getStatus());
			}
		}

		return false;
	}

	public static boolean hasPodRestartedAtLeastNTimes(Pod pod, int n) {
		if (pod.getStatus().getContainerStatuses() != null) {
			return pod.getStatus().getContainerStatuses().stream().filter(stats -> stats.getRestartCount() >= n).count() > 0;
		}

		return false;
	}

	public static boolean hasPodRestarted(Pod pod) {
		if (pod.getStatus().getContainerStatuses() != null) {
			return pod.getStatus().getContainerStatuses().stream().filter(stats -> stats.getRestartCount() > 0).count() > 0;
		}

		return false;
	}

	private static boolean _areNPodsReady(Predicate<Pod> podFilter, int n) {
		return openshift.getPods().stream().filter(podFilter).filter(WaitUtil::isPodReady).count() >= n;
	}

	private static boolean _areExactlyNPodsReady(Predicate<Pod> podFilter, int n) {
		return openshift.getPods().stream().filter(podFilter).filter(WaitUtil::isPodReady).count() == n;
	}

	private static boolean _areExactlyNPodsReady(int n) {
		return openshift.getPods().stream().filter(WaitUtil::isPodReady).count() == n;
	}

	private static boolean hasAnyPodRestarted(Predicate<Pod> podFilter) {
		return openshift.getPods().stream().filter(podFilter).filter(WaitUtil::hasPodRestarted).count() > 0;
	}

	public static boolean isAPodReady(Predicate<Pod> podFilter) {
		return _areNPodsReady(podFilter, 1);
	}

	public static BooleanSupplier isAPodReady(String appName) {
		return () -> isAPodReady(pod -> appName.equals(pod.getMetadata().getLabels().get("app")));
	}

  public static BooleanSupplier areNWorkerReady(int n) {
    return () -> _areNPodsReady(pod -> "worker".equals(pod.getMetadata().getLabels().get("oshinko-type")), n);
  }

  public static BooleanSupplier areNMastersReady(int n) {
    return () -> _areNPodsReady(pod -> "master".equals(pod.getMetadata().getLabels().get("oshinko-type")), n);
  }

	public static BooleanSupplier isAPodReady(final String labelName, final String appName) {
		return () -> isAPodReady(pod -> appName.equals(pod.getMetadata().getLabels().get(labelName)));
	}

	public static BooleanSupplier areNPodsReady(String appName, int n) {
		return () -> _areNPodsReady(pod -> appName.equals(pod.getMetadata().getLabels().get("name")), n);
	}

	public static BooleanSupplier areNPodsReady(final String labelName, final String labelValue, int n) {
		return () -> _areNPodsReady(pod -> labelValue.equals(pod.getMetadata().getLabels().get(labelName)), n);
	}

	public static BooleanSupplier areExactlyNPodsReady(final String labelName, final String labelValue, int n) {
		return () -> _areExactlyNPodsReady(pod -> labelValue.equals(pod.getMetadata().getLabels().get(labelName)), n);
	}

	public static BooleanSupplier areExactlyNPodsReady(int n) {
		return () -> _areExactlyNPodsReady(n);
	}

	public static BooleanSupplier areExactlyNPodsReady(String appName, int n) {
		return () -> _areExactlyNPodsReady(pod -> appName.equals(pod.getMetadata().getLabels().get("app")), n);
	}

	public static BooleanSupplier areNPodsReady(Predicate<Pod> podFilter, int n) {
		return () -> _areNPodsReady(podFilter, n);
	}

	public static BooleanSupplier areNoPodsPresent(final String appName) {
		return () -> openshift.findNamedPods(appName).size() == 0;
	}

	public static BooleanSupplier areNoPodsPresent(Predicate<Pod> podFilter) {
		return () -> openshift.getPods().stream().filter(podFilter).count() == 0;
	}

	public static BooleanSupplier hasPodRestarted(String appName) {
		return () -> hasAnyPodRestarted(pod -> appName.equals(pod.getMetadata().getLabels().get("app")));
	}

	public static BooleanSupplier hasPodRestarted(final String labelName, final String labelValue) {
		return () -> hasAnyPodRestarted(pod -> labelValue.equals(pod.getMetadata().getLabels().get(labelName)));
	}

	public static BooleanSupplier hasPodRestarted(Predicate<Pod> podFilter) {
		return () -> hasAnyPodRestarted(podFilter);
	}

	public static BooleanSupplier hasPodRestartedAtLeastNTimes(Predicate<Pod> podFilter, int n) {
		return () -> openshift.getPods().stream().filter(podFilter).filter(p -> WaitUtil.hasPodRestartedAtLeastNTimes(p, n)).count() > 0;
	}

	public static BooleanSupplier isAPvcBound(String pvcName) { return () -> openshift.getPersistentVolumeClaim(pvcName).getStatus().getPhase().equals("Bound"); }

	public static BooleanSupplier conditionTrueForNIterations(BooleanSupplier condition, int iters) {
		final AtomicInteger ai = new AtomicInteger(0);

		return () -> {
			if (condition.getAsBoolean()) {
				int i = ai.incrementAndGet();
				return i >= iters;
			} else {
				ai.set(0);
				return false;
			}
		};
	}

  public static <X> boolean waitFor(Supplier<X> supplier, Function<X, Boolean> trueCondition, Function<X, Boolean> failCondition, long interval, long timeout) throws InterruptedException, TimeoutException {
		timeout = System.currentTimeMillis() + timeout;

		while (System.currentTimeMillis() < timeout) {

			X x = supplier.get();

			if (failCondition != null && failCondition.apply(x)) {
				return false;
			}

			if (trueCondition.apply(x)) {
				return true;
			}

			Thread.sleep(interval);
		}

		throw new TimeoutException();
	}

	public static boolean waitFor(BooleanSupplier condition, BooleanSupplier failCondition, long interval, long timeout) throws InterruptedException, TimeoutException {

		timeout = System.currentTimeMillis() + timeout;

		while (System.currentTimeMillis() < timeout) {

			if (failCondition != null && failCondition.getAsBoolean()) {
				return false;
			}

			if (condition.getAsBoolean()) {
				return true;
			}

			Thread.sleep(interval);
		}

		throw new TimeoutException();
	}

	public static boolean waitFor(BooleanSupplier condition, BooleanSupplier failCondition) throws InterruptedException, TimeoutException {
		return waitFor(condition, failCondition, DEFAULT_WAIT_INTERVAL, RadConfiguration.timeout());
	}

	public static boolean waitFor(BooleanSupplier condition) throws InterruptedException, TimeoutException {
		return waitFor(condition, null, DEFAULT_WAIT_INTERVAL, RadConfiguration.timeout());
	}

	public static void assertEventually(String message, BooleanSupplier condition, long interval, long timeout) throws InterruptedException {
		try {
			waitFor(condition, null, interval, timeout);
		} catch (TimeoutException x) {
			throw new AssertionError(message, x);
		}
	}

	public static void assertEventually(String message, BooleanSupplier condition) throws InterruptedException {
		assertEventually(message, condition, DEFAULT_WAIT_INTERVAL, RadConfiguration.timeout());
	}
}

