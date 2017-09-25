package com.github.fabriziocucci.jersey.issue3675;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationWithHk2BinderTest extends JerseyTest {

	private static final CountDownLatch COUNT_DOWN_LATCH_FOR_HK2_BINDER_OUTSIDE_FEATURE = new CountDownLatch(1);
	private static final CountDownLatch COUNT_DOWN_LATCH_FOR_HK2_BINDER_INSIDE_FEATURE = new CountDownLatch(1);
	
	@Override
	protected Application configure() {
		return new ResourceConfig()
				.register(new AbstractBinder() {
					@Override
					protected void configure() {
						COUNT_DOWN_LATCH_FOR_HK2_BINDER_OUTSIDE_FEATURE.countDown();
					}
				})
				.register(new Feature() {
					@Override
					public boolean configure(FeatureContext context) {
						context.register(new AbstractBinder() {
							@Override
							protected void configure() {
								COUNT_DOWN_LATCH_FOR_HK2_BINDER_INSIDE_FEATURE.countDown();
							}
						});	
						return true;
					}
					
				});
	}
	
	@Test
	public void testThatHk2BinderRegisteredOutsideFeatureIsConfigured() throws InterruptedException {
		assertThatHk2BinderIsConfigured(COUNT_DOWN_LATCH_FOR_HK2_BINDER_OUTSIDE_FEATURE);
	}
	
	@Test
	public void testThatHk2BinderRegisteredInsideFeatureIsNotConfigured() throws InterruptedException {
		assertThatHk2BinderIsConfigured(COUNT_DOWN_LATCH_FOR_HK2_BINDER_INSIDE_FEATURE);
	}
	
	private static void assertThatHk2BinderIsConfigured(CountDownLatch countDownLatch) throws InterruptedException {
		boolean isBinderConfigured = countDownLatch.await(5L, TimeUnit.SECONDS);
		Assert.assertTrue("HK2 Binder is not configured", isBinderConfigured);
	}
	
}
