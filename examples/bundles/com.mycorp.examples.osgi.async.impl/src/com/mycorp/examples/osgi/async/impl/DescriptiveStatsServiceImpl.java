/****************************************************************************
 * Copyright (c) 2018 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package com.mycorp.examples.osgi.async.impl;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.osgi.service.component.annotations.Component;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import com.mycorp.examples.osgi.async.DescriptiveStatsService;

@Component(property = { "service.exported.interfaces=*", "service.intents=osgi.async", "service.exported.configs=ecf.generic.server" })
public class DescriptiveStatsServiceImpl implements DescriptiveStatsService {

	@Override
	public CompletableFuture<Double> mean(Collection<Float> data) {
		CompletableFuture<Double> cf = new CompletableFuture<Double>();
		if (data == null)
			cf.completeExceptionally(new NullPointerException("data must not be null"));
		double sum = 0.0;
		for(Float datum: data) 
			sum += datum;
		if (data.isEmpty())
			cf.complete(Double.NaN);
		else
			cf.complete(sum/data.size());
		return cf;
	}

	@Override
	public CompletionStage<Long> sum(Long... data) {
		CompletableFuture<Long> cf = new CompletableFuture<Long>();
		if (data == null)
			cf.completeExceptionally(new NullPointerException("data must not be null"));
		long sum = 0;
		for(Long datum: data) 
			sum += datum;
		cf.complete(sum);
		return cf;
	}

	@Override
	public Promise<Double> sumsq(Collection<Double> data) {
		Deferred<Double> d = new Deferred<Double>();
		if (data == null)
			d.fail(new NullPointerException("data must not be null"));
		double sumsq = 0.0;
		for(Double datum: data)
			sumsq += datum.doubleValue() * datum.doubleValue();
		d.resolve(sumsq);
		return d.getPromise();
	}

}
