package com.yammer.tenacity.core.bundle;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;
import com.yammer.tenacity.core.strategies.ManagedConcurrencyStrategy;

import javax.ws.rs.ext.ExceptionMapper;

public class TenacityBundle extends AbstractTenacityPropertyKeys implements Bundle {
    public TenacityBundle(TenacityPropertyKeyFactory keyFactory,
                          Iterable<TenacityPropertyKey> keys) {
        super(keyFactory, keys);
    }

    public TenacityBundle(TenacityPropertyKeyFactory keyFactory,
                          Iterable<TenacityPropertyKey> keys,
                          Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers) {
        super(keyFactory, keys, exceptionMappers);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
    }

    @Override
    public void run(Environment environment) {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new ManagedConcurrencyStrategy(environment));
        environment.addServlet(new HystrixMetricsStreamServlet(), "/tenacity/metrics.stream");
        for (ExceptionMapper<?> exceptionMapper : exceptionMappers) {
            environment.addProvider(exceptionMapper);
        }
        environment.addResource(new TenacityPropertyKeysResource(keys));
        environment.addResource(new TenacityConfigurationResource(keyFactory));
        environment.addResource(new TenacityCircuitBreakersResource(keys));
    }
}