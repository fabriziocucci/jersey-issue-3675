package com.github.fabriziocucci.jersey.issue3675;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationWithJerseyAndHk2BindersTest extends JerseyTest {
	
	private static final CountDownLatch COUNT_DOWN_LATCH_FOR_JERSEY_BINDER_REGISTERED_ON_RESOURCE_CONFIG = new CountDownLatch(1);
	private static final CountDownLatch COUNT_DOWN_LATCH_FOR_JERSEY_BINDER_REGISTERED_ON_FEATURE_CONTEXT = new CountDownLatch(1);
	private static final CountDownLatch COUNT_DOWN_LATCH_FOR_HK2_BINDER_REGISTERED_ON_RESOURCE_CONFIG = new CountDownLatch(1);
	private static final CountDownLatch COUNT_DOWN_LATCH_FOR_HK2_BINDER_REGISTERED_ON_FEATURE_CONTEXT = new CountDownLatch(1);
	
	@Override
	protected Application configure() {
		return new ResourceConfig()
				.register(new org.glassfish.jersey.internal.inject.AbstractBinder() {
					@Override
					protected void configure() {
						COUNT_DOWN_LATCH_FOR_JERSEY_BINDER_REGISTERED_ON_RESOURCE_CONFIG.countDown();
					}
				})
				.register(new Feature() {
					@Override
					public boolean configure(FeatureContext context) {
						context.register(new org.glassfish.jersey.internal.inject.AbstractBinder() {
							@Override
							protected void configure() {
								COUNT_DOWN_LATCH_FOR_JERSEY_BINDER_REGISTERED_ON_FEATURE_CONTEXT.countDown();
							}
						});	
						return true;
					}
					
				})
				.register(new org.glassfish.hk2.utilities.binding.AbstractBinder() {
					@Override
					protected void configure() {
						COUNT_DOWN_LATCH_FOR_HK2_BINDER_REGISTERED_ON_RESOURCE_CONFIG.countDown();
					}
				})
				.register(new Feature() {
					@Override
					public boolean configure(FeatureContext context) {
						context.register(new org.glassfish.hk2.utilities.binding.AbstractBinder() {
							@Override
							protected void configure() {
								COUNT_DOWN_LATCH_FOR_HK2_BINDER_REGISTERED_ON_FEATURE_CONTEXT.countDown();
							}
						});	
						return true;
					}
					
				});
	}
	
	@Test
	public void testThatJerseyBinderRegisteredOnResourceConfigIsConfigured() throws InterruptedException {
		assertThatBinderIsConfigured(COUNT_DOWN_LATCH_FOR_JERSEY_BINDER_REGISTERED_ON_RESOURCE_CONFIG);
	}
	
	@Test
	public void testThatJerseyBinderRegisteredOnFeatureContextIsConfigured() throws InterruptedException {
		assertThatBinderIsConfigured(COUNT_DOWN_LATCH_FOR_JERSEY_BINDER_REGISTERED_ON_FEATURE_CONTEXT);
	}
	
	@Test
	public void testThatHk2BinderRegisteredOnResourceConfigIsConfigured() throws InterruptedException {
		assertThatBinderIsConfigured(COUNT_DOWN_LATCH_FOR_HK2_BINDER_REGISTERED_ON_RESOURCE_CONFIG);
	}
	
	@Test
	public void testThatHk2BinderRegisteredOnFeatureContextIsConfigured() throws InterruptedException {
		assertThatBinderIsConfigured(COUNT_DOWN_LATCH_FOR_HK2_BINDER_REGISTERED_ON_FEATURE_CONTEXT);
	}
	
	private static void assertThatBinderIsConfigured(CountDownLatch countDownLatch) throws InterruptedException {
		boolean isBinderConfigured = countDownLatch.await(5L, TimeUnit.SECONDS);
		Assert.assertTrue("Binder is not configured", isBinderConfigured);
	}
	
}
